package Quantification.ExtendedGTF;

import Basic.GTF.FeatureRecord;
import Basic.GTF.GeneRecord;
import Quantification.NonPeakRegion;
import Quantification.PeakRecordComparator;
import Quantification.QuantifyPeakRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by Tong on 2018/10/18.
 */
public class QuantifyGeneRecord extends GeneRecord {
    private LinkedList<FeatureRecord> merge_featureList;
    private LinkedList<QuantifyFeatureRecord> merge_exceptPeakList;
    private LinkedList<QuantifyPeakRecord> peakList = new LinkedList<QuantifyPeakRecord>();
    private ArrayList<Integer> binList = new ArrayList<Integer>();
    private ArrayList<ArrayList<Integer>> IPbackgroundReadsList = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> InputbackgroundReadsList = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> Treat_IPbackgroundReadsList = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> Treat_InputbackgroundReadsList = new ArrayList<>();
    private ArrayList<Integer> indexList;

    public void setMerge_featureList(LinkedList<FeatureRecord> merge_featureList) {
        this.merge_featureList = merge_featureList;
    }

    public LinkedList getPeakList() {
        return peakList;
    }

    public void AddPeak(QuantifyPeakRecord peak) {
        peakList.add(peak);
    }

    public void RemovePeakRegion() {
        Collections.sort(peakList, new PeakRecordComparator());
        LinkedList<QuantifyFeatureRecord> merge_exceptPeakList = new LinkedList<QuantifyFeatureRecord>();
        if (peakList.size() > 0) {
            for (Iterator<FeatureRecord> feature_iter = merge_featureList.iterator(); feature_iter.hasNext(); ) {
                FeatureRecord feature = feature_iter.next();
                int feature_start = feature.getStart();
                int feature_end = feature.getEnd();
                LinkedList<QuantifyFeatureRecord> temp_NonPeakRegionList = NonPeakRegion.RemoveRegion(peakList, feature_start, feature_end);
                merge_exceptPeakList.addAll(temp_NonPeakRegionList);
                temp_NonPeakRegionList.clear();
//                if(merge_exceptPeakList.size() == 0){
//                    System.out.println(merge_featureList.size());
//                    System.out.println(feature_start +"\t" +feature_end +"\t" + this.getGeneId());
//                }
            }
        }
        if(merge_exceptPeakList.size() == 0 && peakList.size() > 0 ){
            System.out.println(this.getGeneId() + "!");
        }
        this.merge_exceptPeakList = merge_exceptPeakList;
    }

    public LinkedList<QuantifyFeatureRecord> getMerge_exceptPeakList() {
        return merge_exceptPeakList;
    }

    public void setBinList(int gap) {
        if (peakList.size() > 0) {
            for (Iterator<QuantifyFeatureRecord> merge_iter = merge_exceptPeakList.iterator(); merge_iter.hasNext(); ) {
                FeatureRecord featureRec = merge_iter.next();
                int start = featureRec.getStart();
                int end = featureRec.getEnd();
                int bin_site = start;
                while (bin_site < end) {
                    binList.add(bin_site);
                    bin_site = bin_site + gap;
                }
            }
        }
    }

    public ArrayList getBinList() {
        return binList;
    }

    public ArrayList<Integer> getIndexList() {
        return indexList;
    }

    public void setIndexList(ArrayList<Integer> indexList) {
        this.indexList = indexList;
    }

    public void setBackgroundReadsList(boolean isTreat) {
        if (peakList.size() > 0) {
            if (merge_exceptPeakList.size() != 0) {
//                System.out.println(this.getGeneName());
                QuantifyFeatureRecord featureRec_example = merge_exceptPeakList.get(0);
                ArrayList<ArrayList<Integer>> exampleList = featureRec_example.getIPReadsCountList();
//                System.out.println(exampleList.size());
                if (isTreat) {
                    for (int i = 0; i < exampleList.size(); i++) {
                        Treat_IPbackgroundReadsList.add(new ArrayList<>());
                        Treat_InputbackgroundReadsList.add(new ArrayList<>());
                    }
                    for (Iterator<QuantifyFeatureRecord> merge_iter = merge_exceptPeakList.iterator(); merge_iter.hasNext(); ) {
                        QuantifyFeatureRecord featureRec = merge_iter.next();
                        ArrayList<ArrayList<Integer>> IP_binReadsList = featureRec.getTreat_IPReadsCountList();
                        ArrayList<ArrayList<Integer>> Input_binReadsList = featureRec.getTreat_InputReadsCountList();
                        for (int i = 0; i < IP_binReadsList.size(); i++) {
                            ArrayList<Integer> temp_IPList = Treat_IPbackgroundReadsList.get(i);
                            temp_IPList.addAll(IP_binReadsList.get(i));
                            Treat_IPbackgroundReadsList.set(i, temp_IPList);
                            ArrayList<Integer> temp_InputList = Treat_InputbackgroundReadsList.get(i);
                            temp_InputList.addAll(Input_binReadsList.get(i));
                            Treat_InputbackgroundReadsList.set(i, temp_InputList);
                        }
                    }
                } else {
                    for (int i = 0; i < exampleList.size(); i++) {
                        IPbackgroundReadsList.add(new ArrayList<>());
                        InputbackgroundReadsList.add(new ArrayList<>());
                    }
                    for (Iterator<QuantifyFeatureRecord> merge_iter = merge_exceptPeakList.iterator(); merge_iter.hasNext(); ) {
                        QuantifyFeatureRecord featureRec = merge_iter.next();
                        ArrayList<ArrayList<Integer>> IP_binReadsList = featureRec.getIPReadsCountList();
                        ArrayList<ArrayList<Integer>> Input_binReadsList = featureRec.getInputReadsCountList();
                        for (int i = 0; i < IP_binReadsList.size(); i++) {
                            ArrayList<Integer> temp_IPList = IPbackgroundReadsList.get(i);
                            temp_IPList.addAll(IP_binReadsList.get(i));
                            IPbackgroundReadsList.set(i, temp_IPList);
                            ArrayList<Integer> temp_InputList = InputbackgroundReadsList.get(i);
                            temp_InputList.addAll(Input_binReadsList.get(i));
                            InputbackgroundReadsList.set(i, temp_InputList);
                        }
                    }

                }
            }
        }
    }

    public ArrayList<ArrayList<Integer>> getIPbackgroundReadsList() {
        return IPbackgroundReadsList;
    }

    public ArrayList<ArrayList<Integer>> getInputbackgroundReadsList() {
        return InputbackgroundReadsList;
    }

    public ArrayList<ArrayList<Integer>> getTreat_IPbackgroundReadsList() {
        return Treat_IPbackgroundReadsList;
    }

    public ArrayList<ArrayList<Integer>> getTreat_InputbackgroundReadsList() {
        return Treat_InputbackgroundReadsList;
    }

}
