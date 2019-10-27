package edu.touro.mco152.bm;

import javax.swing.*;
import java.util.List;

/**
 * Class that uses SwingWorker to run diskWorker through swingWorker
 */
public class SwingWorkerImplemented extends SwingWorker <Boolean, DiskMark>
{
    private MyDiskWorker worker = null;
    private MyApp mainApp = null;
    private GuiImplemented mainGui;

    public SwingWorkerImplemented(GuiImplemented mainGui, MyApp mainApp)
    {
        super();
        this.mainGui = mainGui;
        this.mainApp = mainApp;
    }

    public void setWorker(MyDiskWorker worker){this.worker = worker;}

    void uSetProgress(int progress){setProgress(progress);}

    void uPublish(Object... marks)
    {
        //I would use the following line but it wasn't casting properly
        //publish((DiskMark[]) marks);

        //so this is my version and it works
        DiskMark[] mark = new DiskMark[marks.length];
        for (int index = 0; index < marks.length; index++)
            mark[index] = (DiskMark) marks[index];
        publish(mark);
    }

    @Override
    protected Boolean doInBackground() throws Exception { return worker.runBenchmark(); }

    @Override
    public void process(List<DiskMark> markList) {
        markList.stream().forEach( (m) -> {
            if (m.type==DiskMark.MarkType.WRITE) {
                mainGui.addWriteMark(m);
            } else {
                mainGui.addReadMark(m);
            }
        });
    }

    @Override
    public void done() {
        System.out.println("display.done was called");
        if (mainApp.runConfigs.autoRemoveData) {
            Util.deleteDirectory(mainApp.runConfigs.dataDir);
        }
        mainApp.state = MyApp.State.IDLE_STATE;
        mainGui.adjustSensitivity();
    }
}


















