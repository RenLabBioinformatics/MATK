package PeakCalling.PeakDistribution;

import Basic.GTF.ChromosomeRecord;
import Basic.GTF.GeneRecord;
import htsjdk.tribble.index.interval.Interval;
import htsjdk.tribble.index.interval.IntervalTree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class GeneIntervalTree {
    private HashMap<String, IntervalTree> geneIntervalTreeMap;

    public GeneIntervalTree() {
        geneIntervalTreeMap = new HashMap<>();
    }

    public void InsertGeneRecords(ChromosomeRecord chromosomeRecord) {
        String chrName = chromosomeRecord.getChromosomeName();
        IntervalTree geneIntervalTree;
        if(geneIntervalTreeMap.containsKey(chrName))
            geneIntervalTree = geneIntervalTreeMap.get(chrName);
        else {
            geneIntervalTree = new IntervalTree();
            geneIntervalTreeMap.put(chrName, geneIntervalTree);
        }
        LinkedList<GeneRecord> chrGeneList = chromosomeRecord.getGeneList();
        for(GeneRecord geneRecord : chrGeneList) {
            String geneBioType = geneRecord.getBioType();
            if(geneBioType.equalsIgnoreCase("protein_coding")) {
                GeneIntervalRecord geneIntervalRecord = new GeneIntervalRecord(geneRecord);
                geneIntervalTree.insert(geneIntervalRecord);
            }
        }
    }

    public LinkedList<GeneRecord> QueryOverlappedGene(String chrName, int position, int strand, boolean isConsiderStrand) {
        LinkedList<GeneRecord> retGeneRecList = new LinkedList<>();
        if(geneIntervalTreeMap.containsKey(chrName)) {
            IntervalTree intervalTree = geneIntervalTreeMap.get(chrName);
            Interval queryInterval = new Interval(position, position);
            List<Interval> overlappedIntervals = intervalTree.findOverlapping(queryInterval);
            for(Interval interval : overlappedIntervals) {
                GeneIntervalRecord overlappedGeneRec = (GeneIntervalRecord) interval;
                if(isConsiderStrand) {
                    if (overlappedGeneRec.getGeneRecord().getStrand() == strand)
                        retGeneRecList.add(overlappedGeneRec.getGeneRecord());
                } else {
                    retGeneRecList.add(overlappedGeneRec.getGeneRecord());
                }
            }
        }
        return retGeneRecList;
    }

    public LinkedList<GeneRecord> QueryOverlappedGene(String chrName, int startPosition, int endPosition, int strand, boolean isConsiderStrand) {
        LinkedList<GeneRecord> retGeneRecList = new LinkedList<>();
        if(geneIntervalTreeMap.containsKey(chrName)) {
            IntervalTree intervalTree = geneIntervalTreeMap.get(chrName);
            Interval queryInterval = new Interval(startPosition, endPosition);
            List<Interval> overlappedIntervals = intervalTree.findOverlapping(queryInterval);
            for(Interval interval : overlappedIntervals) {
                GeneIntervalRecord overlappedGeneRec = (GeneIntervalRecord) interval;
                if(isConsiderStrand) {
                    if (overlappedGeneRec.getGeneRecord().getStrand() == strand)
                        retGeneRecList.add(overlappedGeneRec.getGeneRecord());
                } else {
                    retGeneRecList.add(overlappedGeneRec.getGeneRecord());
                }
            }
        }
        return retGeneRecList;
    }
}
