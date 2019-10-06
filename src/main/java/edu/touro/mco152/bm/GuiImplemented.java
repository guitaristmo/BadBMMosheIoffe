package edu.touro.mco152.bm;

import edu.touro.mco152.bm.persist.DiskRun;
//import edu.touro.mco152.bm.ui.Gui;
import edu.touro.mco152.bm.ui.MyMainFrame;
import edu.touro.mco152.bm.ui.MyRunPanel;
import edu.touro.mco152.bm.ui.MySelectFrame;
import javafx.beans.binding.ObjectExpression;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.List;
//import static edu.touro.mco152.bm.App.dataDir;
//import static edu.touro.mco152.bm.App.worker;

public class GuiImplemented extends SwingWorker <Boolean, DiskMark> implements GuiInterface
{
    public ChartPanel chartPanel = null;
    public MyMainFrame mainFrame = null;
    public MySelectFrame selFrame = null;
    public XYSeries wSeries = null;
    public XYSeries wAvgSeries = null;
    public XYSeries wMaxSeries = null;
    public XYSeries wMinSeries = null;
    public XYSeries rSeries = null;
    public XYSeries rAvgSeries = null;
    public XYSeries rMaxSeries = null;
    public XYSeries rMinSeries = null;
    public JFreeChart chart = null;
    public JProgressBar progressBar = null;
    public MyRunPanel runPanel = null;
    public MyDiskWorker worker = null;
    public MyApp mainApp = null;


    public GuiImplemented(MyApp mainApp)
    {
        super();
        this.mainApp = mainApp;
    }

