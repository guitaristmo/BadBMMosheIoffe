package edu.touro.mco152.bm;

import edu.touro.mco152.bm.persist.DiskRun;

import java.io.File;

public class RunConfigSetting
{
    public DiskRun.BlockSequence blockSequence = DiskRun.BlockSequence.SEQUENTIAL;
    public int numOfMarks = 25;      // desired number of marks
    public int numOfBlocks = 32;     // desired number of blocks
    public int blockSizeKb = 512;    // size of a block in KBs
    public boolean readTest = false;
    public boolean writeTest = true;
    public File locationDir = null;
    public File dataDir = null;
    public File testFile = null;

    public boolean multiFile = true;
    public boolean autoRemoveData = false;
    public boolean autoReset = true;
    public boolean showMaxMin = true;
    public boolean writeSyncEnable = true;

    public long targetMarkSizeKb() { return blockSizeKb * numOfBlocks; }

    public long targetTxSizeKb() { return blockSizeKb * numOfBlocks * numOfMarks; }

}