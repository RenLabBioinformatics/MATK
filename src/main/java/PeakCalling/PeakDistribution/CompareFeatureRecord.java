package PeakCalling.PeakDistribution;

import Basic.GTF.FeatureRecord;

import java.util.Comparator;

public class CompareFeatureRecord implements Comparator<FeatureRecord> {
    private boolean isAscending;

    public CompareFeatureRecord(boolean isAscending) {
        this.isAscending = isAscending;
    }

    @Override
    public int compare(FeatureRecord o1, FeatureRecord o2) {
        int start1 = o1.getStart();
        int start2 = o2.getStart();
        if(start1 < start2) {
            if(isAscending)
                return -1;
            else
                return 1;
        } else if(start1 > start2) {
            if(isAscending)
                return 1;
            else
                return -1;
        } else {
            return 0;
        }
    }
}
