package SingleNucleotide.Data.MeRIP;

import SingleNucleotide.Data.SiteRecord;

import java.io.File;
import java.util.LinkedList;

public class MeRIPAverageEncoder {
    private LinkedList<SiteRecord> siteRecList;
    private File[] ipBAMFiles, inputBAMFiles;
    private MeRIPEncoder[] meRIPEncoders;

    public MeRIPAverageEncoder(LinkedList<SiteRecord> siteRecList) {
            this.siteRecList = siteRecList;
    }

    public void setIPBAMFiles(File ... ipBAMFiles) {
        this.ipBAMFiles = ipBAMFiles;
    }

    public void setInputBAMFiles(File ... inputBAMFiles) {
        this.inputBAMFiles = inputBAMFiles;
    }

    public void EncodeSites(int upstream, int downstream) {
        if(ipBAMFiles.length != inputBAMFiles.length)
            System.err.println("IP sample did not match Input sample!");
        else {
            meRIPEncoders = new MeRIPEncoder[ipBAMFiles.length];
            for(int i = 0; i< meRIPEncoders.length; i++) {
                meRIPEncoders[i] = new MeRIPEncoder(ipBAMFiles[i], inputBAMFiles[i], siteRecList, upstream, downstream);
            }
        }
    }

    public LinkedList<SiteRecord> getSiteRecList() {
        return siteRecList;
    }
}
