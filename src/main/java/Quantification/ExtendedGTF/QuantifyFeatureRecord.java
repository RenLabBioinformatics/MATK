package Quantification.ExtendedGTF;

import Basic.GTF.FeatureRecord;

import java.util.ArrayList;

/**
 * Created by Tong on 2018/11/26.
 */
public class QuantifyFeatureRecord extends FeatureRecord {
    private ArrayList<Integer> BinList = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> IPReadsCountList = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> InputReadsCountList = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> Treat_IPReadsCountList = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> Treat_InputReadsCountList = new ArrayList<>();

    public ArrayList getIPReadsCountList() {
        return IPReadsCountList;
    }

    public ArrayList<ArrayList<Integer>> getInputReadsCountList() {
        return InputReadsCountList;
    }

    public ArrayList<ArrayList<Integer>> getTreat_IPReadsCountList() {
        return Treat_IPReadsCountList;
    }

    public ArrayList<ArrayList<Integer>> getTreat_InputReadsCountList() {
        return Treat_InputReadsCountList;
    }

    public ArrayList<Integer> getBinList() {
        return BinList;
    }

    public void setBinList(int gap) {
        int start = this.getStart();
        int end = this.getEnd();
        for (int i = start; i < end; ) {
            BinList.add(i);
            i = i + gap;
        }
    }

    public void AddIPReadsCountList(ArrayList<Integer> readsCountList){
        IPReadsCountList.add(readsCountList);
    }

    public void AddInputReadsCountList(ArrayList<Integer> readsCountList){
        InputReadsCountList.add(readsCountList);
    }

    public void AddTreatIPReadsCountList(ArrayList<Integer> readsCountList){
        Treat_IPReadsCountList.add(readsCountList);
    }

    public void AddTreatInputReadsCountList(ArrayList<Integer> readsCountList){
        Treat_InputReadsCountList.add(readsCountList);
    }
}
