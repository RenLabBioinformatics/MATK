package SingleNucleotide.Data;

import java.io.File;

public class TrainingDataPreparationConsole {
    public static void main(String[] args) {
        if(args[0].equals("-h") || args[0].equals("--help") || args[0].equals("-help") || args[0].equals("--h")) {
            System.out.println("-MeRIP PositiveFile NegativeFile IP_BAM_Files Input_BAM_Files PSSM_File SaveFile upstream downstream");
            System.out.println("Multiple BAM files can be separated by comma.");
        } else if(args[0].equals("-MeRIP")) {
            SiteReader siteReader = new SiteReader();
            siteReader.ReadSites(new File(args[1]), 1, true);
            siteReader.ReadSites(new File(args[2]),0,true);

            String[] ipFilePathArr = args[3].split(",");
            File[] ipFileArr = new File[ipFilePathArr.length];
            for(int i=0; i<ipFileArr.length; i++) {
                ipFileArr[i] = new File(ipFilePathArr[i]);
            }

            String[] inputFilePathArr = args[4].split(",");
            File[] inputFileArr = new File[inputFilePathArr.length];
            for(int i=0; i<inputFileArr.length; i++) {
                inputFileArr[i] = new File(inputFilePathArr[i]);
            }

            SiteEncoder siteEncoder = new SiteEncoder(siteReader.getSiteRecList(), new File(args[5]), ipFileArr, inputFileArr);
            siteEncoder.EncodeOneHotPSSMMeRIPToCSV(new File(args[6]), Integer.parseInt(args[7]), Integer.parseInt(args[8]));

            System.out.println("Encoding finished!");
        } else {
            System.out.println("Command line error! Please enter -h to check the command.");
        }
    }
}
