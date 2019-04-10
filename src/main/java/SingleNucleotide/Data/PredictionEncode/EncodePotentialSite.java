package SingleNucleotide.Data.PredictionEncode;

import SingleNucleotide.Data.MeRIP.MeRIPAverageEncoder;
import SingleNucleotide.Data.PSSM.PSSMReader;
import SingleNucleotide.Data.SiteRecord;

import java.io.File;
import java.util.LinkedList;

public class EncodePotentialSite {
    private LinkedList<PeakEncodeRecord> peakEncodeRecList;
    private LinkedList<SiteRecord> siteRecList;
    private PSSMReader pssmReader;
    private MeRIPAverageEncoder meRIPAverageEncoder;

    public EncodePotentialSite(String matrixResource) {
        pssmReader = new PSSMReader(matrixResource);
    }

    public EncodePotentialSite(LinkedList<PeakEncodeRecord> peakEncodeRecList, String matrixResource) {
        this.peakEncodeRecList = peakEncodeRecList;
        pssmReader = new PSSMReader(matrixResource);
        //Convert peak record to site record
        siteRecList = new LinkedList<>();
        for(PeakEncodeRecord peakEncodeRecord : peakEncodeRecList) {
            LinkedList<SiteRecord> peakSiteList = peakEncodeRecord.getPotentialSiteList();
            for(SiteRecord siteRecord : peakSiteList) {
                siteRecList.add(siteRecord);
            }
        }
    }

    public EncodePotentialSite(LinkedList<PeakEncodeRecord> peakEncodeRecList, File matrixFile) {
        this.peakEncodeRecList = peakEncodeRecList;
        pssmReader = new PSSMReader(matrixFile);
        //Convert peak record to site record
        siteRecList = new LinkedList<>();
        for(PeakEncodeRecord peakEncodeRecord : peakEncodeRecList) {
            LinkedList<SiteRecord> peakSiteList = peakEncodeRecord.getPotentialSiteList();
            for(SiteRecord siteRecord : peakSiteList) {
                siteRecList.add(siteRecord);
            }
        }
    }

    public EncodePotentialSite(LinkedList<PeakEncodeRecord> peakEncodeRecList, String matrixResource, File[] ipBAMFiles, File[] inputBAMFiles){
        this.peakEncodeRecList = peakEncodeRecList;
        pssmReader = new PSSMReader(matrixResource);
        //Convert peak record to site record
        siteRecList = new LinkedList<>();
        for(PeakEncodeRecord peakEncodeRecord : peakEncodeRecList) {
            LinkedList<SiteRecord> peakSiteList = peakEncodeRecord.getPotentialSiteList();
            for(SiteRecord siteRecord : peakSiteList) {
                siteRecList.add(siteRecord);
            }
        }
        //Encode sites
        meRIPAverageEncoder = new MeRIPAverageEncoder(siteRecList);
        meRIPAverageEncoder.setIPBAMFiles(ipBAMFiles);
        meRIPAverageEncoder.setInputBAMFiles(inputBAMFiles);
    }

    public EncodePotentialSite(LinkedList<PeakEncodeRecord> peakEncodeRecList, File matrixFile, File[] ipBAMFiles, File[] inputBAMFiles) {
        this.peakEncodeRecList = peakEncodeRecList;
        pssmReader = new PSSMReader(matrixFile);
        //Convert peak record to site record
        siteRecList = new LinkedList<>();
        for(PeakEncodeRecord peakEncodeRecord : peakEncodeRecList) {
            LinkedList<SiteRecord> peakSiteList = peakEncodeRecord.getPotentialSiteList();
            for(SiteRecord siteRecord : peakSiteList) {
                siteRecList.add(siteRecord);
            }
        }
        //Encode sites
        meRIPAverageEncoder = new MeRIPAverageEncoder(siteRecList);
        meRIPAverageEncoder.setIPBAMFiles(ipBAMFiles);
        meRIPAverageEncoder.setInputBAMFiles(inputBAMFiles);
    }

    public void setSiteRecList(LinkedList<SiteRecord> siteRecList) {
        this.siteRecList = siteRecList;
    }

    public void EnocdeOneHotPSSM() {
        for(SiteRecord siteRecord : siteRecList) {
            String sequence = siteRecord.getSequence();
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
            siteRecord.setFeatures(retVal);
        }
    }

    public void EncodeOneHotPSSMMeRIP(int upstream, int downstream) {
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

    public LinkedList<PeakEncodeRecord> getPeakEncodeRecList() {
        return peakEncodeRecList;
    }
}
