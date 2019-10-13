
package edu.touro.mco152.bm;

import edu.touro.mco152.bm.ui.MarkResetObject;
import java.io.File;
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
    private boolean firstPass = true;

    private BenchmarkInterface firstBenchmark;
    private BenchmarkInterface secondBenchmark;

    public int nextMarkNumber = 1;   // number of the next mark

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
        System.out.println("MyDiskWorker: starting the Benchmarking process");
        user.msg("Running readTest "+appInstance.runConfigs.readTest+"   writeTest "+appInstance.runConfigs.writeTest);
        user.msg("num files: "+appInstance.runConfigs.numOfMarks+", num blks: "+appInstance.runConfigs.numOfBlocks
                +", blk size (kb): "+appInstance.runConfigs.blockSizeKb+", blockSequence: "+appInstance.runConfigs.blockSequence);


        firstBenchmark = new WriteBenchmark(this, appInstance.runConfigs, user);
        secondBenchmark = new ReadBenchmark(this, appInstance.runConfigs, user);


        rUnitsComplete = 0;
        wUnitsComplete = 0;
        unitsComplete = 0;

        int wUnitsTotal = appInstance.runConfigs.writeTest ? appInstance.runConfigs.numOfBlocks * appInstance.runConfigs.numOfMarks : 0;
        int rUnitsTotal = appInstance.runConfigs.readTest ? appInstance.runConfigs.numOfBlocks * appInstance.runConfigs.numOfMarks : 0;
        unitsTotal = wUnitsTotal + rUnitsTotal;


        //----------------------------------------first test

        user.updateLegend();

        if (appInstance.runConfigs.autoReset) {
            nextMarkNumber = 1;
            appInstance.resetTestData();
        }

        int startFileNum = nextMarkNumber;


        /////////////////////////////////////////////////////////////////////////


        if(appInstance.runConfigs.writeTest)
        {
            firstBenchmark.initializeRun();

            user.msg("disk info: ("+ firstBenchmark.getItemInfo()+")");
            user.setTitle(firstBenchmark.getItemInfo());


            if (!appInstance.runConfigs.multiFile) {
                appInstance.runConfigs.testFile = new File(appInstance.runConfigs.dataDir.getAbsolutePath()+File.separator+"testdata.jdm");
            }


            for (int m=startFileNum; m<startFileNum+appInstance.runConfigs.numOfMarks && !user.iIsCancelled(); m++)
            {
                firstBenchmark.run(m);
            }
            nextMarkNumber += appInstance.runConfigs.numOfMarks;
            firstBenchmark.persistRun(firstPass);
            firstBenchmark.addRunToGui();
            firstPass = false;
        }


        // try renaming all files to clear catch
        if (appInstance.runConfigs.readTest && appInstance.runConfigs.writeTest && !user.iIsCancelled()) {
            user.showFileRenamingMessage();
        }


        //--------------------------------------read test

        if (appInstance.runConfigs.readTest) {
            secondBenchmark.initializeRun();


            for (int m = startFileNum; m < startFileNum + appInstance.runConfigs.numOfMarks && !user.iIsCancelled(); m++)
            {
                secondBenchmark.run(m);
            }
            nextMarkNumber += appInstance.runConfigs.numOfMarks;
            secondBenchmark.persistRun(firstPass);
            secondBenchmark.addRunToGui();
            firstPass = false;
        }


        return true;
    }

    public void resetSequence(){nextMarkNumber = 1;}

    public MarkResetObject resetTestData()
    {
        nextMarkNumber = 1;
        return new MarkResetObject(firstBenchmark.resetTestData(), secondBenchmark.resetTestData());
    }

    public void incrementWriteUnits()
    {
        wUnitsComplete++;
        unitsComplete = rUnitsComplete + wUnitsComplete;
        user.iSetProgress((int)((float)unitsComplete/(float)unitsTotal * 100f));
    }

    public void incrementReadUnits()
    {
        rUnitsComplete++;
        unitsComplete = rUnitsComplete + wUnitsComplete;
        user.iSetProgress((int)((float)unitsComplete/(float)unitsTotal * 100f));
    }

}
















