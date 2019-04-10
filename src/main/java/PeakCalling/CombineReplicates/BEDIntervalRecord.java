package PeakCalling.CombineReplicates;


import Basic.BED.BEDRecord;
import htsjdk.tribble.index.interval.Interval;

public class BEDIntervalRecord extends Interval {
    private BEDRecord bedRec;

    public BEDIntervalRecord(BEDRecord bedRec) {
        super(bedRec.getChrStart(), bedRec.getChrEnd());
        this.bedRec = bedRec;
    }

    public BEDRecord getBedRec() {
        return bedRec;
    }
}
