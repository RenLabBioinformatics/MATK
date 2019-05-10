package Pipeline;

import Annotation.AnnotateBEDByGTF;
import Basic.BED.BEDReader;
import Basic.BED.BEDRecord;
import Basic.Genome.GenomeTools;
import SingleNucleotide.CNN.CNNModel;
import SingleNucleotide.Data.PredictionEncode.EncodePotentialSite;
import SingleNucleotide.Data.PredictionEncode.ExtractPotentialSiteFromBED;
import SingleNucleotide.Data.PredictionEncode.PeakEncodeRecord;
import SingleNucleotide.Data.SiteRecord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

public class SitePredictionPipeline {
    private HashMap<String, LinkedList<BEDRecord>> bedRecMap;
    private HashMap<String, LinkedList<BEDRecord>> siteRecBEDMap;

    public SitePredictionPipeline(HashMap<String, LinkedList<BEDRecord>> bedRecMap) {
        this.bedRecMap = bedRecMap;
    }

    public SitePredictionPipeline(File bedFile) {
        BEDReader bedReader = new BEDReader(bedFile);
        bedRecMap = bedReader.getBEDChromosomeMap();
    }

    public void PredictSites(String species, String threshold, File genomeFile) {
        String matrixResource;
        SitePredictionParameter predictParam;
        if (species.equalsIgnoreCase("Mouse")) {
            predictParam = new SitePredictionParameter("/Mouse_PSSM_Param.txt");
            matrixResource = "/Mouse_PSSM.txt";
        } else if (species.equalsIgnoreCase("Human")) {
            predictParam = new SitePredictionParameter("/Human_PSSM_Param.txt");
            matrixResource = "/Human_PSSM.txt";
        } else {
            predictParam = new SitePredictionParameter("/Human_PSSM_Param.txt");
            matrixResource = "/Human_PSSM.txt";
        }
        //Extract potential DRACH sites
        System.out.println("Extracting potential DRACH sites from BED file...");
        ExtractPotentialSiteFromBED extractPotentialSiteFromBED = new ExtractPotentialSiteFromBED(bedRecMap, genomeFile);
        extractPotentialSiteFromBED.ExtractPotentialSites(predictParam.getUpStream(), predictParam.getDownStream());
        LinkedList<PeakEncodeRecord> peakEnocdeRecList = extractPotentialSiteFromBED.getExtractedSiteList();
        extractPotentialSiteFromBED.Close();
        //Encode potential DRACH sites
        System.out.println("Encoding potential sites using only the primary sequence features...");
        EncodePotentialSite encodePotentialSite = new EncodePotentialSite(peakEnocdeRecList, matrixResource);
        encodePotentialSite.EnocdeOneHotPSSM();
        //Predict sites
        siteRecBEDMap = new HashMap<>();

        System.out.println("Loading CNN model...");
        CNNModel cnnModel = new CNNModel();
        if (species.equalsIgnoreCase("Mouse"))
            cnnModel.LoadModelFromResource("/Mouse_OneHot_PSSM_Model");
        else if (species.equalsIgnoreCase("Human"))
            cnnModel.LoadModelFromResource("/Human_OneHot_PSSM_Model");
        else
            cnnModel.LoadModelFromResource("/Human_OneHot_PSSM_Model");

        double cutoff;
        if (threshold.equalsIgnoreCase("High"))
            cutoff = predictParam.getHighCutoff();
        else if (threshold.equalsIgnoreCase("Medium"))
            cutoff = predictParam.getMediumCutoff();
        else if (threshold.equalsIgnoreCase("Low"))
            cutoff = predictParam.getLowCutoff();
        else
            cutoff = predictParam.getHighCutoff();

        System.out.println("Begin prediction of single-nucleotide-resolution sites...");
        int index = 1;
        for (PeakEncodeRecord peakEncodeRecord : peakEnocdeRecList) {
            LinkedList<SiteRecord> siteRecList = peakEncodeRecord.getPotentialSiteList();
            for (SiteRecord siteRecord : siteRecList) {
                double predictionScore = cnnModel.Predict(siteRecord.getFeatures(), 1);
                if (predictionScore >= cutoff) {
                    //Positive m6A site
                    String chrName = siteRecord.getChrName();
                    int startPos = siteRecord.getChrStart();
                    int endPos = siteRecord.getChrEnd();
                    String siteName = "Site" + index;
                    int strand = siteRecord.getStrand();
                    double score = predictionScore;
                    BEDRecord bedRec = new BEDRecord(chrName, startPos, endPos);
                    bedRec.setName(siteName);
                    bedRec.setStrand(strand);
                    bedRec.setScore(score);

                    LinkedList<BEDRecord> siteRecBEDList;
                    if (siteRecBEDMap.containsKey(chrName))
                        siteRecBEDList = siteRecBEDMap.get(chrName);
                    else {
                        siteRecBEDList = new LinkedList<>();
                        siteRecBEDMap.put(chrName, siteRecBEDList);
                    }
                    siteRecBEDList.add(bedRec);
                    index++;
                }
            }
        }
    }

