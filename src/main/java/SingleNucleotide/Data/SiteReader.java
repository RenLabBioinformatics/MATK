package SingleNucleotide.Data;

import Basic.Genome.GenomeTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by Ben on 2018/4/29.
 */
public class SiteReader {
    private LinkedList<SiteRecord> siteRecList;
    private HashSet<String> siteSet;

    public SiteReader() {
        siteRecList = new LinkedList<SiteRecord>();
        siteSet = new HashSet<>();
    }

    public void ReadSites(File siteRecFile, int label, boolean isSkipFirstLine) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(siteRecFile));
            String strLine;
            String[] strArr;
            if(isSkipFirstLine)
                br.readLine();
            while (br.ready()) {
                strLine = br.readLine();
                strArr = strLine.split("\t");
                SiteRecord siteRecord = new SiteRecord();
                siteRecord.setChrName(strArr[0]);
                siteRecord.setChrStart(Integer.parseInt(strArr[1]));
                siteRecord.setChrEnd(Integer.parseInt(strArr[2]));
                if(strArr[3].equals("+"))
                    siteRecord.setStrand(1);
                else
                    siteRecord.setStrand(0);
                siteRecord.setSequence(strArr[4]);
                siteRecord.setLabel(label);
                String siteStr = siteRecord.getChrName() + "|" + siteRecord.getChrEnd() + "|" + GenomeTools.FormatStrand(siteRecord.getStrand());
                if(siteSet.contains(siteStr))
                    System.out.println("Site " + siteStr + " existing! Ignore it.");
                else {
                    siteSet.add(siteStr);
                    siteRecList.add(siteRecord);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LinkedList<SiteRecord> getSiteRecList() {
        return siteRecList;
    }
}
