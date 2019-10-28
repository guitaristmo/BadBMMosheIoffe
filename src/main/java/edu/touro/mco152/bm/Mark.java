package edu.touro.mco152.bm;

import java.text.DecimalFormat;

/**
 * This represents a mark
 * This is just used to pass around diskMark in this setup
 * To extend functionality to benchmark other devices, you would write
 * your own 'mark' and then in your implementation of GuiInterface, decide
 * how to publish these marks
 * Because the Gui is hard coded to use data in DiskMark, I couldn't actually
 * have MainFrame use Mark instead of DiskMark
 */
public abstract class Mark
{
    static DecimalFormat df = new DecimalFormat("###.###");

    public enum MarkType { READ,WRITE; }
}
