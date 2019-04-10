package Quantification;

import java.util.Comparator;

/**
 * Created by Tong on 2018/4/17.
 */
public class PeakRecordComparator implements Comparator<QuantifyPeakRecord> {
    public int compare(QuantifyPeakRecord o1, QuantifyPeakRecord o2 ) {

        return (o1.getPeakStart() -o2.getPeakStart());

    }
}
