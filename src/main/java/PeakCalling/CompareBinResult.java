package PeakCalling;

import java.util.Comparator;

/**
 * Created by Ben on 2018/4/25.
 */
public class CompareBinResult implements Comparator<BinResult> {
    public int compare(BinResult o1, BinResult o2) {
        if(o1.getIndex() < o2.getIndex()) {
            return -1;
        } else if ( o2.getIndex() > o2.getIndex()) {
            return 1;
        } else {
            return 0;
        }
    }
}
