package edu.touro.mco152.bm;

/**
 * This is the interface for a Benchmark
 * Any implementing class must know how to run one
 * iteration of a benchmark and how to persist it(or not too)
 * It must have an object called a Mark, which actually stores the data
 * about the run, for persistence and output purposes.
 */
public interface BenchmarkInterface
{
    Mark run(int loopIterationCounter);

    void addRunToGui();

    void initializeRun();

    String getItemInfo();

    void persistRun(boolean firstPass);

    DiskMark resetTestData();


}
