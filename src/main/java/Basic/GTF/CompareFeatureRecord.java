package Basic.GTF;

import java.util.Comparator;

public class CompareFeatureRecord implements Comparator<FeatureRecord> {
    @Override
    public int compare(FeatureRecord featureRec1, FeatureRecord featureRec2) {
        if(featureRec1.getStart() < featureRec2.getStart())
            return -1;
        else if(featureRec1.getStart() > featureRec1.getStart())
            return 1;
        else
            return 0;
    }
}