    public void PredictSites(String species, String threshold, File genomeFile, File[] ipBAMFiles, File[] inputBAMFiles) {
        String matrixResource;
        SitePredictionParameter predictParam;
        if (species.equalsIgnoreCase("Mouse")) {
            predictParam = new SitePredictionParameter("/Mouse_PSSM_MeRIP_Param.txt");
            matrixResource = "/Mouse_PSSM.txt";
        } else if (species.equalsIgnoreCase("Human")) {
            predictParam = new SitePredictionParameter("/Human_PSSM_MeRIP_Param.txt");
            matrixResource = "/Human_PSSM.txt";
        } else {
            predictParam = new SitePredictionParameter("/Human_PSSM_MeRIP_Param.txt");
            matrixResource = "/Human_PSSM.txt";
        }
        //Extract potential DRACH sites
        System.out.println("Extracting potential DRACH sites from BED file...");
        ExtractPotentialSiteFromBED extractPotentialSiteFromBED = new ExtractPotentialSiteFromBED(bedRecMap, genomeFile);
        extractPotentialSiteFromBED.ExtractPotentialSites(predictParam.getUpStream(), predictParam.getDownStream());
        LinkedList<PeakEncodeRecord> peakEnocdeRecList = extractPotentialSiteFromBED.getExtractedSiteList();
        extractPotentialSiteFromBED.Close();
        //Encode potential DRACH sites
        System.out.println("Encoding potential sites using primary sequence features and MeRIP-seq features...");
        EncodePotentialSite encodePotentialSite = new EncodePotentialSite(peakEnocdeRecList, matrixResource, ipBAMFiles, inputBAMFiles);
        encodePotentialSite.EncodeOneHotPSSMMeRIP(predictParam.getUpStream(), predictParam.getDownStream());
        //Predict sites
        siteRecBEDMap = new HashMap<>();

        System.out.println("Loading CNN model...");
        CNNModel cnnModel = new CNNModel();
        if (species.equalsIgnoreCase("Mouse"))
            cnnModel.LoadModelFromResource("/Mouse_OneHot_PSSM_MeRIP_Model");
        else if (species.equalsIgnoreCase("Human"))
            cnnModel.LoadModelFromResource("/Human_OneHot_PSSM_MeRIP_Model");
        else
            cnnModel.LoadModelFromResource("/Human_OneHot_PSSM_MeRIP_Model");

        double cutoff;
        if (threshold.equalsIgnoreCase("High"))
            cutoff = predictParam.getHighCutoff();
        else if (threshold.equalsIgnoreCase("Medium"))
            cutoff = predictParam.getMediumCutoff();
        else if (threshold.equalsIgnoreCase("Low"))
            cutoff = predictParam.getLowCutoff();
        else
            cutoff = predictParam.getHighCutoff();

        System.out.println("Begin prediction of single-nucleotide-resolution sites...");
        int index = 1;
        for (PeakEncodeRecord peakEncodeRecord : peakEnocdeRecList) {
            LinkedList<SiteRecord> siteRecList = peakEncodeRecord.getPotentialSiteList();
            for (SiteRecord siteRecord : siteRecList) {
                double predictionScore = cnnModel.Predict(siteRecord.getFeatures(), 2);
                if (predictionScore >= cutoff) {
                    //Positive m6A site
                    String chrName = siteRecord.getChrName();
                    int startPos = siteRecord.getChrStart();
                    int endPos = siteRecord.getChrEnd();
                    String siteName = "Site" + index;
                    int strand = siteRecord.getStrand();
                    double score = predictionScore;
                    BEDRecord bedRec = new BEDRecord(chrName, startPos, endPos);
                    bedRec.setName(siteName);
                    bedRec.setStrand(strand);
                    bedRec.setScore(score);

                    LinkedList<BEDRecord> siteRecBEDList;
                    if (siteRecBEDMap.containsKey(chrName))
                        siteRecBEDList = siteRecBEDMap.get(chrName);
                    else {
                        siteRecBEDList = new LinkedList<>();
                        siteRecBEDMap.put(chrName, siteRecBEDList);
                    }
                    siteRecBEDList.add(bedRec);
                    index++;
                }
            }
        }
    }

