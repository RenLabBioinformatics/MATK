package Quantification.QuantifyMeRIPSeq;

import Basic.GTF.ChromosomeRecord;
import Quantification.ExtendedGTF.QuantifyChromosomeRecord;
import Quantification.ExtendedGTF.QuantifyGeneRecord;
import Quantification.*;
import htsjdk.tribble.index.interval.IntervalTree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

//import PeakCalling.PeakRecord;

/**
 * Created by Tong on 2018/5/26.
 */
public class DifferentialAnalysis {
    private String IP_bamfile_treat;
    private String Input_bamfile_treat;
    private String IP_bamfile_control;
    private String Input_bamfile_control;
    private String bed_file_treat;
    private String bed_file_control;
    private String gtf_file;
    private final int gap = 1;
    private final int background_gap = 5;
    private int iteration_time;
    private int burn_in_time;
    private String outputfile;
    private HashMap<String, QuantifyChromosomeRecord> chromosomeMap;

    public DifferentialAnalysis(String IP_bamfile_control, String Input_bamfile_control, String IP_bamfile_treat, String Input_bamfile_treat, String bed_file_control, String bed_file_treat, String gtf_file, int iteration_time, int burn_in_time, String outputfile) {
        this.IP_bamfile_control = IP_bamfile_control;
        this.Input_bamfile_control = Input_bamfile_control;
        this.IP_bamfile_treat = IP_bamfile_treat;
        this.Input_bamfile_treat = Input_bamfile_treat;
        this.bed_file_control = bed_file_control;
        this.bed_file_treat = bed_file_treat;
        this.iteration_time = iteration_time;
        this.burn_in_time = burn_in_time;
        this.gtf_file = gtf_file;
        this.outputfile = outputfile;
    }

