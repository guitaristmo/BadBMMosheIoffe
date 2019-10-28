
package edu.touro.mco152.bm;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker.StateValue;

import edu.touro.mco152.bm.persist.PropertiesManager;
import edu.touro.mco152.bm.persist.DiskRun;
import edu.touro.mco152.bm.ui.MarkResetObject;

/**
 * Primary class for running the Benchmark
 * This class does the program setup - connecting the UI, (which is just an interface and different ones can be used), the config settings,
 * and running diskworker.
 *
 * I will explain here most of the benefits of my program restructuring:
 * Part 1: Remove the Swing and Gui from the benchmarking process.
 * DiskWorker and App have been restructured to have no Gui or Swing references, which
 * benefits us in 2 ways:
 * SRP compliant - less reason to change - they are only responsible for running the benchmark (I will explain
 * each in more detail later), and not for displaying it or choosing in what threads to run the benchmark.
 * DIP - The benchmarking software is no longer reliant on Swing or on a GUI. Any implementation of the GuiInterface will
 * suffice for App and Diskworker to run. Now, you can just write a new class that implements GuiInterface as a console app, and the
 * program will still run fine.
 *
 * Part 2: Remove Disk from Diskworker.
 * Diskworker is no longer limited to benchmarking disks: I will explain in greater detail in Diskworker
 *
 */
public class MyApp {

    private GuiInterface display;
    public PropertiesManager propManager;
    public MyDiskWorker worker;
    public RunConfigSetting runConfigs;

    public static final String APP_CACHE_DIR = System.getProperty("user.home") + File.separator + ".jDiskMark";
    public static final String DATADIRNAME = "jDiskMarkData";
    public static final int MEGABYTE = 1024 * 1024;
    public static final int KILOBYTE = 1024;
    public enum State {IDLE_STATE, DISK_TEST_STATE};
    public State state = State.IDLE_STATE;

    public MyApp()
    {
        runConfigs = new RunConfigSetting();
        propManager = new PropertiesManager(this);
        propManager.loadConfig();
        System.out.println(this.getConfigString());

        // configure the embedded DB in .jDiskMark
        System.setProperty("derby.system.home", APP_CACHE_DIR);

        //initialize gui
        display = new GuiImplemented(this);
        display.init();
        loadSavedRuns();

        // save configuration on exit...
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {propManager.saveConfig(); }
        });
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

    public String getConfigString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Config for Java Disk Mark ").append(getVersion()).append('\n');
        sb.append("readTest: ").append(runConfigs.readTest).append('\n');
        sb.append("writeTest: ").append(runConfigs.writeTest).append('\n');
        sb.append("locationDir: ").append(runConfigs.locationDir).append('\n');
        sb.append("multiFile: ").append(runConfigs.multiFile).append('\n');
        sb.append("autoRemoveData: ").append(runConfigs.autoRemoveData).append('\n');
        sb.append("autoReset: ").append(runConfigs.autoReset).append('\n');
        sb.append("blockSequence: ").append(runConfigs.blockSequence).append('\n');
        sb.append("showMaxMin: ").append(runConfigs.showMaxMin).append('\n');
        sb.append("numOfFiles: ").append(runConfigs.numOfMarks).append('\n');
        sb.append("numOfBlocks: ").append(runConfigs.numOfBlocks).append('\n');
        sb.append("blockSizeKb: ").append(runConfigs.blockSizeKb).append('\n');
        return sb.toString();
    }

    public void loadSavedRuns() {
        display.clearRuns();

        // populate run table with saved runs from db
        System.out.println("loading stored run data");
        System.out.println("XXXX Java version is "  + System.getProperty("java.version"));

//        DiskRun.findAll().stream().forEach((DiskRun run) -> {
//            display.addRun(run);
//        });
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
            return;
            //}
        }

        //2. check can write to location
        if (runConfigs.locationDir.canWrite() == false) {
            display.msg("Selected directory can not be written to... aborting");
            return;
        }

        //3. update state
        state = State.DISK_TEST_STATE;
        display.adjustSensitivity();

        //4. create data dir reference
        runConfigs.dataDir = new File (runConfigs.locationDir.getAbsolutePath()+File.separator+DATADIRNAME);

        //5. remove existing test data if exist
        if (runConfigs.autoRemoveData && runConfigs.dataDir.exists()) {
            if (runConfigs.dataDir.delete()) {
                display.msg("removed existing data dir");
            } else {
                display.msg("unable to remove existing data dir");
            }
        }

        //6. create data dir if not already present
        if (runConfigs.dataDir.exists() == false) { runConfigs.dataDir.mkdirs(); }

        //7. start disk worker thread
        if (worker == null)
        {
            worker = new MyDiskWorker(runConfigs, display);
            display.setWorker(worker);
        }

        display.iAddPropertyChangeListener((final PropertyChangeEvent event) ->
        {
            switch (event.getPropertyName()) {
                case "progress":
                    int value = (Integer)event.getNewValue();
                    display.iSetProgress(value);
                    long kbProcessed = (value) * this.runConfigs.targetTxSizeKb() / 100;
                    display.iSetProgressBarString(String.valueOf(kbProcessed)+" / "+String.valueOf(this.runConfigs.targetTxSizeKb()));
                    break;
                case "state":
                    switch ((StateValue) event.getNewValue()) {
                        case STARTED:
                            display.iSetProgressBarString("0 / "+String.valueOf(this.runConfigs.targetTxSizeKb()));
                            break;
                        case DONE:
                            break;
                    } // end inner switch
                    break;
            }
        });
        display.iExecute();
    }

    public void resetSequence(){worker.resetSequence();}

    public void resetTestData()
    {
        MarkResetObject marks = worker.resetTestData();
        display.resetTestData(marks);
    }
}
