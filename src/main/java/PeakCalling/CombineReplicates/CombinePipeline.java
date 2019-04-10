package PeakCalling.CombineReplicates;

import Basic.BED.BEDRecord;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

public class CombinePipeline {
    private LinkedList<BEDIntervalTree> bedIntervalTrees;
    private CombineSet[] confirmedSets, discardSets;
    private HashMap<String, LinkedList<BEDRecord>> combinedBEDRecMap;

    public CombinePipeline() {
        bedIntervalTrees = new LinkedList<>();
    }

    public void AddBEDRecordMap(HashMap<String, LinkedList<BEDRecord>> bedRecMap) {
        BEDIntervalTree bedIntervalTree = new BEDIntervalTree(bedRecMap);
        bedIntervalTrees.add(bedIntervalTree);
    }

    public void AddBEDFile(File bedFile) {
        BEDIntervalTree bedIntervalTree = new BEDIntervalTree(bedFile);
        bedIntervalTrees.add(bedIntervalTree);
    }

    private LinkedList<BEDRecord> QueryOverlappedPeaks(BEDRecord bedRec) {
        LinkedList<BEDRecord> overlappedList = new LinkedList<>();

        for(BEDIntervalTree bedIntervalTree : bedIntervalTrees) {
            BEDRecord bestOverlappedBEDRec = bedIntervalTree.QueryBestOverlappedBEDRecord(bedRec);
            if(bestOverlappedBEDRec != null)
                overlappedList.add(bestOverlappedBEDRec);
        }

        return overlappedList;
    }

    private double FishersMethod(LinkedList<BEDRecord> bestOverlappedList) {
        double chiSquare = 0;
        for(BEDRecord bedRec : bestOverlappedList) {
            chiSquare = chiSquare + Math.log(bedRec.getScore());
        }
        chiSquare = -2*chiSquare;

        ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(2 * bestOverlappedList.size());
        return (1 - chiSquaredDistribution.cumulativeProbability(chiSquare));
    }

    public void Combine(int C, boolean isTechnicalReplicate) {
        //Setup confirmed set and discard set
        confirmedSets = new CombineSet[bedIntervalTrees.size()];
        for(int i=0; i<confirmedSets.length; i++)
            confirmedSets[i] = new CombineSet();

        discardSets = new CombineSet[bedIntervalTrees.size()];
        for(int i=0; i<discardSets.length; i++)
            discardSets[i] = new CombineSet();
        //Combine replicates
        int index = 0;
        for(BEDIntervalTree bedIntervalTree : bedIntervalTrees) {
            HashMap<String, LinkedList<BEDRecord>> bedRecMap = bedIntervalTree.getBedRecMap();
            for(String chrName : bedRecMap.keySet()) {
                LinkedList<BEDRecord> bedRecList = bedRecMap.get(chrName);
                for(BEDRecord bedRec : bedRecList) {
                    LinkedList<BEDRecord> bestOverlappedList = QueryOverlappedPeaks(bedRec);
                    if(bestOverlappedList.size() >= C) {
                        double fisherPValue = FishersMethod(bestOverlappedList);
                        if(fisherPValue < 0.05)
                            confirmedSets[index].AddRecord(bedRec);
                        else
                            discardSets[index].AddRecord(bedRec);
                    } else {
                        discardSets[index].AddRecord(bedRec);
                    }
                }
            }
            index++;
        }
        for(int i=0; i<confirmedSets.length; i++) {
            CombineSet intersectSet = confirmedSets[i].getIntersect(discardSets[i]);
            if(isTechnicalReplicate) {
                confirmedSets[i].RemoveSet(intersectSet);
            } else {
                discardSets[i].RemoveSet(intersectSet);
            }
        }
        //Merge combined peaks
        MergePeaks mergePeaks = new MergePeaks(confirmedSets);
        mergePeaks.Merge();
        combinedBEDRecMap = mergePeaks.getMergedBedRecMap();
    }

    public CombineSet[] getConfirmedSets() {
        return confirmedSets;
    }

    public CombineSet[] getDiscardSets() {
        return discardSets;
    }

    public HashMap<String, LinkedList<BEDRecord>> getCombinedBEDRecMap() {
        return combinedBEDRecMap;
    }

    public void SaveCombinedBEDRecord(File saveFile) {
        try {
            FileWriter fw = new FileWriter(saveFile);
            for(String chrName : combinedBEDRecMap.keySet()) {
                LinkedList<BEDRecord> bedRecList = combinedBEDRecMap.get(chrName);
                for(BEDRecord bedRec : bedRecList) {
                    fw.write(chrName + "\t" + bedRec.getChrStart() + "\t" + bedRec.getChrEnd() + "\t" + bedRec.getName() + "\t" + bedRec.getScore() + "\n");
                }
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CombinePipeline combinePipeline = new CombinePipeline();
        combinePipeline.AddBEDFile(new File("E:\\工作文档\\MATK\\PeakCalling\\A549\\SRR2120887_SRR2120890.bed"));
        combinePipeline.AddBEDFile(new File("E:\\工作文档\\MATK\\PeakCalling\\A549\\SRR2120888_SRR2120891.bed"));
        combinePipeline.AddBEDFile(new File("E:\\工作文档\\MATK\\PeakCalling\\A549\\SRR2120889_SRR2120892.bed"));
        combinePipeline.Combine(2, false);
        combinePipeline.SaveCombinedBEDRecord(new File("E:\\工作文档\\MATK\\PeakCalling\\A549\\Combined.bed"));
    }
}