    public void DifferentialAnalysisProcess() {
        HashMap<String, LinkedList<QuantifyPeakRecord>> Merge_PeakMap = PeakReader.MergePeak(bed_file_control, bed_file_treat);
        NonPeakRegion WithoutPeak = new NonPeakRegion();
        WithoutPeak.SetGTF(gtf_file);
        WithoutPeak.MergeTranscript();
        WithoutPeak.PeakAdd2Gene(Merge_PeakMap, gap);
        WithoutPeak.NonPeakBin(background_gap);
        HashMap<String, IntervalTree> NonPeakIntervalTreeMap = WithoutPeak.getIntervalTreeHashMap();
        HashMap<String, IntervalTree> PeakIntervalTreeMap = WithoutPeak.getIntervalPeakTreeHashMap();
        this.chromosomeMap = WithoutPeak.getChrMap();
//        ArrayList<bam2vec> IP_treat_BamList = new ArrayList<>();
//        ArrayList<bam2vec> IP_control_BamList = new ArrayList<>();
//        ArrayList<bam2vec> Input_treat_BamList = new ArrayList<>();
//        ArrayList<bam2vec> Input_control_BamList = new ArrayList<>();
        String[] Input_treat_bamfileArr = Input_bamfile_treat.split(";");
        String[] IP_treat_bamfileArr = IP_bamfile_treat.split(";");
        String[] Input_control_bamfileArr = Input_bamfile_control.split(";");
        String[] IP_control_bamfileArr = IP_bamfile_control.split(";");

        LinkedList<QuantifyPeakRecord> peakReordList = new LinkedList<>();

        try {
            for (int i = 0; i < Input_control_bamfileArr.length; i++) {
                String Input_bamfile_temp = Input_control_bamfileArr[i];
                bam2vec InputbamInformation = new bam2vec();
                InputbamInformation.bamfileReader(Input_bamfile_temp);
                InputbamInformation.Bam2Count(PeakIntervalTreeMap, NonPeakIntervalTreeMap, gap, background_gap);
//                Input_control_BamList.add(InputbamInformation);
                InputbamInformation.bamfileReaderClose();
                WithoutPeak.BamInformation2PeakTree(false, false);
                WithoutPeak.BamInformation2NonPeakTree(false, false);
            }

            for (int i = 0; i < IP_control_bamfileArr.length; i++) {
                String IP_bamfile_temp = IP_control_bamfileArr[i];
                bam2vec IPbamInformation = new bam2vec();
                IPbamInformation.bamfileReader(IP_bamfile_temp);
                IPbamInformation.Bam2Count(PeakIntervalTreeMap, NonPeakIntervalTreeMap, gap, background_gap);
//                IP_control_BamList.add(IPbamInformation);
                IPbamInformation.bamfileReaderClose();
                WithoutPeak.BamInformation2PeakTree(true, false);
                WithoutPeak.BamInformation2NonPeakTree(true, false);
            }
            for (int i = 0; i < Input_treat_bamfileArr.length; i++) {
                String Input_bamfile_temp = Input_treat_bamfileArr[i];
                bam2vec InputbamInformation = new bam2vec();
                InputbamInformation.bamfileReader(Input_bamfile_temp);
                InputbamInformation.Bam2Count(PeakIntervalTreeMap, NonPeakIntervalTreeMap, gap, background_gap);
//                Input_treat_BamList.add(InputbamInformation);
                InputbamInformation.bamfileReaderClose();
                WithoutPeak.BamInformation2PeakTree(false, true);
                WithoutPeak.BamInformation2NonPeakTree(false, true);
            }

            for (int i = 0; i < IP_treat_bamfileArr.length; i++) {
                String IP_bamfile_temp = IP_treat_bamfileArr[i];
                bam2vec IPbamInformation = new bam2vec();
                IPbamInformation.bamfileReader(IP_bamfile_temp);
                IPbamInformation.Bam2Count(PeakIntervalTreeMap, NonPeakIntervalTreeMap, gap, background_gap);
//                IP_treat_BamList.add(IPbamInformation);
                IPbamInformation.bamfileReaderClose();
                WithoutPeak.BamInformation2PeakTree(true, true);
                WithoutPeak.BamInformation2NonPeakTree(true, true);
            }

            for (Map.Entry<String, QuantifyChromosomeRecord> entry : chromosomeMap.entrySet()) {
                ChromosomeRecord chrRec = entry.getValue();
                LinkedList GeneList = chrRec.getGeneList();
                for (Iterator<QuantifyGeneRecord> gene_iter = GeneList.iterator(); gene_iter.hasNext(); ) {
                    QuantifyGeneRecord gene = gene_iter.next();
                    gene.setBackgroundReadsList(false);
                    gene.setBackgroundReadsList(true);
                    if (gene.getPeakList().size() > 0) {
                        QuantifyGeneRecord gene_change = DifferentPvalueFromMCMC(gene);
                        peakReordList.addAll(gene_change.getPeakList());
                    }
                }
                GeneList.clear();
            }

            FDR(peakReordList);
            DifferentialResultPrinter(outputfile, peakReordList);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public QuantifyGeneRecord DifferentPvalueFromMCMC(QuantifyGeneRecord Gene) {
        ArrayList<Integer> background_index = new ArrayList<Integer>();
        QuantifyGeneRecord Gene_change = new QuantifyGeneRecord();
        String GeneID = Gene.getGeneId();
        String GeneName = Gene.getGeneName();
//        System.out.println(GeneID);
        LinkedList<QuantifyPeakRecord> peakList = Gene.getPeakList();
        ArrayList<Integer> background_bin_List = Gene.getBinList();
//        ArrayList<Integer> background_index = Gene.getIndexList();

        for (int i = 0; i < background_bin_List.size(); i++) {
            background_index.add(i);
        }
        for (Iterator<QuantifyPeakRecord> peak_iter = peakList.iterator(); peak_iter.hasNext(); ) {
            QuantifyPeakRecord peak = peak_iter.next();
            peak.setGeneID(GeneID);
            peak.setGeneName(GeneName);
            Collections.shuffle(background_index);

            //control
            LinkedList<Double> Control_resultList = Quantify.OnePeakMCMC(peak, false, Gene, background_index, iteration_time, burn_in_time);
            peak.AddPM_MCMC_List_control(Control_resultList);
            peak.calculatePM_mean_control();

            //treat
            LinkedList<Double> Treat_resultList = Quantify.OnePeakMCMC(peak, true, Gene, background_index, iteration_time, burn_in_time);
            peak.AddPM_MCMC_List_treat(Treat_resultList);

            peak.calculatePM_mean_treat();
            peak.calculateMCMCComparePValue();
            peak.treat_control_foldchange();
        }
        Gene_change = Gene;
        return Gene_change;
    }

    public void FDR(LinkedList<QuantifyPeakRecord> PeakRecordList) {
        Collections.sort(PeakRecordList, new PValueComparator());
        int peak_size = PeakRecordList.size();
        int count = 0;
        for (Iterator<QuantifyPeakRecord> iterator = PeakRecordList.iterator(); iterator.hasNext(); ) {
            count++;
            QuantifyPeakRecord peak = iterator.next();
            double qvalue = peak.getPvalue() * peak_size / count;
            if (qvalue > 1) {
                qvalue = 1;
            }
            peak.setRank(count);
            peak.setQvalue(qvalue);
        }
    }

    public void DifferentialResultPrinter(String outputfile, LinkedList<QuantifyPeakRecord> PeakRecordList) {
        try {
            FileWriter fw = new FileWriter(outputfile);
            fw.write("chr\tpeakStart\tpeakEnd\tGene ID\tGene Name\tcontrol score\ttreat score\tfold change\tp-value\tq-value\n");
            for (Iterator<QuantifyPeakRecord> iterator = PeakRecordList.iterator(); iterator.hasNext(); ) {
                QuantifyPeakRecord peakRecord = iterator.next();
                fw.write(peakRecord.getChr() + "\t" + peakRecord.getPeakStart() + "\t" + peakRecord.getPeakEnd() + "\t" + peakRecord.getGeneID() + "\t");
                fw.write(peakRecord.getGeneName() + "\t" + peakRecord.getPM_mean_control() + "\t" + peakRecord.getPM_mean_treat() + "\t" + peakRecord.getFoldchange() + "\t" + peakRecord.getPvalue() + "\t" + peakRecord.getQvalue() + "\n");
            }
            fw.close();
        } catch (IOException ex) {

        }
    }

    public static void main(String[] args){
        String ControlbedFile = "G:\\大型文件\\MeRIP-Seq定量\\MiaoData\\NC\\NC_0314_peak.bed";
        String TreatedbedFile = "G:\\大型文件\\MeRIP-Seq定量\\MiaoData\\siWTAP1\\peak.bed";
        String gtfFile = "G:\\大型文件\\MeRIP-Seq定量\\MiaoData\\Homo_sapiens.GRCh38.90.chr.gtf";
        String ControlIPbamFile = "G:\\大型文件\\MeRIP-Seq定量\\MiaoData\\NC\\IpNC.sort.rmp.uniq.bam";
        String ControlInputbamFile = "G:\\大型文件\\MeRIP-Seq定量\\MiaoData\\NC\\InNC.sort.rmp.uniq.bam";
        String treatedIPbamFile = "G:\\大型文件\\MeRIP-Seq定量\\MiaoData\\siWTAP1\\Ipsiwp-1.sort.rmp.uniq.bam;G:\\大型文件\\MeRIP-Seq定量\\MiaoData\\siWTAP1\\Ipsiwp-11.sort.rmp.uniq.bam";
        String treatedInputbamFile = "G:\\大型文件\\MeRIP-Seq定量\\MiaoData\\siWTAP1\\Insiwp-1.sort.rmp.uniq.bam;G:\\大型文件\\MeRIP-Seq定量\\MiaoData\\siWTAP1\\Insiwp-11.sort.rmp.uniq.bam";
        int iteration = 10000;
        int burn_in_time = 9000;
        String outputfile = "G:\\大型文件\\debug\\diff_test.bed";
        DifferentialAnalysis differentialAnalysis = new DifferentialAnalysis(ControlIPbamFile,ControlInputbamFile,treatedIPbamFile,treatedInputbamFile,ControlbedFile,TreatedbedFile,gtfFile,iteration,burn_in_time,outputfile);
        differentialAnalysis.DifferentialAnalysisProcess();
    }

}
