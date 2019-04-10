package PeakCalling.CombineReplicates;

import Basic.BED.BEDRecord;

import java.util.Comparator;

public class CompareBEDRecord implements Comparator<BEDRecord> {
    @Override
    public int compare(BEDRecord o1, BEDRecord o2) {
        int startPos1 = o1.getChrStart();
        int startPos2 = o2.getChrStart();
        if(startPos1 < startPos2)
            return -1;
        else if(startPos1 > startPos2)
            return 1;
        else
            return 0;
    }
}
