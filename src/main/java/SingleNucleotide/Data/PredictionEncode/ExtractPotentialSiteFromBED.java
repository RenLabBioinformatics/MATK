package SingleNucleotide.Data.PredictionEncode;

import Basic.BED.BEDRecord;
import Basic.Genome.GenomeTools;
import SingleNucleotide.Data.SiteRecord;
import org.biojava.nbio.genome.parsers.twobit.TwoBitParser;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractPotentialSiteFromBED {
    private HashMap<String, LinkedList<BEDRecord>> bedRecMap;
    private Pattern methylationMotif;
    private TwoBitParser twoBitParser;
    private LinkedList<PeakEncodeRecord> extractedSiteList;

    public ExtractPotentialSiteFromBED(HashMap<String, LinkedList<BEDRecord>> bedRecMap, File twoBitFile) {
        try {
            this.bedRecMap = bedRecMap;
            twoBitParser = new TwoBitParser(twoBitFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean IsContainsChromosome(String chrName, String[] chromosomes) {
        boolean retVal = false;
        for (int i = 0; i < chromosomes.length; i++) {
            if (chromosomes[i].equals(chrName)) {
                retVal = true;
                break;
            }
        }
        return retVal;
    }

    private void AddPotentialSites(BEDRecord bedRec, int upStream, int downStream) {
        try {
            PeakEncodeRecord peakEncodeRecord = new PeakEncodeRecord(bedRec);
            int startPos = bedRec.getChrStart();
            int endPos = bedRec.getChrEnd();
            for (int i = startPos; i <= endPos; i++) {
                twoBitParser.reset();
                //Sense strand
                String potentialSiteSeq = twoBitParser.loadFragment(i - 2 - upStream, upStream + 5 + downStream);
                Matcher matcher = methylationMotif.matcher(potentialSiteSeq);
                if(matcher.matches()) {
                    SiteRecord siteRecord = new SiteRecord();
                    siteRecord.setChrName(bedRec.getChrName());
                    siteRecord.setChrStart(i);
                    siteRecord.setChrEnd(i + 1);
                    siteRecord.setSequence(potentialSiteSeq);
                    siteRecord.setStrand(GenomeTools.SenseStrand);
                    peakEncodeRecord.AddPotentialSite(siteRecord);
                }
                //Anti-sense strand
                twoBitParser.reset();
                potentialSiteSeq = twoBitParser.loadFragment(i - 2 - downStream, upStream + 5 + downStream);
                potentialSiteSeq = GenomeTools.reverseSeq(potentialSiteSeq);
                matcher = methylationMotif.matcher(potentialSiteSeq);
                if(matcher.matches()) {
                    SiteRecord siteRecord = new SiteRecord();
                    siteRecord.setChrName(bedRec.getChrName());
                    siteRecord.setChrStart(i);
                    siteRecord.setChrEnd(i + 1);
                    siteRecord.setSequence(potentialSiteSeq);
                    siteRecord.setStrand(GenomeTools.AntiSenseStrand);
                    peakEncodeRecord.AddPotentialSite(siteRecord);
                }
            }
            extractedSiteList.add(peakEncodeRecord);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ExtractPotentialSites(int upStream, int downStream) {
        try {
            extractedSiteList = new LinkedList<>();

            String motif = "[A-Z]{" + upStream + "}[AGT][GA]AC[ACT][A-Z]{" + downStream + "}";//Construct DRACH motif
            methylationMotif = Pattern.compile(motif);
            LinkedList<BEDRecord> bedList;

            String[] chromosomes = twoBitParser.getSequenceNames();

            for (String chrName : bedRecMap.keySet()) {
                bedList = bedRecMap.get(chrName);
                if (IsContainsChromosome(chrName, chromosomes)) {
                    twoBitParser.setCurrentSequence(chrName);
                    System.out.println("Extract potential methylation sites from chromosome " + chrName);
                    for (BEDRecord bedRec : bedList) {
                        AddPotentialSites(bedRec, upStream, downStream);
                    }
                    twoBitParser.close();
                } else {
                    System.out.println("There are no chromosome " + chrName + " in the genome! Skipped this chromosome!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Close() {
        try {
            twoBitParser.closeParser();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LinkedList<PeakEncodeRecord> getExtractedSiteList() {
        return extractedSiteList;
    }
}
