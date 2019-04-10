package Pipeline;

import Annotation.AnnotatePeakByGTF;
import BAMProcess.ExtractIPInputBin;
import Basic.BED.BEDRecord;
import Basic.BED.BEDWriter;
import PeakCalling.CombineReplicates.CombinePipeline;
import PeakCalling.SingleSamplePeakCalling;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

public class PeakCallingPipeline {
    private HashMap<String, LinkedList<BEDRecord>> peakRecMap, annotatedPeakRecMap;
    private File[] ipBAMFiles, inputBAMFiles;

    public void setIpBAMFiles(File[] ipBAMFiles) {
        this.ipBAMFiles = ipBAMFiles;
    }

    public void setInputBAMFiles(File[] inputBAMFiles) {
        this.inputBAMFiles = inputBAMFiles;
    }

    public void PeakCalling(String outputFilePath, double q, int C, boolean isTechnicalReplicates) {
        if(ipBAMFiles.length != inputBAMFiles.length)
            System.err.println("Sample size for IP and Input did not match");
        CombinePipeline combinePipeline = new CombinePipeline();
        for(int i=0; i<ipBAMFiles.length; i++) {
            //Extract IP-Input feature
            ExtractIPInputBin extractIPInputBin = new ExtractIPInputBin(ipBAMFiles[i], inputBAMFiles[i], 25);
            File featureFile = new File(outputFilePath + ".csv");
            File locationFile = new File(outputFilePath + ".loc");
            extractIPInputBin.SavaIPInputFeature(featureFile, locationFile);
            //VAE peak calling
            SingleSamplePeakCalling singleSamplePeakCalling = new SingleSamplePeakCalling(featureFile, locationFile, q);
            if(ipBAMFiles.length == 1) {
                peakRecMap = singleSamplePeakCalling.getBEDRecMap();
            } else {
                combinePipeline.AddBEDRecordMap(singleSamplePeakCalling.getBEDRecMap());
            }
            //Delete temporary files
            featureFile.delete();
            locationFile.delete();
        }
        if(ipBAMFiles.length > 1) {
            System.out.println("Combining peaks from different replicates...");
            combinePipeline.Combine(C, isTechnicalReplicates);
            peakRecMap = combinePipeline.getCombinedBEDRecMap();
        }
    }

    public HashMap<String, LinkedList<BEDRecord>> getPeakRecMap() {
        return peakRecMap;
    }

    public void AnnotatePeakRecMap(File gtfFile) {
        AnnotatePeakByGTF annotatePeakByGTF = new AnnotatePeakByGTF(gtfFile);
        annotatePeakByGTF.AnnotatePeaks(peakRecMap);
        annotatedPeakRecMap = annotatePeakByGTF.getAnnotatedPeaks();
    }

    public void SavePeakRecMap(File saveFile) {
        try {
            FileWriter fw = new FileWriter(saveFile);
            for(String chrName : peakRecMap.keySet()) {
                LinkedList<BEDRecord> bedRecList = peakRecMap.get(chrName);
                for(BEDRecord bedRec : bedRecList) {
                    fw.write(chrName + "\t" + bedRec.getChrStart() + "\t" + bedRec.getChrEnd() + "\t" + bedRec.getName() + "\t" + bedRec.getScore() + "\n");
                }
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SaveAnnotatedPeakRecMap(File saveFile) {
        BEDWriter bedWriter = new BEDWriter(annotatedPeakRecMap);
        bedWriter.SaveInFullBED(saveFile);
    }

    public static void main(String[] args) {
        PeakCallingPipeline peakCallingPipeline = new PeakCallingPipeline();

        File[] ipBAMFiles = new File[1];
        ipBAMFiles[0] = new File("E:\\工作文档\\nanopore\\input.bam");
        File[] inputBAMFiles = new File[1];
        inputBAMFiles[0] = new File("E:\\工作文档\\nanopore\\ip.bam");
        peakCallingPipeline.setIpBAMFiles(ipBAMFiles);
        peakCallingPipeline.setInputBAMFiles(inputBAMFiles);

        peakCallingPipeline.PeakCalling("E:\\工作文档\\nanopore\\MATK_input_ip.bed", 0.05, 2, false);
        peakCallingPipeline.SavePeakRecMap(new File("E:\\工作文档\\nanopore\\MATK_input_ip.bed"));
    }
}
