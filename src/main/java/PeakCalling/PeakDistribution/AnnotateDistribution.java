package PeakCalling.PeakDistribution;

import Basic.GTF.*;
import Basic.Genome.GenomeTools;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class AnnotateDistribution {
    GeneIntervalTree geneIntervalTree;

    public AnnotateDistribution(File gtfFile) {
        //Reading gene set annotation from gtf file
        ReadGtfFile readGtfFile = new ReadGtfFile();
        readGtfFile.ReadFromFile(gtfFile);
        HashMap<String, ChromosomeRecord> chrRecMap = readGtfFile.getChromosomeMap();
        //Constructing gene interval tree
        geneIntervalTree = new GeneIntervalTree();
        for (String chrName : chrRecMap.keySet()) {
            geneIntervalTree.InsertGeneRecords(chrRecMap.get(chrName));
        }
    }

    private int ConvertFeatureType(String feature) {
        int retVal;
        if (feature.equalsIgnoreCase("exon"))
            retVal = 0;
        else if (feature.equalsIgnoreCase("five_prime_utr"))
            retVal = 1;
        else if (feature.equalsIgnoreCase("CDS"))
            retVal = 2;
        else if (feature.equalsIgnoreCase("start_codon"))
            retVal = 2;
        else if (feature.equalsIgnoreCase("three_prime_utr"))
            retVal = 3;
        else if (feature.equalsIgnoreCase("stop_codon"))
            retVal = 4;
        else if (feature.equalsIgnoreCase("utr"))
            retVal = 3;
        else
            retVal = 0;
        return retVal;
    }

    private double CalculateTranscriptLocation(int position, TranscriptRecord transcriptRecord, String feature) {
        LinkedList<FeatureRecord> featureTypeList = new LinkedList<>();
        LinkedList<FeatureRecord> featureRecords = transcriptRecord.getFeatureList();
        for (FeatureRecord featureRecord : featureRecords) {
            if (featureRecord.getFeature().equalsIgnoreCase(feature))
                featureTypeList.add(featureRecord);
        }

        double totalLen = 0;
        double curLen = 0;
        boolean isEnd = false;
        if (transcriptRecord.getStrand() == GenomeTools.SenseStrand) {
            Collections.sort(featureTypeList, new CompareFeatureRecord(true));//sort by ascending order
            for (FeatureRecord featureRecord : featureTypeList) {
                totalLen = totalLen + (featureRecord.getEnd() - featureRecord.getStart() + 1);
                if (!isEnd) {
                    if ((position >= featureRecord.getStart()) && (position <= featureRecord.getEnd())) {
                        curLen = curLen + (position - featureRecord.getStart() + 1);
                        isEnd = true;
                    } else {
                        curLen = curLen + (featureRecord.getEnd() - featureRecord.getStart() + 1);
                    }
                }
            }
        } else {
            Collections.sort(featureTypeList, new CompareFeatureRecord(false));//sort by ascending order
            for (FeatureRecord featureRecord : featureTypeList) {
                totalLen = totalLen + (featureRecord.getEnd() - featureRecord.getStart() + 1);
                if (!isEnd) {
                    if ((position >= featureRecord.getStart()) && (position <= featureRecord.getEnd())) {
                        curLen = curLen + (featureRecord.getEnd() - position + 1);
                        isEnd = true;
                    } else {
                        curLen = curLen + (featureRecord.getEnd() - featureRecord.getStart() + 1);
                    }
                }
            }
        }

        double location = (curLen / totalLen) * 100;
        if (feature.equalsIgnoreCase("CDS"))
            location = location + 100;
        if (feature.equalsIgnoreCase("three_prime_utr"))
            location = location + 200;

        return location;
    }

    //Return the location of a given position in an recorded transcript
    //The location is range from 0-1, representing the percentage distribution.
    public double AnnotateTranscriptLocation(String chrName, int position, int strand, boolean isConsiderStrand) {
        double retVal = -1;

        LinkedList<GeneRecord> overlappedGenes = geneIntervalTree.QueryOverlappedGene(chrName, position, strand, isConsiderStrand);
        int maxAnnotationType = -1;
        TranscriptRecord annotatedTranscript = null;
        FeatureRecord annotatedFeature = null;
        //AnnotateSite the most match transcript
        for (GeneRecord geneRec : overlappedGenes) {
            LinkedList<TranscriptRecord> transcriptRecList = geneRec.getTranscriptList();
            for (TranscriptRecord transcriptRecord : transcriptRecList) {
                LinkedList<FeatureRecord> featureRecList = transcriptRecord.getFeatureList();
                for (FeatureRecord featureRecord : featureRecList) {
                    int featureStart = featureRecord.getStart();
                    int featureEnd = featureRecord.getEnd();
                    if ((position >= featureStart) && (position <= featureEnd)) {
                        int tmpVal = ConvertFeatureType(featureRecord.getFeature());
                        if (tmpVal > maxAnnotationType) {
                            annotatedTranscript = transcriptRecord;
                            annotatedFeature = featureRecord;
                        }
                    }
                }
            }
        }
        //Calculate transcript location
        if (annotatedFeature != null) {
            String annotatedFeatureStr = annotatedFeature.getFeature();
            if (!annotatedFeatureStr.equalsIgnoreCase("exon")) {
                if(annotatedFeatureStr.equalsIgnoreCase("stop_codon"))
                    retVal = 200;
                else {
                    if (annotatedFeatureStr.equalsIgnoreCase("start_codon"))
                        annotatedFeatureStr = "CDS";
                    retVal = CalculateTranscriptLocation(position, annotatedTranscript, annotatedFeatureStr);
                }
            }
        }

        return retVal;
    }

    public static void main(String[] args) {
        AnnotateDistribution annotateDistribution = new AnnotateDistribution(new File("E:\\Genome\\Human\\hg19\\Homo_sapiens.GRCh37.87.chr.gtf"));
        double location = annotateDistribution.AnnotateTranscriptLocation("chr9", 4860362,0,false);
        System.out.println(location);
    }
}
