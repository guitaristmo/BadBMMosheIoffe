package edu.touro.mco152.bm.persist;

import edu.touro.mco152.bm.MyApp;
import edu.touro.mco152.bm.ui.MySelectFrame;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for managing property storage for App
 * SRP - not apps job for storing properties about the benchmarks
 */
public class PropertiesManager
{
    MyApp app;

    public static final String PROPERTIESFILE = "jdm.properties";
    public Properties p;

    public PropertiesManager(MyApp app)
    {
        this.app = app;
        p = new Properties();
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
        app.runConfigs.locationDir = new File(value);
        value = p.getProperty("multiFile", String.valueOf(app.runConfigs.multiFile));
        app.runConfigs.multiFile = Boolean.valueOf(value);
        value = p.getProperty("autoRemoveData", String.valueOf(app.runConfigs.autoRemoveData));
        app.runConfigs.autoRemoveData = Boolean.valueOf(value);
        value = p.getProperty("autoReset", String.valueOf(app.runConfigs.autoReset));
        app.runConfigs.autoReset = Boolean.valueOf(value);
        value = p.getProperty("blockSequence", String.valueOf(app.runConfigs.blockSequence));
        app.runConfigs.blockSequence = DiskRun.BlockSequence.valueOf(value);
        value = p.getProperty("showMaxMin", String.valueOf(app.runConfigs.showMaxMin));
        app.runConfigs.showMaxMin = Boolean.valueOf(value);
        value = p.getProperty("numOfFiles", String.valueOf(app.runConfigs.numOfMarks));
        app.runConfigs.numOfMarks = Integer.valueOf(value);
        value = p.getProperty("numOfBlocks", String.valueOf(app.runConfigs.numOfBlocks));
        app.runConfigs.numOfBlocks = Integer.valueOf(value);
        value = p.getProperty("blockSizeKb", String.valueOf(app.runConfigs.blockSizeKb));
        app.runConfigs.blockSizeKb = Integer.valueOf(value);
        value = p.getProperty("writeTest", String.valueOf(app.runConfigs.writeTest));
        app.runConfigs.writeTest = Boolean.valueOf(value);
        value = p.getProperty("readTest", String.valueOf(app.runConfigs.readTest));
        app.runConfigs.readTest = Boolean.valueOf(value);
        value = p.getProperty("writeSyncEnable", String.valueOf(app.runConfigs.writeSyncEnable));
        app.runConfigs.writeSyncEnable = Boolean.valueOf(value);
    }

    public void saveConfig() {
        p.setProperty("locationDir", app.runConfigs.locationDir.getAbsolutePath());
        p.setProperty("multiFile", String.valueOf(app.runConfigs.multiFile));
        p.setProperty("autoRemoveData", String.valueOf(app.runConfigs.autoRemoveData));
        p.setProperty("autoReset", String.valueOf(app.runConfigs.autoReset));
        p.setProperty("blockSequence", String.valueOf(app.runConfigs.blockSequence));
        p.setProperty("showMaxMin", String.valueOf(app.runConfigs.showMaxMin));
        p.setProperty("numOfFiles", String.valueOf(app.runConfigs.numOfMarks));
        p.setProperty("numOfBlocks", String.valueOf(app.runConfigs.numOfBlocks));
        p.setProperty("blockSizeKb", String.valueOf(app.runConfigs.blockSizeKb));
        p.setProperty("writeTest", String.valueOf(app.runConfigs.writeTest));
        p.setProperty("readTest", String.valueOf(app.runConfigs.readTest));
        p.setProperty("writeSyncEnable", String.valueOf(app.runConfigs.writeSyncEnable));

        try {
            OutputStream out = new FileOutputStream(new File(PROPERTIESFILE));
            p.store(out, "jDiskMark Properties File");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MySelectFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch  (IOException ex) {
            Logger.getLogger(MySelectFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
