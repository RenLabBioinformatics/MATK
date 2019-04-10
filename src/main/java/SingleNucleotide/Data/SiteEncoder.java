package SingleNucleotide.Data;

import SingleNucleotide.Data.MeRIP.MeRIPAverageEncoder;
import SingleNucleotide.Data.PSSM.PSSMReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by Ben on 2018/5/2.
 */
public class SiteEncoder {
    private LinkedList<SiteRecord> siteRecList;
    private LinkedList<EncodeRecord> encodeList;
    private PSSMReader pssmReader;
    private MeRIPAverageEncoder meRIPAverageEncoder;

    public SiteEncoder(LinkedList<SiteRecord> siteRecList) {
        this.siteRecList = siteRecList;
    }

    public SiteEncoder(LinkedList<SiteRecord> siteRecList, File matrixFile) {
        this.siteRecList = siteRecList;
        pssmReader = new PSSMReader(matrixFile);
    }

    public SiteEncoder(LinkedList<SiteRecord> siteRecList, File matrixFile, File[] ipBAMFiles, File[] inputBAMFiles) {
        this.siteRecList = siteRecList;
        pssmReader = new PSSMReader(matrixFile);
        meRIPAverageEncoder = new MeRIPAverageEncoder(siteRecList);
        meRIPAverageEncoder.setIPBAMFiles(ipBAMFiles);
        meRIPAverageEncoder.setInputBAMFiles(inputBAMFiles);
    }

    private double[][] EncodeOneHot(String sequence) {
        double[][] retVal = new double[4][sequence.length()];
        for(int i=0; i<sequence.length(); i++) {
            switch (sequence.charAt(i)) {
                case 'A':
                    retVal[0][i] = 1;
                    break;
                case 'T':
                    retVal[1][i] = 1;
                    break;
                case 'C':
                    retVal[2][i] = 1;
                    break;
                case 'G':
                    retVal[3][i] = 1;
                    break;
                default:
                    retVal[0][i] = 1;
            }
        }
        return retVal;
    }

    private double[][] EncodeOneHotPSSM(String sequence) {
        double[][] retVal = new double[4][sequence.length()];
        for(int i=0; i<sequence.length(); i++) {
            switch (sequence.charAt(i)) {
                case 'A':
                    retVal[0][i] = pssmReader.getPSSMValue('A', i);
                    break;
                case 'T':
                    retVal[1][i] = pssmReader.getPSSMValue('T', i);
                    break;
                case 'C':
                    retVal[2][i] = pssmReader.getPSSMValue('C', i);
                    break;
                case 'G':
                    retVal[3][i] = pssmReader.getPSSMValue('G', i);
                    break;
                default:
                    retVal[0][i] = pssmReader.getPSSMValue('A', i);
            }
        }
        return retVal;
    }

    private void EncodeOneHotPSSMMeRIP(int upstream, int downstream) {
        meRIPAverageEncoder.EncodeSites(upstream, downstream);
        for(SiteRecord siteRecord : siteRecList) {
            String sequence = siteRecord.getSequence();
            double[][] retVal = new double[4][sequence.length()*2];
            //Encode PSSM
            for(int i=0; i<sequence.length(); i++) {
                switch (sequence.charAt(i)) {
                    case 'A':
                        retVal[0][i] = pssmReader.getPSSMValue('A', i);
                        break;
                    case 'T':
                        retVal[1][i] = pssmReader.getPSSMValue('T', i);
                        break;
                    case 'C':
                        retVal[2][i] = pssmReader.getPSSMValue('C', i);
                        break;
                    case 'G':
                        retVal[3][i] = pssmReader.getPSSMValue('G', i);
                        break;
                    default:
                        retVal[0][i] = pssmReader.getPSSMValue('A', i);
                }
            }
            //Encode MeRIP-seq
            double[] tmpVec = siteRecord.getMeripFeatures();
            int index = sequence.length();
            for(int i=0; i<sequence.length(); i++) {
                switch (sequence.charAt(i)) {
                    case 'A':
                        retVal[0][index] = tmpVec[i];
                        break;
                    case 'T':
                        retVal[1][index] = tmpVec[i];
                        break;
                    case 'C':
                        retVal[2][index] = tmpVec[i];
                        break;
                    case 'G':
                        retVal[3][index] = tmpVec[i];
                        break;
                    default:
                        retVal[0][index] = tmpVec[i];
                }
                index++;
            }
            siteRecord.setFeatures(retVal);
        }
    }

