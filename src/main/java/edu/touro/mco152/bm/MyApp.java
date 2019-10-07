
package edu.touro.mco152.bm;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker.StateValue;
import edu.touro.mco152.bm.ui.MySelectFrame;
import edu.touro.mco152.bm.persist.DiskRun;
//import edu.touro.mco152.bm.ui.Gui;


/**
 * Primary class for global variables.
 */
public class MyApp {

    public GuiInterface display;

    public static final String APP_CACHE_DIR = System.getProperty("user.home") + File.separator + ".jDiskMark";
    public static final String PROPERTIESFILE = "jdm.properties";
    public static final String DATADIRNAME = "jDiskMarkData";
    public static final int MEGABYTE = 1024 * 1024;
    public static final int KILOBYTE = 1024;
    public static final int IDLE_STATE = 0;
    public static final int DISK_TEST_STATE = 1;

    public enum State {IDLE_STATE, DISK_TEST_STATE};
    public State state = State.IDLE_STATE;

    public Properties p;
    public File locationDir = null;
    public File dataDir = null;
    public File testFile = null;

    // options
    public boolean multiFile = true;
    public boolean autoRemoveData = false;
    public boolean autoReset = true;
    public boolean showMaxMin = true;
    public boolean writeSyncEnable = true;

    // run configuration
    public boolean readTest = false;
    public boolean writeTest = true;
    public DiskRun.BlockSequence blockSequence = DiskRun.BlockSequence.SEQUENTIAL;
    public int numOfMarks = 25;      // desired number of marks
    public int numOfBlocks = 32;     // desired number of blocks
    public int blockSizeKb = 512;    // size of a block in KBs

    public MyDiskWorker worker;
    public int nextMarkNumber = 1;   // number of the next mark
    public double wMax = -1, wMin = -1, wAvg = -1;
    public double rMax = -1, rMin = -1, rAvg = -1;



    public MyApp()
    {
        display = new GuiImplemented(this);
        display.setUpDisplay();
        /* Create and display the form */
        //java.awt.EventQueue.invokeLater(App::init);
        //java.awt.EventQueue.invokeLater(this::init);
        init();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        new MyApp();
    }

