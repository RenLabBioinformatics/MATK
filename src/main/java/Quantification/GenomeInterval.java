package Quantification;

import htsjdk.tribble.index.interval.IntervalTree;

import java.util.*;

/**
 * Created by Tong on 2018/11/22.
 */
public class GenomeInterval {
    private HashMap<String, LinkedList<QuantifyPeakRecord>> PeakMap = new HashMap<String, LinkedList<QuantifyPeakRecord>>();
    private HashMap<String, IntervalTree> intervalTreeHashMap = new HashMap<String, IntervalTree>();
    private ArrayList<String> chrList = new ArrayList<>();

    public void setPeakMap(HashMap<String, LinkedList<QuantifyPeakRecord>> peakMap) {
        PeakMap = peakMap;
    }

    public HashMap<String, IntervalTree> getIntervalTreeHashMap() {
        return intervalTreeHashMap;
    }

    public ArrayList<String> getChrList() {
        return chrList;
    }

    public void MakePeakTree(int gap){
        for (Map.Entry<String, LinkedList<QuantifyPeakRecord>> entry : PeakMap.entrySet()) {
            IntervalTree intervalTree = new IntervalTree();
            String chr = entry.getKey();
            chrList.add(chr);
            LinkedList<QuantifyPeakRecord> peakList = entry.getValue();
            for(Iterator<QuantifyPeakRecord> iterator = peakList.iterator(); iterator.hasNext();){
                QuantifyPeakRecord peak = iterator.next();
                peak.setPeakBinList(gap);
                BedInterval interval = new BedInterval(peak);
                intervalTree.insert(interval);
//                LinkedList<int[]> PeakRegionList = peak.getPeakRegionList();
//                for (Iterator<int[]> region_iter = PeakRegionList.iterator(); region_iter.hasNext(); ) {
//                    int[] region = region_iter.next();
//                    int Start = region[0];
//                    int End = region[1];
//                    for(int i = Start; i<End;){
//                        BedInterval interval = new BedInterval(i, i+gap);
//                        i = i+gap;
//                        intervalTree.insert(interval);
//                    }
//                }
            }
            intervalTreeHashMap.put(chr,intervalTree);
        }
    }

    public void BamInformation2PeakTree(boolean isIP,boolean isTreat){
        for (Map.Entry<String, IntervalTree> entry : intervalTreeHashMap.entrySet()) {
            IntervalTree tree = entry.getValue();
            List intervals = tree.getIntervals();
            if(isIP) {
                for (Iterator<BedInterval> iterator = intervals.iterator(); iterator.hasNext(); ) {
                    BedInterval interval = iterator.next();
                    interval.AddIPReadsCountList2Record(true,isTreat);
                }
            }else{
                for (Iterator<BedInterval> iterator = intervals.iterator(); iterator.hasNext(); ) {
                    BedInterval interval = iterator.next();
                    interval.AddInputCountList2Record(true,isTreat);
                }
            }
        }
    }

}