    public ChartPanel createChartPanel() {

        wSeries = new XYSeries("Writes");
        wAvgSeries = new XYSeries("Write Avg");
        wMaxSeries = new XYSeries("Write Max");
        wMinSeries = new XYSeries("Write Min");
        rSeries = new XYSeries("Reads");
        rAvgSeries = new XYSeries("Read Avg");
        rMaxSeries = new XYSeries("Read Max");
        rMinSeries = new XYSeries("Read Min");

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(wSeries);
        dataset.addSeries(wAvgSeries);
        dataset.addSeries(wMaxSeries);
        dataset.addSeries(wMinSeries);
        dataset.addSeries(rSeries);
        dataset.addSeries(rAvgSeries);
        dataset.addSeries(rMaxSeries);
        dataset.addSeries(rMinSeries);

        chart = ChartFactory.createXYLineChart(
                "XY Chart", // Title
                null, // x-axis Label
                "Bandwidth MB/s", // y-axis Label
                dataset, // Dataset
                PlotOrientation.VERTICAL, // Plot Orientation
                true,// Show Legend
                true, // Use tooltips
                false // Configure chart to generate URLs?
        );
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.DARK_GRAY);
        ((NumberAxis) plot.getRangeAxis()).setAutoRangeIncludesZero(false);
        NumberAxis range = (NumberAxis) plot.getDomainAxis();
        range.setNumberFormatOverride(NumberFormat.getNumberInstance());
        chart.getTitle().setVisible(false);
        chartPanel = new ChartPanel(chart) {
            // Only way to set the size of chart panel
            // ref: http://www.jfree.org/phpBB2/viewtopic.php?p=75516
            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(500, 325);
            }
        };

        plot.getRenderer().setSeriesPaint(0, Color.YELLOW);
        plot.getRenderer().setSeriesPaint(1, Color.WHITE);
        plot.getRenderer().setSeriesPaint(2, Color.GREEN);
        plot.getRenderer().setSeriesPaint(3, Color.RED);
        plot.getRenderer().setSeriesPaint(4, Color.LIGHT_GRAY);
        plot.getRenderer().setSeriesPaint(5, Color.ORANGE);
        plot.getRenderer().setSeriesPaint(6, Color.GREEN);
        plot.getRenderer().setSeriesPaint(7, Color.RED);
        updateLegend();
        return chartPanel;
    }

    private void addWriteMark(DiskMark mark) {
        wSeries.add(mark.getMarkNum(), mark.getBwMbSec());
        wAvgSeries.add(mark.getMarkNum(), mark.getCumAvg());
        if (mainApp.showMaxMin) {
            wMaxSeries.add(mark.getMarkNum(), mark.getCumMax());
            wMinSeries.add(mark.getMarkNum(), mark.getCumMin());
        }
        mainFrame.refreshWriteMetrics();
        System.out.println(mark.toString());
    }

    private void addReadMark(DiskMark mark) {
        rSeries.add(mark.getMarkNum(), mark.getBwMbSec());
        rAvgSeries.add(mark.getMarkNum(), mark.getCumAvg());
        if (mainApp.showMaxMin) {
            rMaxSeries.add(mark.getMarkNum(), mark.getCumMax());
            rMinSeries.add(mark.getMarkNum(), mark.getCumMin());
        }
        mainFrame.refreshReadMetrics();
        System.out.println(mark.toString());
    }

    @Override
    public void resetTestData() {
        wSeries.clear();
        rSeries.clear();
        wAvgSeries.clear();
        rAvgSeries.clear();
        wMaxSeries.clear();
        rMaxSeries.clear();
        wMinSeries.clear();
        rMinSeries.clear();
        progressBar.setValue(0);
        mainFrame.refreshReadMetrics();
        mainFrame.refreshWriteMetrics();
    }

    @Override
    public void setTitle(String title)
    {
        chartPanel.getChart().getTitle().setVisible(true);
        chartPanel.getChart().getTitle().setText(title);
    }

    @Override
    public void addRun(DiskRun run)
    {
        runPanel.addRun(run);
    }

    @Override
    public void showFileRenamingMessage()
    {
        JOptionPane.showMessageDialog(mainFrame,
                "For valid READ measurements please clear the disk cache by\n" +
                        "using the included RAMMap.exe or flushmem.exe utilities.\n" +
                        "Removable drives can be disconnected and reconnected.\n" +
                        "For system drives use the WRITE and READ operations \n" +
                        "independantly by doing a cold reboot after the WRITE",
                "Clear Disk Cache Now",JOptionPane.PLAIN_MESSAGE);
    }

    @Override
    public boolean iIsCancelled() { return isCancelled(); }

    @Override
    public void clearRuns()
    {
        runPanel.clearTable();
    }

    @Override
    public void updateLegend() {
        chart.getXYPlot().getRenderer().setSeriesVisibleInLegend(0, mainApp.writeTest);
        chart.getXYPlot().getRenderer().setSeriesVisibleInLegend(1, mainApp.writeTest);
        chart.getXYPlot().getRenderer().setSeriesVisibleInLegend(2, mainApp.writeTest&&mainApp.showMaxMin);
        chart.getXYPlot().getRenderer().setSeriesVisibleInLegend(3, mainApp.writeTest&&mainApp.showMaxMin);

        chart.getXYPlot().getRenderer().setSeriesVisibleInLegend(4, mainApp.readTest);
        chart.getXYPlot().getRenderer().setSeriesVisibleInLegend(5, mainApp.readTest);
        chart.getXYPlot().getRenderer().setSeriesVisibleInLegend(6, mainApp.readTest&&mainApp.showMaxMin);
        chart.getXYPlot().getRenderer().setSeriesVisibleInLegend(7, mainApp.readTest&&mainApp.showMaxMin);
    }

    @Override
    public void iSetProgressBarString(String progress)
    {
        progressBar.setString(progress);
    }

    @Override
    public void iExecute(boolean firstRun) {
        if(firstRun)
            execute();
        else
        {
            try
            {
                worker.newDoInBackground();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("display.iExecute: about to call SwingWorker.execute");

    }

    @Override
    public void iPublish(Object... marks)
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
    public void iCancel(boolean cancel) { cancel(cancel); }

    @Override
    public void iAddPropertyChangeListener(PropertyChangeListener listener) { addPropertyChangeListener(listener); }

    @Override
    public void iSetProgress(int progress)
    {
        setProgress(progress);
    }

    @Override
    public void msg(String message) {mainFrame.msg(message); }

    @Override
    public void setUpDisplay()
    {
        /* Set the Nimbus look and feel */
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
            /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
             * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
             */
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
                java.util.logging.Logger.getLogger(MyMainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
            //</editor-fold>
        }

    }

    @Override
    public void init()
    {
        mainFrame = new MyMainFrame(mainApp, this);
        selFrame = new MySelectFrame(mainApp, this, mainFrame);

        mainFrame.refreshConfig();
        mainFrame.setLocationRelativeTo(null);
        progressBar = mainFrame.getProgressBar();

        mainFrame.setVisible(true);
    }

    @Override
    public void adjustSensitivity()
    {
        mainFrame.adjustSensitivity();
    }

    @Override
    public void setWorker(MyDiskWorker worker){this.worker = worker;}





    @Override
    protected Boolean doInBackground() throws Exception {
        System.out.println("display.doInBackground: about to call worker.newDoInBackground");
        return worker.newDoInBackground();
    }

    @Override
    public void process(List<DiskMark> markList) {
        markList.stream().forEach( (m) -> {
            if (m.type==DiskMark.MarkType.WRITE) {
                addWriteMark(m);
            } else {
                addReadMark(m);
            }
        });
    }

    @Override
    public void done() {
        if (mainApp.autoRemoveData) {
            Util.deleteDirectory(mainApp.dataDir);
        }
        mainApp.state = MyApp.State.IDLE_STATE;
        mainFrame.adjustSensitivity();
    }
}


















