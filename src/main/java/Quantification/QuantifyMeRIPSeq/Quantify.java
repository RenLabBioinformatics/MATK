package Quantification.QuantifyMeRIPSeq;

import Basic.GTF.ChromosomeRecord;
import Quantification.*;
import Quantification.ExtendedGTF.QuantifyChromosomeRecord;
import Quantification.ExtendedGTF.QuantifyGeneRecord;
import Quantification.MCMC.MCMCIteration;
import htsjdk.tribble.index.interval.IntervalTree;
import org.apache.commons.math3.distribution.UniformRealDistribution;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by Tong on 2018/4/19.
 */
public class Quantify {
    private String IP_bamfile;
    private String Input_bamfile;
    private String bed_file;
    private String gtf_file;
    private final int gap = 1;
    private final int background_gap = 5;
    private int iteration_time;
    private int burn_in_time;
    private String outputfile;
    private HashMap<String, QuantifyChromosomeRecord> chromosomeMap;

    public Quantify(String IP_bamfile, String input_bamfile, String bed_file, String gtf_file, int iteration_time, int burn_in_time, String outputfile) {
        this.IP_bamfile = IP_bamfile;
        this.Input_bamfile = input_bamfile;
        this.bed_file = bed_file;
        this.gtf_file = gtf_file;
        this.iteration_time = iteration_time;
        this.burn_in_time = burn_in_time;
        this.outputfile = outputfile;
    }

    public void QuantifyProcess() {
        ArrayList<bam2vec> IPBamList = new ArrayList<>();
        ArrayList<bam2vec> InputBamList = new ArrayList<>();
        String[] Input_bamfileArr = Input_bamfile.split(";");
        String[] IP_bamfileArr = IP_bamfile.split(";");

        PeakReader peakReader = new PeakReader(bed_file);
        HashMap<String, LinkedList<QuantifyPeakRecord>> PeakMap = peakReader.getPeakMap();
//        GenomeInterval PeakInterval = new GenomeInterval();
//        PeakInterval.setPeakMap(PeakMap);
//        PeakInterval.MakePeakTree(gap);
//        HashMap<String, IntervalTree> PeakIntervalTreeMap = PeakInterval.getIntervalTreeHashMap();
//        ArrayList<String> chrList = PeakInterval.getChrList();
        NonPeakRegion WithoutPeak = new NonPeakRegion();
        WithoutPeak.SetGTF(gtf_file);
        WithoutPeak.PeakAdd2Gene(PeakMap, gap);
        WithoutPeak.MergeTranscript();
        WithoutPeak.NonPeakBin(background_gap);
        HashMap<String, IntervalTree> NonPeakIntervalTreeMap = WithoutPeak.getIntervalTreeHashMap();
        HashMap<String, IntervalTree> PeakIntervalTreeMap = WithoutPeak.getIntervalPeakTreeHashMap();
        this.chromosomeMap = WithoutPeak.getChrMap();

        for (int i = 0; i < Input_bamfileArr.length; i++) {
            String Input_bamfile_temp = Input_bamfileArr[i];
            bam2vec InputbamInformation = new bam2vec();
            InputbamInformation.bamfileReader(Input_bamfile_temp);
            InputbamInformation.Bam2Count(PeakIntervalTreeMap, NonPeakIntervalTreeMap, gap, background_gap);
            InputBamList.add(InputbamInformation);
            InputbamInformation.bamfileReaderClose();
            WithoutPeak.BamInformation2PeakTree(false, false);
            WithoutPeak.BamInformation2NonPeakTree(false, false);
        }

        for (int i = 0; i < IP_bamfileArr.length; i++) {
            String IP_bamfile_temp = IP_bamfileArr[i];
            bam2vec IPbamInformation = new bam2vec();
            IPbamInformation.bamfileReader(IP_bamfile_temp);
            IPbamInformation.Bam2Count(PeakIntervalTreeMap, NonPeakIntervalTreeMap, gap, background_gap);
            IPBamList.add(IPbamInformation);
            IPbamInformation.bamfileReaderClose();
            WithoutPeak.BamInformation2PeakTree(true, false);
            WithoutPeak.BamInformation2NonPeakTree(true, false);
        }

        String file = this.outputfile;

        try {
            FileWriter fw = new FileWriter(file);
            fw.write("# chr\tpeakStart\tpeakEnd\tGene Annotation\tscore\n");
            for (Map.Entry<String, QuantifyChromosomeRecord> entry : chromosomeMap.entrySet()) {
                String chr = entry.getKey();
                ChromosomeRecord chrRec = entry.getValue();
                LinkedList GeneList = chrRec.getGeneList();
                for (Iterator<QuantifyGeneRecord> gene_iter = GeneList.iterator(); gene_iter.hasNext(); ) {
                    QuantifyGeneRecord gene = gene_iter.next();
                    String geneID = gene.getGeneId();
                    String geneName = gene.getGeneName();
                    gene.setBackgroundReadsList(false);
                    if (gene.getPeakList().size() > 0) {
                        Gene2MCMC(gene, IPBamList, InputBamList, chr);
                        Quantify.ResultPrinter(fw, gene.getPeakList(), geneID, geneName);
                    }
                }

//                System.out.println(chr);
            }
            IPBamList.clear();
            InputBamList.clear();
            fw.close();
        } catch (IOException ex) {
            System.out.println("IOException");
        }

    }