    /*
    * AnnotateSite predicted sites and removed those have different annotated strand
    * */
    public void AnnotateSite(File gtfFile) {
        System.out.println("Annotating m6A sites by gene set annotation file...");
        AnnotateBEDByGTF annotateBEDByGTF = new AnnotateBEDByGTF(gtfFile);
        annotateBEDByGTF.AnnotateSite(siteRecBEDMap);
    }

    public HashMap<String, LinkedList<BEDRecord>> getSiteRecBEDMap() {
        return siteRecBEDMap;
    }

    public void SavePredictedSiteInBED(File bedFile) {
        System.out.println("Saving predicted sites in BED format...");
        try {
            FileWriter fw = new FileWriter(bedFile);
            for(String chrName : siteRecBEDMap.keySet()) {
                LinkedList<BEDRecord> bedRecList = siteRecBEDMap.get(chrName);
                for(BEDRecord bedRec : bedRecList) {
                    fw.write(bedRec.getChrName() + "\t" + bedRec.getChrStart() + "\t" + bedRec.getChrEnd() + "\t"
                            + bedRec.getName() + "\t" + bedRec.getScore() +
                            "\t" + GenomeTools.FormatStrand(bedRec.getStrand()) + "\n");
                }
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        File[] ipBAMFiles = new File[3];
        ipBAMFiles[0] = new File("E:\\工作文档\\MATK\\BAM\\Human\\A549\\SRR2120887_sort_uniq.bam");
        ipBAMFiles[1] = new File("E:\\工作文档\\MATK\\BAM\\Human\\A549\\SRR2120888_sort_uniq.bam");
        ipBAMFiles[2] = new File("E:\\工作文档\\MATK\\BAM\\Human\\A549\\SRR2120889_sort_uniq.bam");

        File[] inputBAMFiles = new File[3];
        inputBAMFiles[0] = new File("E:\\工作文档\\MATK\\BAM\\Human\\A549\\SRR2120890_sort_uniq.bam");
        inputBAMFiles[1] = new File("E:\\工作文档\\MATK\\BAM\\Human\\A549\\SRR2120891_sort_uniq.bam");
        inputBAMFiles[2] = new File("E:\\工作文档\\MATK\\BAM\\Human\\A549\\SRR2120892_sort_uniq.bam");

        SitePredictionPipeline sitePredictionPipeline = new SitePredictionPipeline(new File("E:\\工作文档\\MATK\\PeakCalling\\A549\\Combined.bed"));
        sitePredictionPipeline.PredictSites("Human", "High", new File("E:\\Genome\\Human\\hg19\\Homo_sapiens.GRCh37.chr.2bit"),
                ipBAMFiles, inputBAMFiles);

        sitePredictionPipeline.AnnotateSite(new File("E:\\Genome\\Human\\hg19\\Homo_sapiens.GRCh37.87.chr.gtf"));
        sitePredictionPipeline.SavePredictedSiteInBED(new File("E:\\工作文档\\MATK\\PeakCalling\\A549\\Combined_Sites_Annotated.bed"));
    }
}
