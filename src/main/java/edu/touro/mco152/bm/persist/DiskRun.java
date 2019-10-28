
package edu.touro.mco152.bm.persist;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 */
@Entity
@Table(name="DiskRun")
@NamedQueries({
@NamedQuery(name="DiskRun.findAll",
    query="SELECT d FROM DiskRun d")
})
public class DiskRun extends Run implements Serializable {

	private static final long serialVersionUID = 1L;

    static public enum IOMode { READ, WRITE, READ_WRITE; }
    static public enum BlockSequence {SEQUENTIAL, RANDOM; }

    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    
    // configuration
    @Column
    String diskInfo = null;
    @Column
	private
    IOMode ioMode;
    @Column
	private
    BlockSequence blockOrder;
    @Column
	private
    int numMarks = 0;
    @Column
	private
    int numBlocks = 0;
    @Column
	private
    int blockSize = 0;
    @Column
	private
    long txSize = 0;
    @Column
    int totalMarks = 0;
    @Column
	private
    double runMin = 0;
    @Column
	private
    double runMax = 0;
    @Column
	private
    double runAvg = 0;
    
    @Override
    public String toString() {
        return "Run("+getIoMode()+","+getBlockOrder()+"): "+totalMarks+" run avg: "+getRunAvg();
    }
    
    public DiskRun() {
        this.startTime = new Date();
    }
    
    public DiskRun(IOMode type, BlockSequence order) {
        this.startTime = new Date();
        setIoMode(type);
        setBlockOrder(order);
    }
    
    // display friendly methods

    public String getMin() {
        return getRunMin() == -1 ? "- -" : DF.format(getRunMin());
    }
    
    public void setMin(double min) {
        setRunMin(min);
    }
    
    public String getMax() {
        return getRunMax() == -1 ? "- -" : DF.format(getRunMax());
    }
    
    public void setMax(double max) {
        setRunMax(max);
    }
    
    public String getAvg() {
        return getRunAvg() == -1 ? "- -" : DF.format(getRunAvg());
    }
    
    public void SetAvg(double avg) {
        setRunAvg(avg);
    }

    // basic getters and setters
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getDiskInfo() {
        return diskInfo;
    }
    public void setDiskInfo(String info) {
        diskInfo = info;
    }
    
    // utility methods for collection
    
    public static List<DiskRun> findAll() {
        EntityManager em = EM.getEntityManager();
        return em.createNamedQuery("DiskRun.findAll", DiskRun.class).getResultList();
    }
    
    public static int deleteAll() {
        EntityManager em = EM.getEntityManager();
        em.getTransaction().begin();
        int deletedCount = em.createQuery("DELETE FROM DiskRun").executeUpdate();
        em.getTransaction().commit();
        return deletedCount;
    }

	public IOMode getIoMode() {
		return ioMode;
	}

	public void setIoMode(IOMode ioMode) {
		this.ioMode = ioMode;
	}

	public BlockSequence getBlockOrder() {
		return blockOrder;
	}

	public void setBlockOrder(BlockSequence blockOrder) {
		this.blockOrder = blockOrder;
	}

	public int getNumMarks() {
		return numMarks;
	}

	public void setNumMarks(int numMarks) {
		this.numMarks = numMarks;
	}

	public int getNumBlocks() {
		return numBlocks;
	}

	public void setNumBlocks(int numBlocks) {
		this.numBlocks = numBlocks;
	}

	public int getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	public long getTxSize() {
		return txSize;
	}

	public void setTxSize(long txSize) {
		this.txSize = txSize;
	}

	public double getRunMax() {
		return runMax;
	}

	public void setRunMax(double runMax) {
		this.runMax = runMax;
	}

	public double getRunMin() {
		return runMin;
	}

	public void setRunMin(double runMin) {
		this.runMin = runMin;
	}

	public double getRunAvg() {
		return runAvg;
	}

	public void setRunAvg(double runAvg) {
		this.runAvg = runAvg;
	}
}
