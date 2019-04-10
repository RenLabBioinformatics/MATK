package PeakCalling;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Ben on 2018/4/25.
 */
public class MergeBin {
    private HashMap<String, LinkedList<BinResult>> peakBinResMap;
    private HashMap<String, LinkedList<PeakRecord>> peakListMap;

    public MergeBin(HashMap<String, LinkedList<BinResult>> peakBinResMap) {
        this.peakBinResMap = peakBinResMap;
        peakListMap = new HashMap<String, LinkedList<PeakRecord>>();
    }

    public void Merge() {
        for(String chrName : peakBinResMap.keySet()) {
            LinkedList<BinResult> binResList = peakBinResMap.get(chrName);
            Collections.sort(binResList, new CompareBinResult());

            LinkedList<PeakRecord> peakList = new LinkedList<PeakRecord>();
            double averageCount = 0;
            for(BinResult binRes : binResList) {
                if(peakList.isEmpty()) {
                    PeakRecord peakRecord = new PeakRecord(chrName, binRes.getIndex(), binRes.getIndex(), binRes.getScore(), binRes.getProbability());
                    peakList.add(peakRecord);
                    averageCount = 1;
                } else {
                    PeakRecord lastPeakRecord = peakList.getLast();
                    if( binRes.getIndex() == (lastPeakRecord.getEndIndex() + 1) ) {
                        averageCount++;
                        double scoreAvr = lastPeakRecord.getScore() + (binRes.getScore() - lastPeakRecord.getScore())/averageCount;//Compute average using recursion formula
                        double proAvr = lastPeakRecord.getProbability() + (binRes.getProbability() - lastPeakRecord.getProbability())/averageCount;//Compute average using recursion formula
                        lastPeakRecord.setEndIndex(binRes.getIndex());
                        lastPeakRecord.setScore(scoreAvr);
                        lastPeakRecord.setProbability(proAvr);
                    } else {
                        PeakRecord peakRecord = new PeakRecord(chrName, binRes.getIndex(), binRes.getIndex(), binRes.getScore(), binRes.getProbability());
                        peakList.add(peakRecord);
                        averageCount = 1;
                    }
                }
            }
            peakListMap.put(chrName, peakList);
        }
    }

    public HashMap<String, LinkedList<PeakRecord>> getPeakListMap() {
        return peakListMap;
    }
}
