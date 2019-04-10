package SingleNucleotide.Validation;

import java.util.Comparator;

public class CompareAUCPerformance implements Comparator<Performance> {
    @Override
    public int compare(Performance o1, Performance o2) {
        double sp1 = o1.getSp();
        double sp2 = o2.getSp();
        if(sp1 > sp2)
            return -1;
        else if(sp1 < sp2)
            return 1;
        else
            return 0;
    }
}
