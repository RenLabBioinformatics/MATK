package Annotation;

import Basic.BED.BEDRecord;
import Basic.BED.BLOCKRecord;
import Basic.GTF.FeatureRecord;
import Basic.GTF.GeneRecord;
import htsjdk.tribble.index.interval.Interval;
import htsjdk.tribble.index.interval.IntervalTree;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class GeneAnnotationRecord{
    private LinkedList<BEDRecord> associatedPeakList;
    private GeneRecord geneRec;
    private IntervalTree featureIntervalTree;
    private final int startTrim = 1, endTrim = 2, bothTrim = 3, inside = 4, outside = 5;

    public GeneAnnotationRecord(GeneRecord geneRec) {
        this.geneRec = geneRec;
        associatedPeakList = new LinkedList<>();
        //Create feature interval tree
        featureIntervalTree = new IntervalTree();
        LinkedList<FeatureRecord> featureRecList = geneRec.getFeatureList();
        for(FeatureRecord featureRec : featureRecList) {
            FeatureRecordInterval interval = new FeatureRecordInterval(featureRec);
            featureIntervalTree.insert(interval);
        }
    }

    public void AddAssociatedPeak(BEDRecord peakRec) {
        associatedPeakList.add(peakRec);
    }

    public LinkedList<BEDRecord> getAssociatedPeakList() {
        return associatedPeakList;
    }

    //Refine the start and end position according to GTF annotation
    private int RefinePeak(BEDRecord peakRec, int featureStart, int featureEnd) {
        int retVal = inside;
        boolean isStartTrim = false;
        int peakStart = peakRec.getChrStart() + 1;
        int peakEnd = peakRec.getChrEnd() + 1;
        if(peakStart <= featureStart) {
            peakStart = featureStart - 1;
            retVal = startTrim;
            isStartTrim = true;
        }
        if(peakEnd >= featureEnd) {
            peakEnd = featureEnd - 1;
            if(isStartTrim)
                retVal = bothTrim;
            else
                retVal = endTrim;
        }
        peakRec.setChrStart(peakStart);
        peakRec.setChrEnd(peakEnd);
        return retVal;
    }

    public void MergeBlock() {
        Collections.sort(associatedPeakList, new ComparePeak());
        BEDRecord forePeak = null;
        int index = 0;
        int foreType = outside, curType;
        for(Iterator<BEDRecord> itr = associatedPeakList.iterator(); itr.hasNext();) {
            BEDRecord bedRec = itr.next();
            //Find overlapped feature and refine peak position
            Interval queryInterval = new Interval(bedRec.getChrStart() + 1, bedRec.getChrEnd() + 1);//translate to 1-base coordinate
            List<Interval> overlapIntervalList = featureIntervalTree.findOverlapping(queryInterval);
            FeatureRecordInterval overlapInterval;
            if(overlapIntervalList.size() == 0) {
                overlapInterval = null;
                curType = outside;
            } else {
                overlapInterval = (FeatureRecordInterval) (overlapIntervalList.get(0));
                curType = RefinePeak(bedRec, overlapInterval.getFeatureRec().getStart(), overlapInterval.getFeatureRec().getEnd());
            }
            if(index == 0)
                foreType = curType;
            //Merge peak block
            if(forePeak == null)
                forePeak = bedRec;
            else {
                if( ((foreType == bothTrim) || (foreType == endTrim))&&(curType == startTrim) ) {
                    forePeak.setChrEnd(bedRec.getChrEnd());
                    BLOCKRecord blockRecord = new BLOCKRecord(bedRec.getChrName(), bedRec.getChrStart(), bedRec.getChrEnd());
                    LinkedList<BLOCKRecord> blockRecList;
                    if(forePeak.getBlockRecList() == null) {
                        blockRecList = new LinkedList<>();
                        BLOCKRecord initialBlock = new BLOCKRecord(forePeak.getChrName(), forePeak.getChrStart(), forePeak.getChrEnd());
                        blockRecList.add(initialBlock);
                        forePeak.setBlockRecList(blockRecList);
                    } else {
                        blockRecList = forePeak.getBlockRecList();
                    }
                    blockRecList.add(blockRecord);
                    itr.remove();
                } else {
                    forePeak = bedRec;
                    foreType = curType;
                }
            }
            index++;
        }
    }

    public GeneRecord getGeneRec() {
        return geneRec;
    }
}
