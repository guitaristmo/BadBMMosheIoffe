package edu.touro.mco152.bm;

import edu.touro.mco152.bm.persist.DiskRun;
import edu.touro.mco152.bm.persist.EM;
import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import static edu.touro.mco152.bm.DiskMark.MarkType.READ;
import static edu.touro.mco152.bm.MyApp.KILOBYTE;
import static edu.touro.mco152.bm.MyApp.MEGABYTE;

/**
 * Stores all the information about how to run Read Benchmark and persist it
 */
public class ReadBenchmark implements BenchmarkInterface
{
    private DiskRun run;
    private DiskMark rMark;
    private MyDiskWorker testRunner;
    private GuiInterface userInterface;
    private RunConfigSetting configSettings;
    private EntityManager em;

    private int blockSize;
    private byte [] blockArr;


    public ReadBenchmark(MyDiskWorker testRunner, RunConfigSetting configSettings, GuiInterface userInterface)
    {
        this.testRunner = testRunner;
        this.configSettings = configSettings;
        this.userInterface = userInterface;
        run = new DiskRun(DiskRun.IOMode.WRITE, configSettings.blockSequence);
        rMark = new DiskMark(READ);
    }

    @Override
    public DiskMark run(int m)
    {
        System.out.println("Read Benchmark: starting reading benchmark ");
        if (configSettings.multiFile) {
            configSettings.testFile = new File(configSettings.dataDir.getAbsolutePath()
                    + File.separator+"testdata"+m+".jdm");
        }

        rMark.setMarkNum(m);
        long startTime = System.nanoTime();
        long totalBytesReadInMark = 0;

        try (RandomAccessFile rAccFile = new RandomAccessFile(configSettings.testFile,"r")) {
            for (int b=0; b<configSettings.numOfBlocks; b++)
            {
                if (configSettings.blockSequence == DiskRun.BlockSequence.RANDOM) {
                    int rLoc = Util.randInt(0, configSettings.numOfBlocks-1);
                    rAccFile.seek(rLoc*blockSize);
                } else {
                    rAccFile.seek(b*blockSize);
                }

                rAccFile.readFully(blockArr, 0, blockSize);
                totalBytesReadInMark += blockSize;
                testRunner.incrementReadUnits();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        long endTime = System.nanoTime();
        long elapsedTimeNs = endTime - startTime;
        double sec = (double)elapsedTimeNs / (double)1000000000;
        double mbRead = (double) totalBytesReadInMark / (double) MEGABYTE;
        rMark.setBwMbSec(mbRead / sec);
        userInterface.msg("m:"+m+" READ IO is "+rMark.getBwMbSec()+" MB/s    "
                + "(MBread "+mbRead+" in "+sec+" sec)");
        rMark.updateMetrics();
        System.out.println("Read Benchmark: about to publish read mark");

        run.setRunMax(rMark.getCumMax());
        run.setRunMin(rMark.getCumMin());
        run.setRunAvg(rMark.getCumAvg());
        run.setEndTime(new Date());
        return rMark;
    }

    @Override
    public void persistRun(boolean firstPass)
    {
        if(firstPass)
    {
        System.out.println("worker: about to get em");
        em = EM.getEntityManager();
        em.getTransaction().begin();
    }
        em.persist(run);
        em.getTransaction().commit();
    }

    @Override
    public DiskMark resetTestData()
    {
        rMark.resetTestData();
        return rMark;
    }

    @Override
    public void initializeRun()
    {
        blockSize = configSettings.blockSizeKb*KILOBYTE;
        blockArr = new byte [blockSize];
        for (int b=0; b<blockArr.length; b++) {
            if (b%2==0) {
                blockArr[b]=(byte)0xFF;
            }
        }

        run.setNumMarks(configSettings.numOfMarks);
        run.setNumBlocks(configSettings.numOfBlocks);
        run.setBlockSize(configSettings.blockSizeKb);
        run.setTxSize(configSettings.targetTxSizeKb());
        run.setDiskInfo(Util.getDiskInfo(configSettings.dataDir));
    }

    @Override
    public String getItemInfo() {
        return run.getDiskInfo();
    }

    @Override
    public void addRunToGui() { userInterface.addRun(run); }
}
