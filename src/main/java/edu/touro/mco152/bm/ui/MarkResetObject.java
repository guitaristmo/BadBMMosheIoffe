package edu.touro.mco152.bm.ui;

import edu.touro.mco152.bm.DiskMark;

public class MarkResetObject
{
    public DiskMark writeMark;
    public DiskMark readMark;

    public MarkResetObject(DiskMark writeMark, DiskMark readMark)
    {
        this.writeMark = writeMark;
        this.readMark = readMark;
    }
}
