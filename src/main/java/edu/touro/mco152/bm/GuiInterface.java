package edu.touro.mco152.bm;

import edu.touro.mco152.bm.persist.Run;
import edu.touro.mco152.bm.ui.MarkResetObject;

import java.beans.PropertyChangeListener;

/**
 * This represents all user interaction that
 * diskworker and app will ever have
 * as well as how to run the benchmark - Swing...
 */
public interface GuiInterface
{
//output and user related
    void setTitle(String title);
    void updateLegend();
    void msg(String message);
    void outputMark(Object... chunks);
    void clearRuns();
    void refreshScreen();
    void addRun(Run run);
    void showFileRenamingMessage();
    void iAddPropertyChangeListener(PropertyChangeListener listener);

    void updateProgress(int progress);
    void setProgressString(String progress);

//cancel related
    boolean benchmarkIsCancelled();
    void cancelBenchmark(boolean cancel);

// related to running the benchmark
    void startBenchmark();
    void init();
    void setWorker(MyDiskWorker worker);
    void resetTestData(MarkResetObject marks);
}