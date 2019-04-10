package SingleNucleotide.Data.MeRIP;

import SingleNucleotide.Data.SiteRecord;
import htsjdk.samtools.SAMRecord;
import htsjdk.tribble.index.interval.Interval;
import htsjdk.tribble.index.interval.IntervalTree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SiteIntervalTree {
    private HashMap<String, IntervalTree> intervalTreeHashMap;

    public SiteIntervalTree() {
        intervalTreeHashMap = new HashMap<>();
    }

    public void AddSiteRecordToTree(SiteRecord siteRecord, int upstream, int downstream) {
        String chrName = siteRecord.getChrName();
        IntervalTree intervalTree;
        if(intervalTreeHashMap.containsKey(chrName))
            intervalTree = intervalTreeHashMap.get(chrName);
        else {
            intervalTree = new IntervalTree();
            intervalTreeHashMap.put(chrName, intervalTree);
        }

        SiteIntervalRecord siteIntervalRec = new SiteIntervalRecord(siteRecord, upstream, downstream);
        intervalTree.insert(siteIntervalRec);
    }

    public LinkedList<SiteRecord> QueryOverlappedSite(SAMRecord samRec) {
        LinkedList<SiteRecord> siteRecords = new LinkedList<>();

        String chrName = samRec.getContig();
        if(intervalTreeHashMap.containsKey(chrName)) {
            IntervalTree intervalTree = intervalTreeHashMap.get(chrName);
            Interval queryInterval = new Interval(samRec.getAlignmentStart(), samRec.getAlignmentEnd());
            List<Interval> overlappedIntervals = intervalTree.findOverlapping(queryInterval);
            for(Interval interval : overlappedIntervals) {
                SiteIntervalRecord siteIntervalRecord = (SiteIntervalRecord)interval;
                siteRecords.add(siteIntervalRecord.getSiteRecord());
            }
        }

        return siteRecords;
    }

    public LinkedList<SiteRecord> QueryOverlappedSite(String chrName, int alignmentStart, int alignmentEnd) {
        LinkedList<SiteRecord> siteRecords = new LinkedList<>();

        if(intervalTreeHashMap.containsKey(chrName)) {
            IntervalTree intervalTree = intervalTreeHashMap.get(chrName);
            Interval queryInterval = new Interval(alignmentStart, alignmentEnd);
            List<Interval> overlappedIntervals = intervalTree.findOverlapping(queryInterval);
            for(Interval interval : overlappedIntervals) {
                SiteIntervalRecord siteIntervalRecord = (SiteIntervalRecord)interval;
                siteRecords.add(siteIntervalRecord.getSiteRecord());
            }
        }

        return siteRecords;
    }
}