    public void Gene2MCMC(QuantifyGeneRecord Gene, ArrayList<bam2vec> IPBamList, ArrayList<bam2vec> InputBamList, String chr) {
        UniformRealDistribution unifromdi = new UniformRealDistribution(0, 1);
        LinkedList<Double> YP = new LinkedList<Double>();
        LinkedList<Double> YB;
        LinkedList<Double> XP = new LinkedList<Double>();
        LinkedList<Double> XB;
        ArrayList<Integer> background_index = new ArrayList<Integer>();
//        ArrayList<Integer> background_index = Gene.getIndexList();
        LinkedList<QuantifyPeakRecord> peakList = Gene.getPeakList();
        ArrayList<Integer> background_bin_List = Gene.getBinList();
        ArrayList<ArrayList<Integer>> Input_background_readsCountList = Gene.getInputbackgroundReadsList();
        ArrayList<ArrayList<Integer>> IP_background_readsCountList = Gene.getIPbackgroundReadsList();
//        System.out.println(IP_background_readsCountList.size());
        for (int i = 0; i < background_bin_List.size(); i++) {
            background_index.add(i);
        }
        for (Iterator<QuantifyPeakRecord> peak_iter = peakList.iterator(); peak_iter.hasNext(); ) {
            QuantifyPeakRecord peak = peak_iter.next();
            Collections.shuffle(background_index);
            ArrayList<ArrayList<Integer>> Input_readsCountList = peak.getInputReadsCountList();
            ArrayList<ArrayList<Integer>> IP_readsCountList = peak.getIPReadsCountList();
            if(Input_background_readsCountList.size() ==0){
                Input_background_readsCountList = peak.getInputReadsCountList();
            }
            if(IP_background_readsCountList.size() == 0){
                IP_background_readsCountList = peak.getInputReadsCountList();
            }
//            LinkedList<int[]> peakRegionList = peak.getPeakRegionList();
            for (int i = 0; i < IPBamList.size(); i++) {
                double IP_reads_count = 0;
                double Input_reads_count = 0;
                ArrayList<Integer> IPBam_readsCount = IP_readsCountList.get(i);
                ArrayList<Integer> InputBam_readsCount = Input_readsCountList.get(i);
                ArrayList<Integer> IPBackground_readsCount = IP_background_readsCountList.get(i);
                ArrayList<Integer> InputBackground_readsCount = Input_background_readsCountList.get(i);
                for (int bin = 0; bin < IPBam_readsCount.size(); bin++) {
                    int readsCount = IPBam_readsCount.get(bin);
                    IP_reads_count = IP_reads_count + readsCount;
                    XP.add((double) readsCount);
                }
                for (int bin = 0; bin < InputBam_readsCount.size(); bin++) {
                    int readsCount = InputBam_readsCount.get(bin);
                    Input_reads_count = Input_reads_count + readsCount;
                    YP.add((double) readsCount);
                }

                XB = new LinkedList<Double>();
                int count = 0;
                int real_count = 0;
//
                YB = new LinkedList<Double>();
                while (count < YP.size() && count < XP.size() && real_count < background_index.size()) {
                    double Input_back_read = InputBackground_readsCount.get(background_index.get(real_count));
                    double IP_back_read = IPBackground_readsCount.get(background_index.get(real_count));

                    if (Input_back_read != 0 && IP_back_read != 0) {
                        YB.add(Input_back_read);
                        XB.add(IP_back_read);
                        Input_reads_count = Input_reads_count + Input_back_read;
                        IP_reads_count = IP_reads_count + IP_back_read;
                        count++;
                    }
                    real_count++;

                }
                MCMCIteration modeltest = new MCMCIteration();
                modeltest.SetCondition(IP_reads_count, Input_reads_count, XB, XP, YB, YP);
                double initial_p1 = unifromdi.sample();
                double initial_p2 = unifromdi.sample();
                double initial_p3 = unifromdi.sample();
                double initial_pm = unifromdi.sample();

                LinkedList<Double> resultList = modeltest.SamplingIteration(initial_p1, initial_p2, initial_p3, initial_pm, iteration_time, burn_in_time);
                peak.AddPM_MCMC_List(resultList);
                YB = null;
                XB = null;
                YP.clear();
                XP.clear();
            }
            peak.calculatePM_mean();
        }
        background_index.clear();
    }

