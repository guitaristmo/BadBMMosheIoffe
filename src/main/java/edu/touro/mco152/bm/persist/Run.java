package edu.touro.mco152.bm.persist;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This represents the results of a benchmark.
 * This interface is used to hold the results of a benchmark
 * which will be displayed by GuiInterface.
 * ISP: Not all benchmarks need to be serializable to be stored. Diskworker has no requirements for them to be serialized.
 * Rather -  this program wants to be serializable, so DiskRun, which extends Run, implements, serializable.
 */
public abstract class Run
{
    @Temporal(TemporalType.TIMESTAMP)
    @Column
    Date startTime;
    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private
    Date endTime = null;

    static final DecimalFormat DF = new DecimalFormat("###.##");
    static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE, MMM d HH:mm:ss");

    public String getStartTimeString() {
        return DATE_FORMAT.format(startTime);
    }

    public String getDuration()
    {
        if (getEndTime() == null) {
            return "unknown";
        }
        long duration = getEndTime().getTime() - startTime.getTime();
        long diffSeconds = duration / 1000 % 60;
        return String.valueOf(diffSeconds) + "s";
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
