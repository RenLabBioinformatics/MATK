package Basic.Genome;

import java.io.*;
import java.util.HashSet;

public class FormatGenomicFile {
    public static String[] HumanEnsemblChrList = {"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","X","Y","MT"};
    public static String[] ZebrafishEnsemblChrList = {"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","MT"};
    public static String[] MouseEnsemblChrList = {"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19", "X", "Y", "MT"};

    public static void FormatEnsemblFASTA(File sourceFile, File targetFile, boolean isAddChr, boolean isRemoveOtherContig, String[] validChrArr) {
        try {
            HashSet<String> validChrSet = new HashSet<>();
            for(int i=0; i<validChrArr.length; i++) {
                validChrSet.add(validChrArr[i]);
            }

            BufferedReader br = new BufferedReader(new FileReader(sourceFile));
            FileWriter fw = new FileWriter(targetFile);

            String strLine;
            boolean isRemoveSeq = false;
            while (br.ready()) {
                strLine = br.readLine();
                if(strLine.startsWith(">")) {
                    String chrName = strLine.substring(1);
                    String[] chrNameArr = chrName.split("\\s+");
                    if(isAddChr) {
                        if(chrNameArr[0].equalsIgnoreCase("MT")) {
                            fw.write(">chrM\n");
                            System.out.println("Formatting " + chrName + " to chrM");
                            isRemoveSeq = false;
                        } else {
                            if(isRemoveOtherContig) {
                                if(validChrSet.contains(chrNameArr[0])) {
                                    fw.write(">chr" + chrNameArr[0] + "\n");
                                    System.out.println("Formatting " + chrName + " to chr" + chrNameArr[0]);
                                    isRemoveSeq = false;
                                } else {
                                    System.out.println("Removing " + chrName);
                                    isRemoveSeq = true;
                                }
                            } else {
                                fw.write(">chr" + chrNameArr[0] + "\n");
                                System.out.println("Formatting " + chrName + " to chr" + chrNameArr[0]);
                                isRemoveSeq = false;
                            }
                        }
                    } else {
                        if(isRemoveOtherContig) {
                            if(validChrSet.contains(chrNameArr[0])) {
                                fw.write(">" + chrNameArr[0] + "\n");
                                System.out.println("Formatting " + chrName + " to " + chrNameArr[0]);
                                isRemoveSeq = false;
                            } else {
                                System.out.println("Removing " + chrName);
                                isRemoveSeq = true;
                            }
                        } else {
                            fw.write(">" + chrNameArr[0] + "\n");
                            System.out.println("Formatting " + chrName + " to " + chrNameArr[0]);
                            isRemoveSeq = false;
                        }
                    }
                } else {
                    if(!isRemoveSeq)
                        fw.write(strLine + "\n");
                }
            }

            fw.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void FormatEnsemblGTF(File sourceFile, File targetFile, boolean isAddChr, boolean isRemoveOtherContig, String[] validaChrArr) {
        try {
            HashSet<String> validChrSet = new HashSet<>();
            for(int i=0; i<validaChrArr.length; i++) {
                validChrSet.add(validaChrArr[i]);
            }

            BufferedReader br = new BufferedReader(new FileReader(sourceFile));
            FileWriter fw = new FileWriter(targetFile);

            String strLine;
            while(br.ready()) {
                strLine = br.readLine();
                if(strLine.startsWith("#")) {
                    fw.write(strLine + "\n");
                } else {
                    String[] strArr = strLine.split("\t");
                    if(isRemoveOtherContig) {
                        if(validChrSet.contains(strArr[0])) {
                            if(isAddChr) {
                                if(strArr[0].equalsIgnoreCase("MT")) {
                                    fw.write("chrM");
                                    for(int i=1; i<strArr.length; i++)
                                        fw.write("\t" + strArr[i]);
                                    fw.write("\n");
                                } else
                                    fw.write("chr" + strLine + "\n");
                            } else {
                                fw.write(strLine + "\n");
                            }
                        }
                    } else {
                        if(isAddChr) {
                            if (strArr[0].equalsIgnoreCase("MT")) {
                                fw.write("chrM");
                                for (int i = 1; i < strArr.length; i++)
                                    fw.write("\t" + strArr[i]);
                                fw.write("\n");
                            } else
                                fw.write("chr" + strLine + "\n");
                        } else {
                            fw.write(strLine + "\n");
                        }
                    }
                }
            }

            br.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FormatGenomicFile.FormatEnsemblFASTA(new File("E:\\Genome\\Human\\hg38\\Homo_sapiens.GRCh38.94_Origin.fa"),
                new File("E:\\Genome\\Human\\hg38\\Homo_sapiens.GRCh38.fa"),
                false, true, FormatGenomicFile.HumanEnsemblChrList);

//        FormatGenomicFile.FormatEnsemblGTF(new File("E:\\Genome\\Human\\hg38\\Homo_sapiens.GRCh38.94.gtf"),
//                new File("E:\\Genome\\Human\\hg38\\Homo_sapiens.GRCh38.94.chr.gtf"), true, true, FormatGenomicFile.HumanEnsemblChrList);
    }
}