    public static void ResultPrinter(FileWriter fw, LinkedList<QuantifyPeakRecord> RecordList, String geneID, String geneName) {
//        System.out.println(RecordList.size());
        try {
            for (Iterator<QuantifyPeakRecord> iter = RecordList.iterator(); iter.hasNext(); ) {
                QuantifyPeakRecord record = iter.next();
                String chr = record.getChr();
                int start = record.getPeakStart();
                int end = record.getPeakEnd();
                double PM_mean = record.getPM_mean();
//                System.out.println(PM_mean);
                fw.write(chr + "\t" + start + "\t" + end + "\t" + geneID + ";" + geneName + "\t" + PM_mean + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static LinkedList OnePeakMCMC(QuantifyPeakRecord peak, boolean isTreat, QuantifyGeneRecord Gene, ArrayList<Integer> background_index, int iteration_time, int burn_in_time) {
        UniformRealDistribution unifromdi = new UniformRealDistribution(0, 1);
        LinkedList<Double> YP = new LinkedList<Double>();
        LinkedList<Double> YB = new LinkedList<>();
        LinkedList<Double> XP = new LinkedList<Double>();
        LinkedList<Double> XB = new LinkedList<>();

        LinkedList<Double> resultList = new LinkedList<>();

        ArrayList<ArrayList<Integer>> InputReadsCountList;
        ArrayList<ArrayList<Integer>> IPReadsCountList;
        ArrayList<ArrayList<Integer>> InputBackReadsCountList;
        ArrayList<ArrayList<Integer>> IPBackReadsCountList;
        if (isTreat) {
            InputReadsCountList = peak.getTreat_InputReadsCountList();
            IPReadsCountList = peak.getTreat_IPReadsCountList();
            InputBackReadsCountList = Gene.getTreat_InputbackgroundReadsList();
            IPBackReadsCountList = Gene.getTreat_IPbackgroundReadsList();
        } else {
            InputReadsCountList = peak.getInputReadsCountList();
            IPReadsCountList = peak.getIPReadsCountList();
            InputBackReadsCountList = Gene.getInputbackgroundReadsList();
            IPBackReadsCountList = Gene.getIPbackgroundReadsList();
        }

        for (int i = 0; i < InputReadsCountList.size(); i++) {
            double IP_reads_count = 0;
            double Input_reads_count = 0;
            ArrayList<Integer> IPBam_readsCount = IPReadsCountList.get(i);
            ArrayList<Integer> InputBam_readsCount = InputReadsCountList.get(i);
            ArrayList<Integer> IPBackground_readsCount = InputReadsCountList.get(i);
            ArrayList<Integer> InputBackground_readsCount = InputReadsCountList.get(i);
            if (InputBackReadsCountList.size() > i) {
                InputBackground_readsCount = InputBackReadsCountList.get(i);
                IPBackground_readsCount = IPBackReadsCountList.get(i);
            }
            for (int bin = 0; bin < IPBam_readsCount.size(); bin++) {
                int readsCount = IPBam_readsCount.get(bin);
                IP_reads_count = IP_reads_count + readsCount;
                XP.add((double) readsCount);
            }
            for (int bin = 0; bin < InputBam_readsCount.size(); bin++) {
                int readsCount = InputBam_readsCount.get(bin);
                Input_reads_count = Input_reads_count + readsCount;
                YP.add((double) readsCount);
            }

            XB = new LinkedList<Double>();
            int count = 0;
            int real_count = 0;


            YB = new LinkedList<Double>();
            while (count < YP.size() && count < XP.size() && real_count < background_index.size()) {
                double Input_back_read = InputBackground_readsCount.get(background_index.get(real_count));
                double IP_back_read = IPBackground_readsCount.get(background_index.get(real_count));

                if (Input_back_read != 0 && IP_back_read != 0) {
                    YB.add(Input_back_read);
                    XB.add(IP_back_read);
                    Input_reads_count = Input_reads_count + Input_back_read;
                    IP_reads_count = IP_reads_count + IP_back_read;
                    count++;
                }
                real_count++;

            }
            MCMCIteration modeltest = new MCMCIteration();
            modeltest.SetCondition(IP_reads_count, Input_reads_count, XB, XP, YB, YP);
            double initial_p1 = unifromdi.sample();
            double initial_p2 = unifromdi.sample();
            double initial_p3 = unifromdi.sample();
            double initial_pm = unifromdi.sample();
            LinkedList<Double> temp_resultList = modeltest.SamplingIteration(initial_p1, initial_p2, initial_p3, initial_pm, iteration_time, burn_in_time);
            resultList.addAll(temp_resultList);
            temp_resultList.clear();
            XP.clear();
            YP.clear();
            XB.clear();
            YB.clear();
//            while (true) {
//                temp_resultList.clear();
//                double receptance = modeltest.SamplingIteration(initial_p1, initial_p2, initial_p3, initial_pm, iteration_time, burn_in_time, variance, temp_resultList);
//                if (receptance < 0.2) {
//                    max = variance;
//                    if (min == 0) {
//                        variance = variance / divisor;
//                    } else {
//                        variance = (min + variance) / 2;
//                    }
//                } else if (receptance > 0.3) {
//                    min = variance;
//                    if (max == 1) {
//                        variance = variance * multiple;
//                    } else {
//                        variance = (max + variance) / 2;
//                    }
//                } else {
//                    break;
//                }
//            }
//            System.out.println("a peak");
//            resultList.addAll(temp_resultList);
        }
        return resultList;
    }



}
