package edu.touro.mco152.bm;

import edu.touro.mco152.bm.persist.DiskRun;
import java.beans.PropertyChangeListener;

public interface GuiInterface
{
    //from Gui
    void updateLegend();
    void resetTestData();


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
