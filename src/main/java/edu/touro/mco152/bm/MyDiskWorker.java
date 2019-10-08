
package edu.touro.mco152.bm;

import static edu.touro.mco152.bm.MyApp.KILOBYTE;
import static edu.touro.mco152.bm.MyApp.MEGABYTE;
import static edu.touro.mco152.bm.DiskMark.MarkType.READ;
import static edu.touro.mco152.bm.DiskMark.MarkType.WRITE;
import edu.touro.mco152.bm.persist.DiskRun;
import edu.touro.mco152.bm.persist.EM;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;


/**
 * Class for benchmarking a disk.
 * SRP - by removing Swing, the only job of this class is to run the benchmark
 * not to make threading decisions(like Swing)
 */
public class MyDiskWorker implements BenchmarkWorker
{
    private GuiInterface user;
    private MyApp appInstance;
    private EntityManager em;
    //private DiskRun run;
    private boolean firstPass = true;

    private BenchmarkInterface firstBenchmark;
    private BenchmarkInterface secondBenchmark;

    private int rUnitsComplete = 0;
    private int wUnitsComplete = 0;
    private int unitsComplete = 0;
    private int unitsTotal;

    public MyDiskWorker(MyApp appInstance, GuiInterface user)
    {
        this.appInstance = appInstance;
        this.user = user;
    }

    @Override
    public boolean runBenchmark() throws Exception
    {
        System.out.println("*** starting new worker thread in MyDiskRun");
        user.msg("Running readTest "+appInstance.runConfigs.readTest+"   writeTest "+appInstance.runConfigs.writeTest);
        user.msg("num files: "+appInstance.runConfigs.numOfMarks+", num blks: "+appInstance.runConfigs.numOfBlocks
                +", blk size (kb): "+appInstance.runConfigs.blockSizeKb+", blockSequence: "+appInstance.runConfigs.blockSequence);

        rUnitsComplete = 0;
        wUnitsComplete = 0;
        unitsComplete = 0;

        int wUnitsTotal = appInstance.runConfigs.writeTest ? appInstance.runConfigs.numOfBlocks * appInstance.runConfigs.numOfMarks : 0;
        int rUnitsTotal = appInstance.runConfigs.readTest ? appInstance.runConfigs.numOfBlocks * appInstance.runConfigs.numOfMarks : 0;
        unitsTotal = wUnitsTotal + rUnitsTotal;
        float percentComplete;

        int blockSize = appInstance.runConfigs.blockSizeKb*KILOBYTE;
        byte [] blockArr = new byte [blockSize];
        for (int b=0; b<blockArr.length; b++) {
            if (b%2==0) {
                blockArr[b]=(byte)0xFF;
            }
        }

        //DiskMark wMark, rMark;
        user.updateLegend();

        if (appInstance.runConfigs.autoReset) {
            appInstance.resetTestData();
            user.resetTestData();
        }

        int startFileNum = appInstance.nextMarkNumber;






        /////////////////////////////////////////////////////////////////////////


        if(appInstance.runConfigs.writeTest) {
            firstBenchmark = new WriteBenchmark(this, appInstance.runConfigs);
            firstBenchmark.initializeRun();
            user.msg("disk info: ("+ firstBenchmark.getItemInfo()+")");
            user.setTitle(firstBenchmark.getItemInfo());

            if (!appInstance.runConfigs.multiFile) {
                appInstance.runConfigs.testFile = new File(appInstance.runConfigs.dataDir.getAbsolutePath()+File.separator+"testdata.jdm");
            }

            for (int m=startFileNum; m<startFileNum+appInstance.runConfigs.numOfMarks && !user.iIsCancelled(); m++) {

                firstBenchmark.run(m);
            }
        }


        // try renaming all files to clear catch
        if (appInstance.readTest && appInstance.writeTest && !user.iIsCancelled()) {
            user.showFileRenamingMessage();
        }

        if (appInstance.readTest) {
            run = new DiskRun(DiskRun.IOMode.READ, appInstance.blockSequence);
            initializeDiskRun();

            for (int m=startFileNum; m<startFileNum+appInstance.numOfMarks && !user.iIsCancelled(); m++) {

                if (appInstance.multiFile) {
                    appInstance.testFile = new File(appInstance.dataDir.getAbsolutePath()
                            + File.separator+"testdata"+m+".jdm");
                }

                rMark = new DiskMark(READ);
                rMark.setMarkNum(m);
                long startTime = System.nanoTime();
                long totalBytesReadInMark = 0;

                try {
                    try (RandomAccessFile rAccFile = new RandomAccessFile(appInstance.testFile,"r")) {
                        for (int b=0; b<appInstance.numOfBlocks; b++) {
                            if (appInstance.blockSequence == DiskRun.BlockSequence.RANDOM) {
                                int rLoc = Util.randInt(0, appInstance.numOfBlocks-1);
                                rAccFile.seek(rLoc*blockSize);
                            } else {
                                rAccFile.seek(b*blockSize);
                            }
                            rAccFile.readFully(blockArr, 0, blockSize);
                            totalBytesReadInMark += blockSize;
                            rUnitsComplete++;
                            unitsComplete = rUnitsComplete + wUnitsComplete;
                            percentComplete = (float)unitsComplete/(float)unitsTotal * 100f;
                            user.iSetProgress((int)percentComplete);
                        }
                    }
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(MyApp.class.getName()).log(Level.SEVERE, null, ex);
                }
        //only possible exception is FileNotFound. Second catch block is redundant
//                catch (IOException ex) {
//                    Logger.getLogger(MyApp.class.getName()).log(Level.SEVERE, null, ex);
//                }

                long endTime = System.nanoTime();
                long elapsedTimeNs = endTime - startTime;
                double sec = (double)elapsedTimeNs / (double)1000000000;
                double mbRead = (double) totalBytesReadInMark / (double) MEGABYTE;
                rMark.setBwMbSec(mbRead / sec);
                user.msg("m:"+m+" READ IO is "+rMark.getBwMbSec()+" MB/s    "
                        + "(MBread "+mbRead+" in "+sec+" sec)");
                appInstance.updateMetrics(rMark);
                user.iPublish(rMark);

                run.setRunMax(rMark.getCumMax());
                run.setRunMin(rMark.getCumMin());
                run.setRunAvg(rMark.getCumAvg());
                run.setEndTime(new Date());
                System.out.println("worker: end of for loop. mark number: " + m);
            }
            System.out.println("worker: finished reading for loop");

            //I wrote this to separate out the em stuff - its glitching up on the second run DiskWorker
            persistRun(run);

            user.addRun(run);
        }
        appInstance.nextMarkNumber += appInstance.numOfMarks;
        return true;
    }