    public void EncodeOneHot() {
        encodeList = new LinkedList<EncodeRecord>();
        for(SiteRecord siteRecord : siteRecList) {
            double[][] encodeVec = EncodeOneHot(siteRecord.getSequence());
            EncodeRecord encodeRecord = new EncodeRecord(encodeVec, siteRecord.getLabel());
            encodeList.add(encodeRecord);
        }
    }

    public void EncodeOneHotPSSM() {
        encodeList = new LinkedList<EncodeRecord>();
        for(SiteRecord siteRecord : siteRecList) {
            double[][] encodeVec = EncodeOneHotPSSM(siteRecord.getSequence());
            EncodeRecord encodeRecord = new EncodeRecord(encodeVec, siteRecord.getLabel());
            encodeList.add(encodeRecord);
        }
    }

    public LinkedList<EncodeRecord> getEncodeList() {
        return encodeList;
    }

    public void EncodeOneHotToCSV(File saveFile) {
        try {
            FileWriter fw = new FileWriter(saveFile);
            for(SiteRecord siteRecord : siteRecList) {
                double[][] feature = EncodeOneHot(siteRecord.getSequence());
                for(int i=0; i<feature.length; i++) {
                    fw.write("[");
                    for(int j=0; j<feature[i].length; j++) {
                        fw.write(feature[i][j] + " ");
                    }
                    fw.write("],");
                }
                fw.write(siteRecord.getLabel() + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void EncodeOneHotPSSMToCSV(File saveFile) {
        try {
            FileWriter fw = new FileWriter(saveFile);
            for(SiteRecord siteRecord : siteRecList) {
                double[][] feature = EncodeOneHotPSSM(siteRecord.getSequence());
                for(int i=0; i<feature.length; i++) {
                    fw.write("[");
                    for(int j=0; j<feature[i].length; j++) {
                        fw.write(feature[i][j] + " ");
                    }
                    fw.write("],");
                }
                fw.write(siteRecord.getLabel() + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void EncodeOneHotPSSMMeRIPToCSV(File saveFile, int upStream, int downStream) {
        try {
            FileWriter fw = new FileWriter(saveFile);
            EncodeOneHotPSSMMeRIP(upStream, downStream);
            for(SiteRecord siteRecord : siteRecList) {
                double[][] feature = siteRecord.getFeatures();
                for(int i=0; i<feature.length; i++) {
                    fw.write("[");
                    for(int j=0; j<feature[i].length; j++) {
                        fw.write(feature[i][j] + " ");
                    }
                    fw.write("],");
                }
                fw.write(siteRecord.getLabel() + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SiteReader siteReader = new SiteReader();
        siteReader.ReadSites(new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\TrainingData\\Sequence\\Mouse_mm10_Positive.txt"),
                1,
                true);
        siteReader.ReadSites(new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\TrainingData\\Sequence\\Mouse_mm10_Negative.txt"),
                0,
                true);

//        File[] ipBAMFiles = new File[4];
//        ipBAMFiles[0] = new File("E:\\工作文档\\MATK\\BAM\\Mouse\\Liver\\SRR2120870_sort_uniq.bam");
//        ipBAMFiles[1] = new File("E:\\工作文档\\MATK\\BAM\\Mouse\\Liver\\SRR2120871_sort_uniq.bam");
//        ipBAMFiles[2] = new File("E:\\工作文档\\MATK\\BAM\\Mouse\\Liver\\SRR2120872_sort_uniq.bam");
//        ipBAMFiles[3] = new File("E:\\工作文档\\MATK\\BAM\\Mouse\\Liver\\SRR2120873_sort_uniq.bam");
//
//        File[] inputBAMFiles = new File[4];
//        inputBAMFiles[0] = new File("E:\\工作文档\\MATK\\BAM\\Mouse\\Liver\\SRR2120874_sort_uniq.bam");
//        inputBAMFiles[1] = new File("E:\\工作文档\\MATK\\BAM\\Mouse\\Liver\\SRR2120864_sort_uniq.bam");
//        inputBAMFiles[2] = new File("E:\\工作文档\\MATK\\BAM\\Mouse\\Liver\\SRR2120865_sort_uniq.bam");
//        inputBAMFiles[3] = new File("E:\\工作文档\\MATK\\BAM\\Mouse\\Liver\\SRR2120866_sort_uniq.bam");

        SiteEncoder siteEncoder = new SiteEncoder(siteReader.getSiteRecList(),
                new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\TrainingData\\Sequence\\Mouse_PSSM.txt"));
        siteEncoder.EncodeOneHotPSSMToCSV(new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\TrainingData\\Sequence\\Mouse_mm10_Training.csv"));
    }
}
