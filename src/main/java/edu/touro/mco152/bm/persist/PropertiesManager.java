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
        app.locationDir = new File(value);
        value = p.getProperty("multiFile", String.valueOf(app.multiFile));
        app.multiFile = Boolean.valueOf(value);
        value = p.getProperty("autoRemoveData", String.valueOf(app.autoRemoveData));
        app.autoRemoveData = Boolean.valueOf(value);
        value = p.getProperty("autoReset", String.valueOf(app.autoReset));
        app.autoReset = Boolean.valueOf(value);
        value = p.getProperty("blockSequence", String.valueOf(app.blockSequence));
        app.blockSequence = DiskRun.BlockSequence.valueOf(value);
        value = p.getProperty("showMaxMin", String.valueOf(app.showMaxMin));
        app.showMaxMin = Boolean.valueOf(value);
        value = p.getProperty("numOfFiles", String.valueOf(app.numOfMarks));
        app.numOfMarks = Integer.valueOf(value);
        value = p.getProperty("numOfBlocks", String.valueOf(app.numOfBlocks));
        app.numOfBlocks = Integer.valueOf(value);
        value = p.getProperty("blockSizeKb", String.valueOf(app.blockSizeKb));
        app.blockSizeKb = Integer.valueOf(value);
        value = p.getProperty("writeTest", String.valueOf(app.writeTest));
        app.writeTest = Boolean.valueOf(value);
        value = p.getProperty("readTest", String.valueOf(app.readTest));
        app.readTest = Boolean.valueOf(value);
        value = p.getProperty("writeSyncEnable", String.valueOf(app.writeSyncEnable));
        app.writeSyncEnable = Boolean.valueOf(value);
    }

    public void saveConfig() {
        p.setProperty("locationDir", app.locationDir.getAbsolutePath());
        p.setProperty("multiFile", String.valueOf(app.multiFile));
        p.setProperty("autoRemoveData", String.valueOf(app.autoRemoveData));
        p.setProperty("autoReset", String.valueOf(app.autoReset));
        p.setProperty("blockSequence", String.valueOf(app.blockSequence));
        p.setProperty("showMaxMin", String.valueOf(app.showMaxMin));
        p.setProperty("numOfFiles", String.valueOf(app.numOfMarks));
        p.setProperty("numOfBlocks", String.valueOf(app.numOfBlocks));
        p.setProperty("blockSizeKb", String.valueOf(app.blockSizeKb));
        p.setProperty("writeTest", String.valueOf(app.writeTest));
        p.setProperty("readTest", String.valueOf(app.readTest));
        p.setProperty("writeSyncEnable", String.valueOf(app.writeSyncEnable));

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
