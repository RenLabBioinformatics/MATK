package PeakCalling.PeakDistribution;

import Basic.GTF.GeneRecord;
import htsjdk.tribble.index.interval.Interval;

public class GeneIntervalRecord extends Interval {
    private GeneRecord geneRecord;

    public GeneIntervalRecord(GeneRecord geneRecord) {
        super(geneRecord.getStart(), geneRecord.getEnd());
        this.geneRecord = geneRecord;
    }

    public GeneRecord getGeneRecord() {
        return geneRecord;
    }
}
