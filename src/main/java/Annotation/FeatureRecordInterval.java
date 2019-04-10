package Annotation;

import Basic.GTF.FeatureRecord;
import htsjdk.tribble.index.interval.Interval;

public class FeatureRecordInterval extends Interval {
    private FeatureRecord featureRec;

    public FeatureRecordInterval(FeatureRecord feaureRec) {
        super(feaureRec.getStart(), feaureRec.getEnd());
        this.featureRec = feaureRec;
    }

    public FeatureRecord getFeatureRec() {
        return featureRec;
    }
}
