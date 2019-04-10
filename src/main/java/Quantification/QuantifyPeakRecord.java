package Quantification;

import Quantification.QuantifyMeRIPSeq.MCMCCompare;

import java.util.*;

/**
 * Created by Tong on 2018/4/17.
 */
public class QuantifyPeakRecord {
    private String chr;
    private int peakStart;
    private int peakEnd;
    private String GeneID;
    private String GeneName;
    private LinkedList<int[]> PeakRegionList = new LinkedList<int[]>();
    private ArrayList<Integer> PeakBinList = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> IPReadsCountList = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> InputReadsCountList = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> Treat_IPReadsCountList = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> Treat_InputReadsCountList = new ArrayList<>();
    private String bamfileword;
    private double PM_mean;
    private double PM_mean_control;
    private double PM_mean_treat;
    private double pvalue;
    private double qvalue;
    private double foldchange;
    private int rank;
    private LinkedList<Double> PMList = new LinkedList<>();
    private LinkedList<Double> PM_treat_List = new LinkedList<>();
    private LinkedList<Double> PM_control_List = new LinkedList<>();

    public void SetPeakRecord(String chr, int peakStart, int peakEnd) {
        this.chr = chr;
        this.peakStart = peakStart;
        this.peakEnd = peakEnd;
    }

    public QuantifyPeakRecord(String chr, int peakStart, int peakEnd, String bamfileword) {
        this.chr = chr;
        this.peakStart = peakStart;
        this.peakEnd = peakEnd;
        this.bamfileword = bamfileword;
    }

    public void AddPeakRegion(int regionStart, int regionEnd){
        int[] region = new int[]{regionStart, regionEnd};
        PeakRegionList.add(region);
    }

    public void setGeneID(String geneID){this.GeneID = geneID;}

    public String getGeneID(){return GeneID;}

    public void setGeneName(String geneName){this.GeneName = geneName;}

    public String getGeneName(){return GeneName;}

    public int getPeakStart() {
        return peakStart;
    }

    public int getPeakEnd() {
        return peakEnd;
    }

    public String getChr() {
        return chr;
    }

    public String getBamfileword() {
        return bamfileword;
    }

    public double getPM_mean() {
        return PM_mean;
    }

    public double getPvalue(){return pvalue;}

    public void setQvalue(double qvalue){this.qvalue = qvalue;}

    public double getQvalue(){return qvalue;}

    public double getPM_mean_control(){
        return PM_mean_control;
    }

    public double getPM_mean_treat(){
        return PM_mean_treat;
    }

    public void AddPM_MCMC_List(LinkedList<Double> PM_MCMC_List){
        this.PMList.addAll(PM_MCMC_List);
    }

    public void AddPM_MCMC_List_treat(LinkedList<Double> PM_treat_List){
        this.PM_treat_List.addAll(PM_treat_List);
        PM_treat_List.clear();
    }

    public void AddPM_MCMC_List_control(LinkedList<Double> PM_control_List){
        this.PM_control_List.addAll(PM_control_List);
        PM_control_List.clear();
    }

    public void calculatePM_mean(){
        double PM = 0;
        for(Iterator<Double> iterator = this.PMList.iterator(); iterator.hasNext();){
            PM = PM + iterator.next();
        }
        this.PM_mean = PM/PMList.size();
        PMList.clear();
    }

    public void calculatePM_mean_control(){
        double PM = 0;
        for(Iterator<Double> iterator = this.PM_control_List.iterator(); iterator.hasNext();){
            PM = PM + iterator.next();
        }
        this.PM_mean_control = PM/PM_control_List.size();

    }

    public void calculatePM_mean_treat(){
        double PM = 0;
        for(Iterator<Double> iterator = this.PM_treat_List.iterator(); iterator.hasNext();){
            PM = PM + iterator.next();
        }
        this.PM_mean_treat = PM/PM_treat_List.size();
    }

    public void setRank(int rank){this.rank = rank;}

    public int getRank(){return rank;}

    public LinkedList getPeakRegionList(){return PeakRegionList;}

    public void setPeakRegionList(LinkedList<int[]> peakRegionList){
        this.PeakRegionList = peakRegionList;
    }


    public void SortRegionList(){
        Collections.sort(PeakRegionList, new Comparator<int[]>() {
            @Override
            public int compare(int[] ints, int[] t1) {
                return (ints[0]-t1[0]);
            }
        });
        LinkedList<int[]> sort_PeakRegionList = new LinkedList<>();
        Iterator<int[]> iterator = PeakRegionList.iterator();
        int[] region = iterator.next();
        while(iterator.hasNext()){
            int[] temp = iterator.next();
            if(temp[0] <= region[1]){
                region = new int[]{region[0], temp[1]};
            }else{
                sort_PeakRegionList.add(region);
                region = temp;
            }
        }
        sort_PeakRegionList.add(region);
        this.PeakRegionList = sort_PeakRegionList;
    }

    public void calculateMCMCComparePValue(){
        MCMCCompare onepeak = new MCMCCompare();
        onepeak.setControl_MCMCList(this.PM_control_List);
        onepeak.setTreat_MCMCList(this.PM_treat_List);
        this.pvalue = onepeak.ComparePValue();
        PM_control_List.clear();
        PM_treat_List.clear();
    }

    public void treat_control_foldchange(){
        this.foldchange = PM_mean_treat/PM_mean_control;
    }

    public double getFoldchange(){
        return foldchange;
    }

    public void setPeakBinList(int gap){
        for (Iterator<int[]> region_iter = PeakRegionList.iterator(); region_iter.hasNext(); ) {
            int[] region = region_iter.next();
            int Start = region[0];
            int End = region[1];
            for(int i = Start; i<End;){
                PeakBinList.add(i);
                i = i+gap;
            }
        }
    }

    public ArrayList<Integer> getPeakBinList() {
        return PeakBinList;
    }

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

    public void AddIPReadsCountList(ArrayList<Integer> ReadsCountList){
        IPReadsCountList.add(ReadsCountList);
    }

    public void AddInputReadsCountList(ArrayList<Integer> ReadsCountList){
        InputReadsCountList.add(ReadsCountList);
    }

    public void AddTreatIPReadsCountList(ArrayList<Integer> ReadsCountList){
        Treat_IPReadsCountList.add(ReadsCountList);
    }

    public void AddTreatInputReadsCountList(ArrayList<Integer> ReadsCountList){
        Treat_InputReadsCountList.add(ReadsCountList);
    }
}
