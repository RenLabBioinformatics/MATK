package SingleNucleotide.Data.MeRIP;

import java.io.*;

public class MergeFile {
    public static void Merge(File...mergeFiles) {
        if(mergeFiles.length <= 2) {
            System.out.println("The number of files is too small.");
            return;
        }
        try {
            FileWriter fw = new FileWriter(mergeFiles[mergeFiles.length - 1]);
            for(int i=0; i<mergeFiles.length - 1; i++) {
                BufferedReader br = new BufferedReader(new FileReader(mergeFiles[i]));
                String strLine;
                while(br.ready()) {
                    strLine = br.readLine();
                    fw.write(strLine + "\n");
                }
                br.close();
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        MergeFile.Merge(new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\TrainingData\\MeRIPSeq\\Mouse\\Brain_Training.csv"),
                new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\TrainingData\\MeRIPSeq\\Mouse\\Liver_Training.csv"),
                new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\TrainingData\\MeRIPSeq\\Mouse\\Mouse_Combined.csv"));
    }
}
