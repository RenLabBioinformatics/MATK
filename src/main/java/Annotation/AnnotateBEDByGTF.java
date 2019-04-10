package Annotation;

import Basic.BED.BEDReader;
import Basic.BED.BEDRecord;
import Basic.BED.BEDWriter;
import Basic.GTF.ChromosomeRecord;
import Basic.GTF.GeneRecord;
import Basic.GTF.ReadGtfFile;
import PeakCalling.PeakDistribution.GeneIntervalTree;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class AnnotateBEDByGTF {
    private GeneIntervalTree geneIntervalTree;

    public AnnotateBEDByGTF(File gtfFile) {
        //Read annotation from gtf file
        ReadGtfFile readGtfFile = new ReadGtfFile();
        readGtfFile.ReadFromFile(gtfFile);
        HashMap<String, ChromosomeRecord> chrRecMap = readGtfFile.getChromosomeMap();
        //Build gene interval tree
        geneIntervalTree = new GeneIntervalTree();
        for(String chrName : chrRecMap.keySet()) {
            geneIntervalTree.InsertGeneRecords(chrRecMap.get(chrName));
        }
    }

    public void AnnotateSite(HashMap<String, LinkedList<BEDRecord>> bedRecMap) {
        for(String chrName : bedRecMap.keySet()) {
            LinkedList<BEDRecord> bedRecList = bedRecMap.get(chrName);
            for(Iterator<BEDRecord> itr = bedRecList.iterator(); itr.hasNext();) {
                BEDRecord bedRecord = itr.next();
                LinkedList<GeneRecord> overlappedGenes = geneIntervalTree.QueryOverlappedGene(chrName, bedRecord.getChrEnd(), bedRecord.getStrand(), false);
                if(!overlappedGenes.isEmpty()) {
                    GeneRecord bestMatchGene = overlappedGenes.getFirst();
                    for (GeneRecord geneRecord : overlappedGenes) {
                        if (geneRecord.getBioType().equalsIgnoreCase("protein_coding"))
                            bestMatchGene = geneRecord;
                    }
                    if(bedRecord.getStrand() == bestMatchGene.getStrand()) {
                        String bedRecName =  bestMatchGene.getGeneName() + "_" + bestMatchGene.getGeneId();
                        bedRecord.setName(bedRecName);
                    } else {
                        itr.remove();
                    }
                } else {
                    //Did not match any known gene
                    String bedRecName = "Unknown";
                    bedRecord.setName(bedRecName);
                }
            }
        }
    }

    public void AnnotatePeak(HashMap<String, LinkedList<BEDRecord>> bedRecMap) {
        for(String chrName : bedRecMap.keySet()) {
            LinkedList<BEDRecord> bedRecList = bedRecMap.get(chrName);
            for(Iterator<BEDRecord> itr = bedRecList.iterator(); itr.hasNext();) {
                BEDRecord bedRecord = itr.next();
                LinkedList<GeneRecord> overlappedGenes = geneIntervalTree.QueryOverlappedGene(chrName, bedRecord.getChrStart(), bedRecord.getChrEnd(), bedRecord.getStrand(), false);
                if(!overlappedGenes.isEmpty()) {
                    GeneRecord bestMatchGene = overlappedGenes.getFirst();
                    for (GeneRecord geneRecord : overlappedGenes) {
                        if (geneRecord.getBioType().equalsIgnoreCase("protein_coding"))
                            bestMatchGene = geneRecord;
                    }
                    bedRecord.setStrand(bestMatchGene.getStrand());
                    String bedRecName =  bestMatchGene.getGeneName() + "_" + bestMatchGene.getGeneId();
                    bedRecord.setName(bedRecName);
                } else {
                    itr.remove();
                }
            }
        }
    }

    public static void main(String[] args) {
        BEDReader bedReader = new BEDReader(new File("E:\\工作文档\\MATK\\PeakCalling\\Huh7\\IP-Input.bed"));
        HashMap<String, LinkedList<BEDRecord>> bedRecMap = bedReader.getBEDChromosomeMap();

        AnnotateBEDByGTF annotateBEDByGTF = new AnnotateBEDByGTF(new File("E:\\Genome\\Human\\hg38\\Homo_sapiens.GRCh38.94.gtf"));
        annotateBEDByGTF.AnnotatePeak(bedRecMap);

        BEDWriter bedWriter = new BEDWriter(bedRecMap);
        bedWriter.SaveInFullBED(new File("E:\\工作文档\\MATK\\PeakCalling\\Huh7\\IP-Input_Annotated.bed"));
    }
}
