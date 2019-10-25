package edu.touro.mco152.bm;

import edu.touro.mco152.bm.persist.DiskRun;
import edu.touro.mco152.bm.ui.MarkResetObject;

import java.beans.PropertyChangeListener;

/**
 * This represents all user interaction that
 * diskworker and app will ever do
 */
public interface GuiInterface
{
    //from Gui
    void updateLegend();
    void resetTestData(MarkResetObject marks);


    //for DiskWorker
    void setTitle(String title);
    void addRun(DiskRun run);
    void showFileRenamingMessage();


    //from Swing Worker
    boolean iIsCancelled();
    void iPublish(Object... chunks);
    void iCancel(boolean cancel);
    void iAddPropertyChangeListener(PropertyChangeListener listener);
    void iSetProgress(int progress);
    void iSetProgressBarString(String progress);
    void iExecute();


    //from app
    void msg(String message);
    void init();
    void clearRuns();
    void adjustSensitivity();
    void setWorker(MyDiskWorker worker);

}
