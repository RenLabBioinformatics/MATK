package PeakCalling.CombineReplicates;

import Basic.BED.BEDReader;
import Basic.BED.BEDRecord;
import htsjdk.tribble.index.interval.Interval;
import htsjdk.tribble.index.interval.IntervalTree;
import play.mvc.WebSocket;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class BEDIntervalTree {
    private HashMap<String, IntervalTree> intervalTreeMap;
    private HashMap<String, LinkedList<BEDRecord>> bedRecMap;

    public BEDIntervalTree(HashMap<String, LinkedList<BEDRecord>> bedRecMap) {
        this.bedRecMap = bedRecMap;
        intervalTreeMap = new HashMap<>();
        //Building interval tree for each chromosome
        for(String chrName : bedRecMap.keySet()) {
            IntervalTree intervalTree;
            if(intervalTreeMap.containsKey(chrName))
                intervalTree = intervalTreeMap.get(chrName);
            else {
                intervalTree = new IntervalTree();
                intervalTreeMap.put(chrName, intervalTree);
            }
            LinkedList<BEDRecord> bedRecList = bedRecMap.get(chrName);
            for(BEDRecord bedRec : bedRecList) {
                BEDIntervalRecord bedIntervalRecord = new BEDIntervalRecord(bedRec);
                intervalTree.insert(bedIntervalRecord);
            }
        }
    }

    public BEDIntervalTree(File bedFile) {
        BEDReader bedReader = new BEDReader(bedFile);
        bedRecMap = bedReader.getBEDChromosomeMap();
        intervalTreeMap = new HashMap<>();
        //Building interval tree for each chromosome
        for(String chrName : bedRecMap.keySet()) {
            IntervalTree intervalTree;
            if(intervalTreeMap.containsKey(chrName))
                intervalTree = intervalTreeMap.get(chrName);
            else {
                intervalTree = new IntervalTree();
                intervalTreeMap.put(chrName, intervalTree);
            }
            LinkedList<BEDRecord> bedRecList = bedRecMap.get(chrName);
            for(BEDRecord bedRec : bedRecList) {
                BEDIntervalRecord bedIntervalRecord = new BEDIntervalRecord(bedRec);
                intervalTree.insert(bedIntervalRecord);
            }
        }
    }

    public LinkedList<BEDRecord> QueryOverlappedBEDRecord(BEDRecord queryBedRec) {
        LinkedList<BEDRecord> retList = new LinkedList<>();

        BEDIntervalRecord queryIntervalRec = new BEDIntervalRecord(queryBedRec);

        String chrName = queryBedRec.getChrName();
        if(intervalTreeMap.containsKey(chrName)) {
            IntervalTree intervalTree = intervalTreeMap.get(chrName);
            List<Interval> overlappedInterval = intervalTree.findOverlapping(queryIntervalRec);
            for(Interval tmpInterval : overlappedInterval) {
                BEDIntervalRecord bedIntervalRecord = (BEDIntervalRecord)tmpInterval;
                retList.add(bedIntervalRecord.getBedRec());
            }
        }

        return retList;
    }

    public BEDRecord QueryBestOverlappedBEDRecord(BEDRecord queryBedRec) {
        BEDRecord bestBedRec = null;

        BEDIntervalRecord queryIntervalRec = new BEDIntervalRecord(queryBedRec);

        String chrName = queryBedRec.getChrName();
        Double minVal = Double.POSITIVE_INFINITY;
        if(intervalTreeMap.containsKey(chrName)) {
            IntervalTree intervalTree = intervalTreeMap.get(chrName);
            List<Interval> overlappedInterval = intervalTree.findOverlapping(queryIntervalRec);
            for(Interval tmpInterval : overlappedInterval) {
                BEDIntervalRecord bedIntervalRecord = (BEDIntervalRecord)tmpInterval;
                if(bedIntervalRecord.getBedRec().getScore() < minVal) {
                    minVal = bedIntervalRecord.getBedRec().getScore();
                    bestBedRec = bedIntervalRecord.getBedRec();
                }
            }
        }

        return bestBedRec;
    }

    public HashMap<String, LinkedList<BEDRecord>> getBedRecMap() {
        return bedRecMap;
    }
}
