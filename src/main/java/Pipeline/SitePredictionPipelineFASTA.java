package Pipeline;

import Basic.FASTA.FastaReader;
import Basic.FASTA.FastaRecord;
import SingleNucleotide.CNN.CNNModel;
import SingleNucleotide.Data.PredictionEncode.EncodePotentialSite;
import SingleNucleotide.Data.PredictionEncode.ExtractPotentialSiteFromFASTA;
import SingleNucleotide.Data.SiteRecord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class SitePredictionPipelineFASTA {
    private LinkedList<SiteResultRecord> siteResRecList;
    private LinkedList<FastaRecord> fasRecList;

    public SitePredictionPipelineFASTA(File fastaFile) {
        FastaReader fastaReader = new FastaReader(fastaFile, FastaReader.Nucleotide);
        fasRecList = fastaReader.getFastaRecList();
    }

    public void PredictSites(String species, String threshold) {
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
        System.out.println("Extracting potential DRACH sites from FASTA file...");
        ExtractPotentialSiteFromFASTA extractPotentialSiteFromFASTA = new ExtractPotentialSiteFromFASTA(fasRecList);
        extractPotentialSiteFromFASTA.ExtractPotentialSites(predictParam.getUpStream(), predictParam.getDownStream());
        LinkedList<SiteRecord> siteRecList = extractPotentialSiteFromFASTA.getExtractedSiteList();
        //Encode potential DRACH sites
        System.out.println("Encoding potential sites using only the primary sequence features...");
        EncodePotentialSite encodePotentialSite = new EncodePotentialSite(matrixResource);
        encodePotentialSite.setSiteRecList(siteRecList);
        encodePotentialSite.EnocdeOneHotPSSM();
        //Predict sites
        siteResRecList = new LinkedList<>();
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
        siteResRecList = new LinkedList<>();
        for(SiteRecord siteRec : siteRecList) {
            double predictionScore = cnnModel.Predict(siteRec.getFeatures(), 1);
            int index = 1;
            if (predictionScore >= cutoff) {
                //Positive m6A sites
                SiteResultRecord siteResRec = new SiteResultRecord();
                siteResRec.setChrName(siteRec.getChrName());
                siteResRec.setSiteName(siteRec.getSequence());
                siteResRec.setStartPos(siteRec.getChrStart());
                siteResRec.setEndPos(siteRec.getChrEnd());
                siteResRec.setScore(predictionScore);
                siteResRecList.add(siteResRec);
                index++;
            }
        }
    }

    public void SaveResultInTSV(File saveFile) {
        try {
            FileWriter fw = new FileWriter(saveFile);
            fw.write("Name\tPosition\tSequence\tScore\n");
            for(SiteResultRecord siteResRec : siteResRecList) {
                fw.write(siteResRec.getChrName() + "\t" + siteResRec.getStartPos() + "\t" + siteResRec.getSiteName() + "\t" + siteResRec.getScore() + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SitePredictionPipelineFASTA sitePredictionPipelineFASTA = new SitePredictionPipelineFASTA(new File("E:\\工作文档\\m6A\\实验相关\\HIV.fasta"));
        sitePredictionPipelineFASTA.PredictSites("Human", "High");
        sitePredictionPipelineFASTA.SaveResultInTSV(new File("E:\\工作文档\\m6A\\实验相关\\HIV.txt"));
    }
}
