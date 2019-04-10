package Annotation;

import Basic.BED.BEDRecord;
import htsjdk.tribble.index.interval.Interval;
import htsjdk.tribble.index.interval.IntervalTree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class GeneAnnotationRecordIntervalTree {
    HashMap<String, IntervalTree> geneAnnoRecTreeMap;

    public GeneAnnotationRecordIntervalTree(HashMap<String, LinkedList<GeneAnnotationRecord>> geneAnnoRecMap) {
        geneAnnoRecTreeMap = new HashMap<>();
        for(String chrName : geneAnnoRecMap.keySet()) {
            IntervalTree intervalTree = new IntervalTree();
            geneAnnoRecTreeMap.put(chrName, intervalTree);
            LinkedList<GeneAnnotationRecord> geneAnnoRecList = geneAnnoRecMap.get(chrName);
            for(GeneAnnotationRecord geneAnnoRec : geneAnnoRecList) {
                GeneAnnotationRecordInterval geneAnnoRecInterval = new GeneAnnotationRecordInterval(geneAnnoRec);
                intervalTree.insert(geneAnnoRecInterval);
            }
        }
    }

    public void AssignPeakToGeneAnnotation(BEDRecord peakRec) {
        String chrName = peakRec.getChrName();
        if(geneAnnoRecTreeMap.containsKey(chrName)) {
            IntervalTree intervalTree = geneAnnoRecTreeMap.get(chrName);
            Interval queryInterval = new Interval(peakRec.getChrStart() + 1, peakRec.getChrEnd() + 1);
            List<Interval> overlappedList = intervalTree.findOverlapping(queryInterval);
            if(overlappedList.size() > 0) {
                GeneAnnotationRecordInterval bestMatchInterval = (GeneAnnotationRecordInterval) overlappedList.get(0);
                for (Interval interval : overlappedList) {
                    GeneAnnotationRecordInterval curGeneInterval = (GeneAnnotationRecordInterval)interval;
                    if(curGeneInterval.getGeneAnnoRec().getGeneRec().getBioType().equalsIgnoreCase("protein_coding")) {
                        bestMatchInterval = curGeneInterval;
                        break;
                    }
                }
                peakRec.setName(bestMatchInterval.getGeneAnnoRec().getGeneRec().getGeneName());
                peakRec.setStrand(bestMatchInterval.getGeneAnnoRec().getGeneRec().getStrand());
                bestMatchInterval.getGeneAnnoRec().AddAssociatedPeak(peakRec);
            }
        }
    }
}
