package edu.touro.mco152.bm;

import edu.touro.mco152.bm.persist.DiskRun;
import edu.touro.mco152.bm.persist.EM;
import edu.touro.mco152.bm.persist.RunInterface;
import org.eclipse.persistence.jpa.config.Entity;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.touro.mco152.bm.DiskMark.MarkType.WRITE;
import static edu.touro.mco152.bm.MyApp.KILOBYTE;
import static edu.touro.mco152.bm.MyApp.MEGABYTE;

public class WriteBenchmark implements BenchmarkInterface
{
    private DiskRun run;
    private DiskMark wMark;
    private MyDiskWorker testRunner;
    private GuiInterface userInterface;
    private RunConfigSetting configSettings;
    private EntityManager em;

    private int blockSize;
    private byte [] blockArr;


    public WriteBenchmark(MyDiskWorker testRunner, RunConfigSetting configSettings, GuiInterface userInterface)
    {
        this.testRunner = testRunner;
        this.configSettings = configSettings;
        this.userInterface = userInterface;
        run = new DiskRun(DiskRun.IOMode.WRITE, configSettings.blockSequence);
        wMark = new DiskMark(WRITE);
    }

    @Override
    public void run(int m)
    {
        if (configSettings.multiFile) {
            configSettings.testFile = new File(configSettings.dataDir.getAbsolutePath()
                    + File.separator+"testdata"+m+".jdm");
        }
        wMark.setMarkNum(m);
        long startTime = System.nanoTime();
        long totalBytesWrittenInMark = 0;

        String mode = "rw";
        if (configSettings.writeSyncEnable) { mode = "rwd"; }

        try {
            try (RandomAccessFile rAccFile = new RandomAccessFile(configSettings.testFile,mode)) {

                for (int b=0; b<configSettings.numOfBlocks; b++)
                {
                    if (configSettings.blockSequence == DiskRun.BlockSequence.RANDOM) {
                        int rLoc = Util.randInt(0, configSettings.numOfBlocks-1);
                        rAccFile.seek(rLoc*blockSize);
                    } else {
                        rAccFile.seek(b*blockSize);
                    }

                    rAccFile.write(blockArr, 0, blockSize);
                    totalBytesWrittenInMark += blockSize;
                    testRunner.incrementWriteUnits();
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MyApp.class.getName()).log(Level.SEVERE, null, ex);
        }
                catch (IOException ex) {
                    Logger.getLogger(MyApp.class.getName()).log(Level.SEVERE, null, ex);
                }
        long endTime = System.nanoTime();
        long elapsedTimeNs = endTime - startTime;
        double sec = (double)elapsedTimeNs / (double)1000000000;
        double mbWritten = (double)totalBytesWrittenInMark / (double)MEGABYTE;
        wMark.setBwMbSec(mbWritten / sec);
        userInterface.msg("m:"+m+" write IO is "+wMark.getBwMbSecAsString()+" MB/s     "
                + "("+Util.displayString(mbWritten)+ "MB written in "
                + Util.displayString(sec)+" sec)");
        wMark.updateMetrics();
        userInterface.iPublish(wMark);

        run.setRunMax(wMark.getCumMax());
        run.setRunMin(wMark.getCumMin());
        run.setRunAvg(wMark.getCumAvg());
        run.setEndTime(new Date());
    }

    @Override
    public DiskMark resetTestData()
    {
        wMark.resetTestData();
        return wMark;
    }

    @Override
    public void persistRun(boolean firstPass)
    {
        if(firstPass)
        {
            System.out.println("worker: about to get em");
            em = EM.getEntityManager();
            em.getTransaction().begin();
            em.persist(run);
            em.getTransaction().commit();
        }
    }

    @Override
    public void addRunToGui() { userInterface.addRun(run); }

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
    public String getItemInfo(){return run.getDiskInfo();}
}
