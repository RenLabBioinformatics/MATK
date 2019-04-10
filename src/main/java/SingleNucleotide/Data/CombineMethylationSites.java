package SingleNucleotide.Data;

import Basic.BED.BEDReader;
import Basic.BED.BEDRecord;
import Basic.Genome.GenomeTools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by Ben on 2018/5/7.
 */
public class CombineMethylationSites {
    private HashSet<String> methySiteSet = new HashSet();
    private LinkedList<BEDRecord> combineList = new LinkedList();

    private void AddBedFile(File bedFile) {
        String methySiteStr;
        BEDReader bedReader = new BEDReader(bedFile);
        HashMap<String, LinkedList<BEDRecord>> bedRecMap = bedReader.getBEDChromosomeMap();
        for (String chrName : bedRecMap.keySet()) {
            LinkedList<BEDRecord> bedRecList = bedRecMap.get(chrName);
            for (BEDRecord bedRec : bedRecList) {
                methySiteStr = bedRec.getChrName() + "\t" + bedRec.getChrStart() + "\t" + bedRec.getChrEnd() + "\t";
                if (bedRec.getStrand() == GenomeTools.SenseStrand)
                    methySiteStr = methySiteStr + "+";
                else if (bedRec.getStrand() == GenomeTools.AntiSenseStrand)
                    methySiteStr = methySiteStr + "-";
                else
                    methySiteStr = methySiteStr + ".";

                if (!methySiteSet.contains(methySiteStr)) {
                    combineList.add(bedRec);
                    methySiteSet.add(methySiteStr);
                }
            }
        }
    }

    public void CombineBedFiles(String bedFilePath) {
        File bedPath = new File(bedFilePath);
        File[] bedFiles = bedPath.listFiles();
        for (int i = 0; i < bedFiles.length; i++) {
            AddBedFile(bedFiles[i]);
            System.out.println("Finished file path " + bedFiles[i].getAbsolutePath());
        }
    }

    public void CombineBedFiles(File ... bedFiles) {
        for (int i = 0; i < bedFiles.length; i++) {
            AddBedFile(bedFiles[i]);
            System.out.println("Finished file path " + bedFiles[i].getAbsolutePath());
        }
    }

    public void SaveCombineFile(String filePath) {
        try {
            FileWriter fw = new FileWriter(filePath);
            for (BEDRecord bedRec : combineList) {

                fw.write(bedRec.getChrName() + "\t" + bedRec.getChrStart() + "\t" + bedRec.getChrEnd() + "\t" + bedRec.getName() + "\t" + bedRec.getScore() + "\t");
                if (bedRec.getStrand() == GenomeTools.SenseStrand)
                    fw.write("+\n");
                else if (bedRec.getStrand() == GenomeTools.AntiSenseStrand)
                    fw.write("-\n");
                else
                    fw.write(".\n");

            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CombineMethylationSites combineMethySite = new CombineMethylationSites();
        combineMethySite.CombineBedFiles(new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\RawBED\\KeDataSet\\Brain_mm10.bed"),
                new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\RawBED\\KeDataSet\\Liver_mm10.bed"));
        combineMethySite.SaveCombineFile("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\RawBED\\KeDataSet\\KeStudyMouse.bed");
    }
}
