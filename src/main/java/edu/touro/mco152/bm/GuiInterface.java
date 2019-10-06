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
    public void setTitle(String title);
    public void addRun(DiskRun run);
    public void showFileRenamingMessage();


//    public ChartPanel chartPanel = null;
//    public MainFrame mainFrame = null;
//    public SelectFrame selFrame = null;
//    public XYSeries wSeries = null;
//    public XYSeries wAvgSeries = null;
//    public XYSeries wMaxSeries = null;
//    public XYSeries wMinSeries = null;
//    public XYSeries rSeries = null;
//    public XYSeries rAvgSeries = null;
//    public XYSeries rMaxSeries = null;
//    public XYSeries rMinSeries = null;
//    public JFreeChart chart = null;
//    public JProgressBar progressBar = null;
//    public RunPanel runPanel = null;

    //from Swing Worker
    public boolean iIsCancelled();
    public void iPublish(Object... chunks);
    public void iCancel(boolean cancel);
    public void iAddPropertyChangeListener(PropertyChangeListener listener);
    public void iSetProgress(int progress);
    public void iSetProgressBarString(String progress);
    public void iExecute(boolean firstRun);


    //from App
    public void msg(String message);

    //for App
    public void setUpDisplay();
    public void init();
    public void clearRuns();
    public void adjustSensitivity();
    public void setWorker(MyDiskWorker worker);

    //from DiskWorker
//    public void process(List<DiskMark> markList);
//    public void done();
}
