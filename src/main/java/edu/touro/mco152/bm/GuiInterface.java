package edu.touro.mco152.bm;

import edu.touro.mco152.bm.persist.DiskRun;

import java.beans.PropertyChangeListener;
import java.util.List;

public interface GuiInterface
{
    //from Gui
    public void updateLegend();
    public void resetTestData();


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
    void setUpDisplay();
    void init();
    void clearRuns();
    void adjustSensitivity();
    void setWorker(MyDiskWorker worker);

}
