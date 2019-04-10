package Quantification;

import java.util.Comparator;

/**
 * Created by Tong on 2018/5/29.
 */
public class PValueComparator implements Comparator<QuantifyPeakRecord> {
    public int compare(QuantifyPeakRecord o1, QuantifyPeakRecord o2 ) {
//        return o1.getPvalue().com
        if(o1.getPvalue() == o2.getPvalue()){
            return 0;
        }else if(o1.getPvalue() > o2.getPvalue()){
            return 1;
        }else{
            return -1;
        }
    }
}
