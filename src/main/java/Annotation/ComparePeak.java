package Annotation;

import Basic.BED.BEDRecord;

import java.util.Comparator;

public class ComparePeak implements Comparator<BEDRecord> {
    @Override
    public int compare(BEDRecord peak1, BEDRecord peak2) {
        if(peak1.getChrStart() < peak2.getChrStart())
            return -1;
        else if(peak1.getChrStart() > peak2.getChrStart())
            return 1;
        else
            return 0;
    }
}