    public void updateProgress(int progress)
    {
        unitsComplete = rUnitsComplete + wUnitsComplete;
        percentComplete = (float)unitsComplete/(float)unitsTotal * 100f;
        user.iSetProgress((int)percentComplete);

    }

//    private void initializeDiskRun()
//    {
//        run.setNumMarks(appInstance.numOfMarks);
//        run.setNumBlocks(appInstance.numOfBlocks);
//        run.setBlockSize(appInstance.blockSizeKb);
//        run.setTxSize(appInstance.targetTxSizeKb());
//        run.setDiskInfo(Util.getDiskInfo(appInstance.dataDir));
//        user.msg("disk info: ("+ run.getDiskInfo()+")");
//        user.setTitle(run.getDiskInfo());
//    }

    public void incrementWriteUnits()
    {
        wUnitsComplete++;
        unitsComplete = rUnitsComplete + wUnitsComplete;
        user.iSetProgress((int)((float)unitsComplete/(float)unitsTotal * 100f));
    }

    public void incrementReadUnits(){rUnitsComplete++;}

    private void persistRun(DiskRun run)
    {
        if(firstPass)
        {
            System.out.println("worker: about to get em");
            em = EM.getEntityManager();
            em.getTransaction().begin();
            firstPass = false;
            em.persist(run);
            em.getTransaction().commit();
        }
    }
}
















