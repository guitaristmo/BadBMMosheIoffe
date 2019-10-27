
package edu.touro.mco152.bm;

import edu.touro.mco152.bm.ui.MarkResetObject;
import java.io.File;


/**
 * Class for benchmarking a disk.
 * It is a platform for benchmarking things which implement BenchmarkInterfaces
 * using any user interface which extends GuiInterface, and receiving data about
 * the test from runConfigs (necessary because of the current GUI)
 */
public class MyDiskWorker
{
    private GuiInterface user;
    private RunConfigSetting runConfigs;
    private boolean firstPass = true;

    private BenchmarkInterface writingBenchmark;
    private BenchmarkInterface readingBenchmark;

    public int nextMarkNumber = 1;   // number of the next mark

    private int rUnitsComplete = 0;
    private int wUnitsComplete = 0;
    private int unitsComplete = 0;
    private int unitsTotal;

    public MyDiskWorker(RunConfigSetting runConfigs, GuiInterface user)
    {
        this.runConfigs = runConfigs;
        this.user = user;
        writingBenchmark = new WriteBenchmark(this, runConfigs, user);
        readingBenchmark = new ReadBenchmark(this, runConfigs, user);
    }

    public boolean runBenchmark() throws Exception
    {
        user.msg("Running readTest "+runConfigs.readTest+"   writeTest "+runConfigs.writeTest);
        user.msg("num files: "+runConfigs.numOfMarks+", num blks: "+runConfigs.numOfBlocks
                +", blk size (kb): "+runConfigs.blockSizeKb+", blockSequence: "+runConfigs.blockSequence);


        //initialize fields for keeping track of progress
        rUnitsComplete = 0;
        wUnitsComplete = 0;
        unitsComplete = 0;
        int wUnitsTotal = runConfigs.writeTest ? runConfigs.numOfBlocks * runConfigs.numOfMarks : 0;
        int rUnitsTotal = runConfigs.readTest ? runConfigs.numOfBlocks * runConfigs.numOfMarks : 0;
        unitsTotal = wUnitsTotal + rUnitsTotal;


        //----------------------------------------write test
        user.updateLegend();

        if (runConfigs.autoReset) {
            nextMarkNumber = 1;

            MarkResetObject marks = resetTestData();
            user.resetTestData(marks);
        }
        int startFileNum = nextMarkNumber;

        if(runConfigs.writeTest)
        {
            writingBenchmark.initializeRun();

            user.msg("disk info: ("+ writingBenchmark.getItemInfo()+")");
            user.setTitle(writingBenchmark.getItemInfo());


            if (!runConfigs.multiFile) {
                runConfigs.testFile = new File(runConfigs.dataDir.getAbsolutePath()+File.separator+"testdata.jdm");
            }


            for (int m=startFileNum; m<startFileNum+runConfigs.numOfMarks && !user.iIsCancelled(); m++)
            {
                user.iPublish(writingBenchmark.run(m));
            }
            nextMarkNumber += runConfigs.numOfMarks;
            writingBenchmark.persistRun(firstPass);
            writingBenchmark.addRunToGui();
            firstPass = false;
        }


        // try renaming all files to clear catch
        if (runConfigs.readTest && runConfigs.writeTest && !user.iIsCancelled()) {
            user.showFileRenamingMessage();
        }


        //--------------------------------------read test

        if (runConfigs.readTest) {
            readingBenchmark.initializeRun();


            for (int m = startFileNum; m < startFileNum + runConfigs.numOfMarks && !user.iIsCancelled(); m++)
            {
                user.iPublish(readingBenchmark.run(m));
            }
            nextMarkNumber += runConfigs.numOfMarks;
            readingBenchmark.persistRun(firstPass);
            readingBenchmark.addRunToGui();
            firstPass = false;
        }
        return true;
    }


    public void resetSequence(){nextMarkNumber = 1;}

    public MarkResetObject resetTestData()
    {
        nextMarkNumber = 1;
        return new MarkResetObject(writingBenchmark.resetTestData(), readingBenchmark.resetTestData());
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