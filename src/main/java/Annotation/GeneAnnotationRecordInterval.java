package Annotation;

import htsjdk.tribble.index.interval.Interval;

public class GeneAnnotationRecordInterval extends Interval {
    private GeneAnnotationRecord geneAnnoRec;

    public GeneAnnotationRecordInterval(GeneAnnotationRecord geneAnnoRec) {
        super(geneAnnoRec.getGeneRec().getStart(), geneAnnoRec.getGeneRec().getEnd());
        this.geneAnnoRec = geneAnnoRec;
    }

    public GeneAnnotationRecord getGeneAnnoRec() {
        return geneAnnoRec;
    }
}
