package BAMProcess;

/**
 * Created by Ben on 2018/4/23.
 */
public class LocationRecord {
    private String chrName;
    private int index;

    public LocationRecord(String chrName, int index) {
        this.chrName = chrName;
        this.index = index;
    }

    public String getChrName() {
        return chrName;
    }

    public void setChrName(String chrName) {
        this.chrName = chrName;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
