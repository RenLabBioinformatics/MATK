package BAMProcess;

/**
 * Created by Ben on 2016/9/3.
 */
public class BAMBin
{
    private double readCount;
    private int binIndex;

    public double getReadCount() {
        return readCount;
    }

    public void setReadCount(double readCount) {
        this.readCount = readCount;
    }

    public int getBinIndex() {
        return binIndex;
    }

    public void setBinIndex(int binIndex) {
        this.binIndex = binIndex;
    }

    @Override
    public String toString() {
        return binIndex + "\t" + readCount;
    }
}
