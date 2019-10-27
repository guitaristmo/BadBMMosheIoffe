
package edu.touro.mco152.bm;

import java.text.DecimalFormat;

/**
 *This class holds information about a single iteration of a disk benchmark
 */
public class DiskMark implements Mark
{
    static DecimalFormat df = new DecimalFormat("###.###");
    
    public enum MarkType { READ,WRITE; }


	public double wMax = -1, wMin = -1, wAvg = -1;
	public double rMax = -1, rMin = -1, rAvg = -1;

    DiskMark(MarkType type) { this.type=type; }
    
    MarkType type;
    private int markNum = 0;       // x-axis
    private double bwMbSec = 0;    // y-axis
    private double cumMin = 0;
    private double cumMax = 0;
    private double cumAvg = 0;
    
    @Override
    public String toString() {
        return "Mark("+type+"): "+getMarkNum()+" bwMbSec: "+getBwMbSecAsString()+" avg: "+getAvgAsString();
    }
    
    String getBwMbSecAsString() { return df.format(getBwMbSec()); }
    
    String getMinAsString() {
        return df.format(getCumMin());
    }
    
    String getMaxAsString() {
        return df.format(getCumMax());
    }
    
    String getAvgAsString() {
        return df.format(getCumAvg());
    }

	public int getMarkNum() {
		return markNum;
	}

	public void setMarkNum(int markNum) {
		this.markNum = markNum;
	}

	public double getBwMbSec() { return bwMbSec; }

	public void setBwMbSec(double bwMbSec) {
		this.bwMbSec = bwMbSec;
	}

	public double getCumAvg() {
		return cumAvg;
	}

	public void setCumAvg(double cumAvg) {
		this.cumAvg = cumAvg;
	}

	public double getCumMin() { return cumMin; }

	public void setCumMin(double cumMin) {
		this.cumMin = cumMin;
	}

	public double getCumMax() { return cumMax; }

	public void setCumMax(double cumMax) {
		this.cumMax = cumMax;
	}

	public void updateMetrics()
	{
		if (type==MarkType.WRITE) {
			if (wMax==-1 || wMax < bwMbSec) {
				wMax =  bwMbSec;
			}
			if (wMin==-1 || wMin > bwMbSec) {
				wMin = bwMbSec;
			}
			if (wAvg==-1) {
				wAvg = bwMbSec;
			} else {
				int n = getMarkNum();
				wAvg = (((double)(n-1)*wAvg)+bwMbSec)/(double)n;
			}
			cumAvg = wAvg;
			cumMax = wMax;
			cumMin = wMin;
		} else {
			if (rMax==-1 || rMax < bwMbSec) {
				rMax = bwMbSec;
			}
			if (rMin==-1 || rMin > bwMbSec) {
				rMin = bwMbSec;
			}
			if (rAvg==-1) {
				rAvg = bwMbSec;
			} else {
				int n = markNum;
				rAvg = (((double)(n-1)*rAvg)+bwMbSec)/(double)n;
			}
			cumAvg = rAvg;
			cumMax = rMax;
			cumMin = rMin;
		}
	}

	public void resetTestData() {
		wAvg = -1;
		wMax = -1;
		wMin = -1;
		rAvg = -1;
		rMax = -1;
		rMin = -1;
	}
}
