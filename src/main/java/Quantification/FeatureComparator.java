package Quantification;

import Basic.GTF.FeatureRecord;

import java.util.Comparator;

/**
 * Created by Tong on 2018/4/17.
 */
public class FeatureComparator implements Comparator<FeatureRecord> {
    public int compare(FeatureRecord o1, FeatureRecord o2 ) {

        return (o1.getStart() -o2.getStart());

    }
}