    /**
     * Get the version from the build properties. Defaults to 0.0 if not found.
     * @return
     */
    public String getVersion() {
        Properties bp = new Properties();
        String version = "0.0";
        try {
            bp.load(new FileInputStream("build.properties"));
            version = bp.getProperty("version");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MyApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MyApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return version;
    }

    /**
     * Initialize the GUI Application.
     */
    public void init() {
        p = new Properties();
        loadConfig();
        System.out.println(this.getConfigString());

        // configure the embedded DB in .jDiskMark
        System.setProperty("derby.system.home", APP_CACHE_DIR);

        //initialize gui
        display.init();

        loadSavedRuns();
        // save configuration on exit...
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {saveConfig(); }
        });
    }

    public void loadConfig() {
        File pFile = new File(PROPERTIESFILE);
        if (!pFile.exists()) { return; }
        try {
            InputStream is = new FileInputStream(pFile);
            p.load(is);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MyApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MyApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        String value;
        value = p.getProperty("locationDir", System.getProperty("user.home"));
        locationDir = new File(value);
        value = p.getProperty("multiFile", String.valueOf(multiFile));
        multiFile = Boolean.valueOf(value);
        value = p.getProperty("autoRemoveData", String.valueOf(autoRemoveData));
        autoRemoveData = Boolean.valueOf(value);
        value = p.getProperty("autoReset", String.valueOf(autoReset));
        autoReset = Boolean.valueOf(value);
        value = p.getProperty("blockSequence", String.valueOf(blockSequence));
        blockSequence = DiskRun.BlockSequence.valueOf(value);
        value = p.getProperty("showMaxMin", String.valueOf(showMaxMin));
        showMaxMin = Boolean.valueOf(value);
        value = p.getProperty("numOfFiles", String.valueOf(numOfMarks));
        numOfMarks = Integer.valueOf(value);
        value = p.getProperty("numOfBlocks", String.valueOf(numOfBlocks));
        numOfBlocks = Integer.valueOf(value);
        value = p.getProperty("blockSizeKb", String.valueOf(blockSizeKb));
        blockSizeKb = Integer.valueOf(value);
        value = p.getProperty("writeTest", String.valueOf(writeTest));
        writeTest = Boolean.valueOf(value);
        value = p.getProperty("readTest", String.valueOf(readTest));
        readTest = Boolean.valueOf(value);
        value = p.getProperty("writeSyncEnable", String.valueOf(writeSyncEnable));
        writeSyncEnable = Boolean.valueOf(value);
    }

    public void saveConfig() {
        p.setProperty("locationDir", this.locationDir.getAbsolutePath());
        p.setProperty("multiFile", String.valueOf(multiFile));
        p.setProperty("autoRemoveData", String.valueOf(autoRemoveData));
        p.setProperty("autoReset", String.valueOf(autoReset));
        p.setProperty("blockSequence", String.valueOf(blockSequence));
        p.setProperty("showMaxMin", String.valueOf(showMaxMin));
        p.setProperty("numOfFiles", String.valueOf(numOfMarks));
        p.setProperty("numOfBlocks", String.valueOf(numOfBlocks));
        p.setProperty("blockSizeKb", String.valueOf(blockSizeKb));
        p.setProperty("writeTest", String.valueOf(writeTest));
        p.setProperty("readTest", String.valueOf(readTest));
        p.setProperty("writeSyncEnable", String.valueOf(writeSyncEnable));

        try {
            OutputStream out = new FileOutputStream(new File(PROPERTIESFILE));
            p.store(out, "jDiskMark Properties File");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MySelectFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch  (IOException ex) {
            Logger.getLogger(MySelectFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getConfigString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Config for Java Disk Mark ").append(getVersion()).append('\n');
        sb.append("readTest: ").append(readTest).append('\n');
        sb.append("writeTest: ").append(writeTest).append('\n');
        sb.append("locationDir: ").append(locationDir).append('\n');
        sb.append("multiFile: ").append(multiFile).append('\n');
        sb.append("autoRemoveData: ").append(autoRemoveData).append('\n');
        sb.append("autoReset: ").append(autoReset).append('\n');
        sb.append("blockSequence: ").append(blockSequence).append('\n');
        sb.append("showMaxMin: ").append(showMaxMin).append('\n');
        sb.append("numOfFiles: ").append(numOfMarks).append('\n');
        sb.append("numOfBlocks: ").append(numOfBlocks).append('\n');
        sb.append("blockSizeKb: ").append(blockSizeKb).append('\n');
        return sb.toString();
    }

    public void loadSavedRuns() {
        display.clearRuns();

        // populate run table with saved runs from db
        System.out.println("loading stored run data");
        System.out.println("XXXX Java version is "  + System.getProperty("java.version"));

        DiskRun.findAll().stream().forEach((DiskRun run) -> {
            display.addRun(run);
        });
    }

    public void clearSavedRuns() {
        DiskRun.deleteAll();

        loadSavedRuns();
    }

    public void cancelBenchmark() {
        if (worker == null) {
            display.msg("worker is null abort...");
            return;
        }
        display.iCancel(true);
    }

    public void startBenchmark() {

        //1. check that there isn't already a worker in progress
        if (state == State.DISK_TEST_STATE) {
            //if (!worker.isCancelled() && !worker.isDone()) {
            display.msg("Test in progress, aborting...");
            System.out.println("AppInstance: Other test still active");
            return;
            //}
        }

        //2. check can write to location
        if (locationDir.canWrite() == false) {
            display.msg("Selected directory can not be written to... aborting");
            return;
        }

        //3. update state
        state = State.DISK_TEST_STATE;
        System.out.println("AppInstance: started new benchmark");

        display.adjustSensitivity();

        //4. create data dir reference
        dataDir = new File (locationDir.getAbsolutePath()+File.separator+DATADIRNAME);

        //5. remove existing test data if exist
        if (this.autoRemoveData && dataDir.exists()) {
            if (dataDir.delete()) {
                display.msg("removed existing data dir");
            } else {
                display.msg("unable to remove existing data dir");
            }
        }

        //6. create data dir if not already present
        if (dataDir.exists() == false) { dataDir.mkdirs(); }

        //7. start disk worker thread

        //worker = new DiskWorker();
        worker = new MyDiskWorker(this, display);
        display.setWorker(worker);


        display.iAddPropertyChangeListener((final PropertyChangeEvent event) ->
        {
            switch (event.getPropertyName()) {
                case "progress":
                    int value = (Integer)event.getNewValue();
                    display.iSetProgress(value);
                    long kbProcessed = (value) * this.targetTxSizeKb() / 100;
                    display.iSetProgressBarString(String.valueOf(kbProcessed)+" / "+String.valueOf(this.targetTxSizeKb()));
                    break;
                case "state":
                    switch ((StateValue) event.getNewValue()) {
                        case STARTED:
                            display.iSetProgressBarString("0 / "+String.valueOf(this.targetTxSizeKb()));
                            break;
                        case DONE:
                            break;
                    } // end inner switch
                    break;
            }
        });
        display.iExecute();
    }

    public long targetMarkSizeKb() {
        return blockSizeKb * numOfBlocks;
    }

    public long targetTxSizeKb() {
        return blockSizeKb * numOfBlocks * numOfMarks;
    }

    public void updateMetrics(DiskMark mark) {
        if (mark.type==DiskMark.MarkType.WRITE) {
            if (wMax==-1 || wMax < mark.getBwMbSec()) {
                wMax = mark.getBwMbSec();
            }
            if (wMin==-1 || wMin > mark.getBwMbSec()) {
                wMin = mark.getBwMbSec();
            }
            if (wAvg==-1) {
                wAvg = mark.getBwMbSec();
            } else {
                int n = mark.getMarkNum();
                wAvg = (((double)(n-1)*wAvg)+mark.getBwMbSec())/(double)n;
            }
            mark.setCumAvg(wAvg);
            mark.setCumMax(wMax);
            mark.setCumMin(wMin);
        } else {
            if (rMax==-1 || rMax < mark.getBwMbSec()) {
                rMax = mark.getBwMbSec();
            }
            if (rMin==-1 || rMin > mark.getBwMbSec()) {
                rMin = mark.getBwMbSec();
            }
            if (rAvg==-1) {
                rAvg = mark.getBwMbSec();
            } else {
                int n = mark.getMarkNum();
                rAvg = (((double)(n-1)*rAvg)+mark.getBwMbSec())/(double)n;
            }
            mark.setCumAvg(rAvg);
            mark.setCumMax(rMax);
            mark.setCumMin(rMin);
        }
    }

    public void resetSequence() {
        nextMarkNumber = 1;
    }

    public void resetTestData() {
        nextMarkNumber = 1;
        wAvg = -1;
        wMax = -1;
        wMin = -1;
        rAvg = -1;
        rMax = -1;
        rMin = -1;
    }
}
