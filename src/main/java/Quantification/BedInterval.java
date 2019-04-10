package Quantification;

import Quantification.ExtendedGTF.QuantifyFeatureRecord;
import htsjdk.samtools.SAMRecord;
import htsjdk.tribble.index.interval.Interval;

import java.util.ArrayList;

/**
 * Created by Tong on 2018/11/22.
 */
public class BedInterval extends Interval {
    private int start;
    private int end;
    private QuantifyPeakRecord peakRecord;
    private QuantifyFeatureRecord backgroundRecord;
    ArrayList<Integer> readsCountList = new ArrayList<>();

    public BedInterval(int start, int end) {
        super(start, end);
        this.start = start;
        this.end = end;
    }

    public BedInterval(QuantifyPeakRecord peakRecord){
        super(peakRecord.getPeakStart(), peakRecord.getPeakEnd());
        this.peakRecord = peakRecord;
        ArrayList<Integer> peakBinList = peakRecord.getPeakBinList();
        for(int i =0; i<peakBinList.size(); i++){
            readsCountList.add(0);
        }
    }

    public BedInterval(QuantifyFeatureRecord backgroundRecord){
        super(backgroundRecord.getStart(),backgroundRecord.getEnd());
        this.backgroundRecord = backgroundRecord;
        ArrayList<Integer> BinList = backgroundRecord.getBinList();
        for(int i =0; i<BinList.size(); i++){
            readsCountList.add(0);
        }
    }

    public void AddReadsCount(SAMRecord samRecord, int gap){
        int start = samRecord.getStart();
        int end = samRecord.getEnd();
        ArrayList<Integer> peakBinList = peakRecord.getPeakBinList();

        for(int i =0; i<peakBinList.size(); i++){
            int bin = peakBinList.get(i);
            if(start <= bin && end >= (bin + gap -1)){
                int readsCount = readsCountList.get(i);
                readsCount = readsCount + 1;
                readsCountList.set(i,readsCount);
            }
        }
    }

    public void AddBackReadsCount(SAMRecord samRecord, int gap){
        int start = samRecord.getStart();
        int end = samRecord.getEnd();
        ArrayList<Integer> backBinList = backgroundRecord.getBinList();
        for(int i =0; i<backBinList.size(); i++){
            int bin = backBinList.get(i);
            if(start <= bin && end >= (bin + gap -1)){
                int readsCount = readsCountList.get(i);
                readsCount = readsCount + 1;
                readsCountList.set(i,readsCount);
            }
        }
    }

    public void AddIPReadsCountList2Record(boolean isPeak, boolean isTreat){
        ArrayList<Integer> temp_readsCountList = new ArrayList<>();
        temp_readsCountList.addAll(readsCountList);
        for(int i = 0; i<readsCountList.size(); i++){
            readsCountList.set(i,0);
        }
        if(isTreat) {
            if (isPeak) {
                this.peakRecord.AddTreatIPReadsCountList(temp_readsCountList);
            } else {
                this.backgroundRecord.AddTreatIPReadsCountList(temp_readsCountList);
            }
        }else{
            if (isPeak) {
                this.peakRecord.AddIPReadsCountList(temp_readsCountList);
            } else {
                this.backgroundRecord.AddIPReadsCountList(temp_readsCountList);
            }
        }
    }

    public void AddInputCountList2Record(boolean isPeak,boolean isTreat){
        ArrayList<Integer> temp_readsCountList = new ArrayList<>();
        temp_readsCountList.addAll(readsCountList);
        for(int i = 0; i<readsCountList.size(); i++){
            readsCountList.set(i,0);
        }
        if(isTreat) {
            if (isPeak) {
                this.peakRecord.AddTreatInputReadsCountList(temp_readsCountList);
            } else {
                this.backgroundRecord.AddTreatInputReadsCountList(temp_readsCountList);
            }
        }else{
            if (isPeak) {
                this.peakRecord.AddInputReadsCountList(temp_readsCountList);
            } else {
                this.backgroundRecord.AddInputReadsCountList(temp_readsCountList);
            }
        }
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
