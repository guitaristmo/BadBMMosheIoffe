package edu.touro.mco152.bm;

import edu.touro.mco152.bm.persist.RunInterface;

public interface BenchmarkInterface
{
    void run(int loopIterationCounter);

    void persistRun(boolean firstPass);

    void addRunToGui();

    void initializeRun();

    String getItemInfo();

    DiskMark resetTestData();
}
