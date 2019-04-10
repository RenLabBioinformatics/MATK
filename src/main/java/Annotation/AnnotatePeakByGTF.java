package Annotation;

import Basic.BED.BEDReader;
import Basic.BED.BEDRecord;
import Basic.BED.BEDWriter;
import Basic.GTF.ChromosomeRecord;
import Basic.GTF.GeneRecord;
import Basic.GTF.ReadGtfFile;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

public class AnnotatePeakByGTF {
    private HashMap<String, LinkedList<GeneAnnotationRecord>> geneAnnoRecMap;
    private GeneAnnotationRecordIntervalTree geneAnnoRecIntervalTree;

    public AnnotatePeakByGTF(File gtfFile) {
        ReadGtfFile readGtfFile = new ReadGtfFile();
        readGtfFile.ReadFromFile(gtfFile);
        readGtfFile.MergeGeneTranscript();
        HashMap<String, ChromosomeRecord> gtfAnnotationMap = readGtfFile.getChromosomeMap();
        //Setup gene annotation map
        geneAnnoRecMap = new HashMap<>();
        for(String chrName : gtfAnnotationMap.keySet()) {
            ChromosomeRecord chrRec = gtfAnnotationMap.get(chrName);
            LinkedList<GeneAnnotationRecord> geneAnnoRecList;
            if(geneAnnoRecMap.containsKey(chrName))
                geneAnnoRecList = geneAnnoRecMap.get(chrName);
            else {
                geneAnnoRecList = new LinkedList<>();
                geneAnnoRecMap.put(chrName, geneAnnoRecList);
            }
            LinkedList<GeneRecord> geneRecList = chrRec.getGeneList();
            for(GeneRecord geneRec : geneRecList) {
                GeneAnnotationRecord geneAnnoRec = new GeneAnnotationRecord(geneRec);
                geneAnnoRecList.add(geneAnnoRec);
            }
        }
        //Build gene annotation interval tree
        geneAnnoRecIntervalTree = new GeneAnnotationRecordIntervalTree(geneAnnoRecMap);
    }

    public void AnnotatePeaks(HashMap<String, LinkedList<BEDRecord>> peakRecMap) {
        //Assign peaks to gene annotation, discard peaks that were not annotated on any given gene.
        for(String chrName : peakRecMap.keySet()) {
            LinkedList<BEDRecord> peakRecList = peakRecMap.get(chrName);
            for(BEDRecord peakRec : peakRecList) {
                geneAnnoRecIntervalTree.AssignPeakToGeneAnnotation(peakRec);
            }
        }
        //Annotate each peak, and merge peaks that were separated by intron regions.
        for(String chrName : geneAnnoRecMap.keySet()) {
            LinkedList<GeneAnnotationRecord> geneAnnoRecList = geneAnnoRecMap.get(chrName);
            for(GeneAnnotationRecord geneAnnoRec : geneAnnoRecList) {
                geneAnnoRec.MergeBlock();
            }
        }
    }

    public HashMap<String, LinkedList<BEDRecord>> getAnnotatedPeaks() {
        HashMap<String, LinkedList<BEDRecord>> retPeakMap = new HashMap<>();
        for(String chrName : geneAnnoRecMap.keySet()) {
            LinkedList<BEDRecord> bedRecList = new LinkedList<>();
            retPeakMap.put(chrName, bedRecList);
            LinkedList<GeneAnnotationRecord> geneAnnoRecList = geneAnnoRecMap.get(chrName);
            for(GeneAnnotationRecord geneAnnoRec : geneAnnoRecList) {
                LinkedList<BEDRecord> associatedPeakList = geneAnnoRec.getAssociatedPeakList();
                for(BEDRecord peakRec : associatedPeakList) {
                    bedRecList.add(peakRec);
                }
            }
        }
        return retPeakMap;
    }

    public static void main(String[] args) {
        BEDReader bedReader = new BEDReader(new File("E:\\工作文档\\MATK\\PeakCalling\\Huh7\\IP-Input.bed"));
        HashMap<String, LinkedList<BEDRecord>> peakRecMap = bedReader.getBEDChromosomeMap();

        AnnotatePeakByGTF annotatePeakByGTF = new AnnotatePeakByGTF(new File("E:\\Genome\\Human\\hg38\\Homo_sapiens.GRCh38.94.gtf"));
        annotatePeakByGTF.AnnotatePeaks(peakRecMap);
        HashMap<String, LinkedList<BEDRecord>> annoPeakRecMap = annotatePeakByGTF.getAnnotatedPeaks();

        BEDWriter bedWriter = new BEDWriter(annoPeakRecMap);
        bedWriter.SaveInFullBED(new File("E:\\工作文档\\MATK\\PeakCalling\\Huh7\\IP-Input_BLOCK.bed"));
    }
}
