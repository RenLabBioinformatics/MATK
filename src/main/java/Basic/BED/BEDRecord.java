package Basic.BED;

import java.util.LinkedList;

/**
 * Created by Ben on 2018/4/25.
 */
public class BEDRecord {
    private String chrName;
    private int chrStart;
    private int chrEnd;
    private String name;
    private double score;
    private int strand;
    private int thickStart;
    private int thickEnd;
    private RGBItem rgbItem;
    private LinkedList<BLOCKRecord> blockRecList;

    public BEDRecord(String chrName, int chrStart, int chrEnd) {
        this.chrName = chrName;
        this.chrStart = chrStart;
        this.chrEnd = chrEnd;
    }

    public BEDRecord(String chrName, int chrStart, int chrEnd, int strand) {
        this.chrName = chrName;
        this.chrStart = chrStart;
        this.chrEnd = chrEnd;
        this.strand = strand;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getStrand() {
        return strand;
    }

    public void setStrand(int strand) {
        this.strand = strand;
    }

    public int getThickStart() {
        return thickStart;
    }

    public void setThickStart(int thickStart) {
        this.thickStart = thickStart;
    }

    public int getThickEnd() {
        return thickEnd;
    }

    public void setThickEnd(int thickEnd) {
        this.thickEnd = thickEnd;
    }

    public RGBItem getRgbItem() {
        return rgbItem;
    }

    public void setRgbItem(RGBItem rgbItem) {
        this.rgbItem = rgbItem;
    }

    public LinkedList<BLOCKRecord> getBlockRecList() {
        return blockRecList;
    }

    public void setBlockRecList(LinkedList<BLOCKRecord> blockRecList) {
        this.blockRecList = blockRecList;
    }
}
