
package edu.touro.mco152.bm;

//import static edu.touro.mco152.bm.App.msg;



import static edu.touro.mco152.bm.MyApp.KILOBYTE;
import static edu.touro.mco152.bm.MyApp.MEGABYTE;
//import static edu.touro.mco152.bm.MyApp.blockSizeKb;
//import static edu.touro.mco152.bm.MyApp.dataDir;
//import static edu.touro.mco152.bm.MyApp.numOfBlocks;
//import static edu.touro.mco152.bm.MyApp.numOfMarks;
//import static edu.touro.mco152.bm.MyApp.testFile;
import static edu.touro.mco152.bm.DiskMark.MarkType.READ;
import static edu.touro.mco152.bm.DiskMark.MarkType.WRITE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import edu.touro.mco152.bm.persist.DiskRun;
import edu.touro.mco152.bm.persist.EM;


/**
 * Thread running the disk benchmarking. only one of these threads can run at
 * once.
 */
public class MyDiskWorker
{
    GuiInterface user;
    MyApp appInstance;
    EntityManager em;
    DiskRun run;
    private boolean firstPass = true;

    public MyDiskWorker(MyApp appInstance, GuiInterface user)
    {
        this.appInstance = appInstance;
        this.user = user;
    }

    protected Boolean newDoInBackground() throws Exception
    {
        System.out.println("*** starting new worker thread in MyDiskRun");
        user.msg("Running readTest "+appInstance.readTest+"   writeTest "+appInstance.writeTest);
        user.msg("num files: "+appInstance.numOfMarks+", num blks: "+appInstance.numOfBlocks
                +", blk size (kb): "+appInstance.blockSizeKb+", blockSequence: "+appInstance.blockSequence);

        int wUnitsComplete = 0,
                rUnitsComplete = 0,
                unitsComplete;

        int wUnitsTotal = appInstance.writeTest ? appInstance.numOfBlocks * appInstance.numOfMarks : 0;
        int rUnitsTotal = appInstance.readTest ? appInstance.numOfBlocks * appInstance.numOfMarks : 0;
        int unitsTotal = wUnitsTotal + rUnitsTotal;
        float percentComplete;

        int blockSize = appInstance.blockSizeKb*KILOBYTE;
        byte [] blockArr = new byte [blockSize];
        for (int b=0; b<blockArr.length; b++) {
            if (b%2==0) {
                blockArr[b]=(byte)0xFF;
            }
        }

        DiskMark wMark, rMark;
        user.updateLegend();

        if (appInstance.autoReset) {
            appInstance.resetTestData();
            user.resetTestData();
        }

        int startFileNum = appInstance.nextMarkNumber;

        if(appInstance.writeTest) {
            run = new DiskRun(DiskRun.IOMode.WRITE, appInstance.blockSequence);
            setDiskRunStuff();


            if (!appInstance.multiFile) {
                appInstance.testFile = new File(appInstance.dataDir.getAbsolutePath()+File.separator+"testdata.jdm");
            }
            for (int m=startFileNum; m<startFileNum+appInstance.numOfMarks && !user.iIsCancelled(); m++) {

                if (appInstance.multiFile) {
                    appInstance.testFile = new File(appInstance.dataDir.getAbsolutePath()
                            + File.separator+"testdata"+m+".jdm");
                }
                wMark = new DiskMark(WRITE);
                wMark.setMarkNum(m);
                long startTime = System.nanoTime();
                long totalBytesWrittenInMark = 0;

                String mode = "rw";
                if (appInstance.writeSyncEnable) { mode = "rwd"; }

                try {
                    try (RandomAccessFile rAccFile = new RandomAccessFile(appInstance.testFile,mode)) {
                        for (int b=0; b<appInstance.numOfBlocks; b++) {
                            if (appInstance.blockSequence == DiskRun.BlockSequence.RANDOM) {
                                int rLoc = Util.randInt(0, appInstance.numOfBlocks-1);
                                rAccFile.seek(rLoc*blockSize);
                            } else {
                                rAccFile.seek(b*blockSize);
                            }
                            rAccFile.write(blockArr, 0, blockSize);
                            totalBytesWrittenInMark += blockSize;
                            wUnitsComplete++;
                            unitsComplete = rUnitsComplete + wUnitsComplete;
                            percentComplete = (float)unitsComplete/(float)unitsTotal * 100f;
                            user.iSetProgress((int)percentComplete);
                        }
                    }
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(MyApp.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(MyApp.class.getName()).log(Level.SEVERE, null, ex);
                }
                long endTime = System.nanoTime();
                long elapsedTimeNs = endTime - startTime;
                double sec = (double)elapsedTimeNs / (double)1000000000;
                double mbWritten = (double)totalBytesWrittenInMark / (double)MEGABYTE;
                wMark.setBwMbSec(mbWritten / sec);
                user.msg("m:"+m+" write IO is "+wMark.getBwMbSecAsString()+" MB/s     "
                        + "("+Util.displayString(mbWritten)+ "MB written in "
                        + Util.displayString(sec)+" sec)");
                appInstance.updateMetrics(wMark);
                user.iPublish(wMark);

                run.setRunMax(wMark.getCumMax());
                run.setRunMin(wMark.getCumMin());
                run.setRunAvg(wMark.getCumAvg());
                run.setEndTime(new Date());
            }

            //Check out the comment in the read section
            persistRun(run);

            user.addRun(run);
        }


        // try renaming all files to clear catch
        if (appInstance.readTest && appInstance.writeTest && !user.iIsCancelled()) {
            user.showFileRenamingMessage();
        }

        if (appInstance.readTest) {
            run = new DiskRun(DiskRun.IOMode.READ, appInstance.blockSequence);
            setDiskRunStuff();

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
                } catch (IOException ex) {
                    Logger.getLogger(MyApp.class.getName()).log(Level.SEVERE, null, ex);
                }
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

    private void setDiskRunStuff()
    {
        run.setNumMarks(appInstance.numOfMarks);
        run.setNumBlocks(appInstance.numOfBlocks);
        run.setBlockSize(appInstance.blockSizeKb);
        run.setTxSize(appInstance.targetTxSizeKb());
        run.setDiskInfo(Util.getDiskInfo(appInstance.dataDir));
        user.msg("disk info: ("+ run.getDiskInfo()+")");
        user.setTitle(run.getDiskInfo());
    }



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
















