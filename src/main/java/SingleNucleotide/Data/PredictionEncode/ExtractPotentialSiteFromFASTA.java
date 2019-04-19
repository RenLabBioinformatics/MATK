package SingleNucleotide.Data.PredictionEncode;

import Basic.FASTA.FastaRecord;
import SingleNucleotide.Data.SiteRecord;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractPotentialSiteFromFASTA {
    private LinkedList<FastaRecord> fastaRecList;
    private Pattern methylationMotif;
    private LinkedList<SiteRecord> extractedSiteList;

    public ExtractPotentialSiteFromFASTA(LinkedList<FastaRecord> fastaRecList) {
        this.fastaRecList = fastaRecList;
    }

    public void ExtractPotentialSites(int upStream, int downStream) {
        try {
            extractedSiteList = new LinkedList<>();

            String motif = "[A-Z]{" + upStream + "}[AGT][GA]AC[ACT][A-Z]{" + downStream + "}";//Construct DRACH motif
            methylationMotif = Pattern.compile(motif);

            int fragmentLen = upStream + downStream + 5;
            for(FastaRecord fasRec : fastaRecList) {
                String seq = fasRec.getSequence();
                for(int i=0; i<=(seq.length() - fragmentLen); i++) {
                    String fragment = seq.substring(i, i + fragmentLen);
                    Matcher motifMatcher = methylationMotif.matcher(fragment);
                    if(motifMatcher.matches()) {
                        SiteRecord siteRecord = new SiteRecord();
                        siteRecord.setChrName(fasRec.getName());
                        siteRecord.setChrStart(i + 1 + upStream + 2);
                        siteRecord.setChrEnd(i + 1 + upStream + 2);
                        siteRecord.setSequence(fragment);
                        extractedSiteList.add(siteRecord);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LinkedList<SiteRecord> getExtractedSiteList() {
        return extractedSiteList;
    }
}
