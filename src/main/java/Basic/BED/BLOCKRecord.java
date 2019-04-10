package Basic.BED;

/**
 * Created by Ben on 2018/4/25.
 */
public class BLOCKRecord {
    private String chrName;
    private int chrStart;
    private int chrEnd;

    public BLOCKRecord(String chrName, int chrStart, int chrEnd) {
        this.chrName = chrName;
        this.chrStart = chrStart;
        this.chrEnd = chrEnd;
    }

    public String getChrName() {
        return chrName;
    }

    public void setChrName(String chrName) {
        this.chrName = chrName;
    }

    public int getChrStart() {
        return chrStart;
    }

    public void setChrStart(int chrStart) {
        this.chrStart = chrStart;
    }

    public int getChrEnd() {
        return chrEnd;
    }

    public void setChrEnd(int chrEnd) {
        this.chrEnd = chrEnd;
    }
}
