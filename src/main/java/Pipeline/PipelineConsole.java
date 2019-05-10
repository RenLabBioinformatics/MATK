package Pipeline;

import PeakCalling.PeakDistribution.AnnotateBEDDistrubution;
import Quantification.QuantifyMeRIPSeq.DifferentialAnalysis;
import Quantification.QuantifyMeRIPSeq.Quantify;

import java.io.*;
import java.util.HashMap;

public class PipelineConsole {
    public static void PrintHelp() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(PipelineConsole.class.getResourceAsStream("/help.txt")));
            String strLine;
            while(br.ready()) {
                strLine = br.readLine();
                System.out.println(strLine);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HashMap<String, String> getParameterMap(String[] args) {
        HashMap<String, String> paramMap = new HashMap<>();
        //Step up default value
        //default setting for peak calling
        paramMap.put("-q", "0.05");
        paramMap.put("-c", "2");
        paramMap.put("-technicalRep", "false");
        //default setting for single nucleotide-resolution prediction
        paramMap.put("-sp", "Human");
        paramMap.put("-t", "High");
        paramMap.put("-gtf", "null");
        paramMap.put("-mode", "MeRIP");
        //default setting for topology plot
        paramMap.put("-strand", "false");
        paramMap.put("-numPoint", "300");
        //default setting for quantification or differentiation
        paramMap.put("-iteration", "10000");
        paramMap.put("-burn_in", "9000");
        //Read user-defined value
        String key = "", value = "";
        boolean isKey = true;
        for(int i=1; i<args.length; i++) {
            if(isKey) {
                key = args[i];
                isKey = false;
            } else {
                value = args[i];
                paramMap.put(key, value);
                isKey = true;
            }
        }
        return paramMap;
    }

    private static File[] getBAMFiles(String pathStr) {
        String[] filePathArr = pathStr.split(";");
        File[] bamFiles = new File[filePathArr.length];
        for(int i=0; i<filePathArr.length; i++)
            bamFiles[i] = new File(filePathArr[i]);
        return bamFiles;
    }

    public static void main(String[] args) {
        if(args[0].equalsIgnoreCase("-h")) {
            PrintHelp();
        } else if(args[0].equalsIgnoreCase("-peakCalling")) {
            HashMap<String, String> paramMap = getParameterMap(args);
            String outputPath = paramMap.get("-out");
            String ip = paramMap.get("-ip");
            File[] ipBAMFiles = getBAMFiles(ip);
            String input = paramMap.get("-input");
            File[] inputBAMFiles = getBAMFiles(input);
            PeakCallingPipeline peakCallingPipeline = new PeakCallingPipeline();
            peakCallingPipeline.setIpBAMFiles(ipBAMFiles);
            peakCallingPipeline.setInputBAMFiles(inputBAMFiles);
            peakCallingPipeline.PeakCalling(outputPath, Double.parseDouble(paramMap.get("-q")),
                    Integer.parseInt(paramMap.get("-c")),
                    Boolean.parseBoolean(paramMap.get("-technicalRep")));
            String gtfFilePath = paramMap.get("-gtf");
            if(!gtfFilePath.equalsIgnoreCase("null")) {
                //Use gtf file to annotate peaks
                peakCallingPipeline.AnnotatePeakRecMap(new File(gtfFilePath));
                peakCallingPipeline.SaveAnnotatedPeakRecMap(new File(outputPath));
            } else
                peakCallingPipeline.SavePeakRecMap(new File(outputPath));
            System.out.println("Peak calling completed.");
        } else if(args[0].equalsIgnoreCase("-singleNucleotide")) {
            HashMap<String, String> paramMap = getParameterMap(args);
            String outputPath = paramMap.get("-out");
            String species = paramMap.get("-sp");
            String threshold = paramMap.get("-t");
            //Prediction of single nucleotide-resolution sites
            if(paramMap.get("-mode").equalsIgnoreCase("Fasta")) {
                File fastaFile = new File(paramMap.get("-fasta"));
                SitePredictionPipelineFASTA sitePredictionPipelineFASTA = new SitePredictionPipelineFASTA(fastaFile);
                sitePredictionPipelineFASTA.PredictSites(species, threshold);
                sitePredictionPipelineFASTA.SaveResultInTSV(new File(outputPath));
            } else {
                File bedFile = new File(paramMap.get("-bed"));
                File genomeFile = new File(paramMap.get("-2bit"));
                SitePredictionPipeline sitePredictionPipeline = new SitePredictionPipeline(bedFile);
                if (paramMap.get("-mode").equalsIgnoreCase("MeRIP")) {
                    String ip = paramMap.get("-ip");
                    File[] ipBAMFiles = getBAMFiles(ip);
                    String input = paramMap.get("-input");
                    File[] inputBAMFiles = getBAMFiles(input);
                    sitePredictionPipeline.PredictSites(species, threshold, genomeFile, ipBAMFiles, inputBAMFiles);
                } else if (paramMap.get("-mode").equalsIgnoreCase("Sequence")) {
                    sitePredictionPipeline.PredictSites(species, threshold, genomeFile);
                } else {
                    System.out.println("Unknown prediction mode set. Using MeRIP mode to predict m6A sites");
                    String ip = paramMap.get("-ip");
                    File[] ipBAMFiles = getBAMFiles(ip);
                    String input = paramMap.get("-input");
                    File[] inputBAMFiles = getBAMFiles(input);
                    sitePredictionPipeline.PredictSites(species, threshold, genomeFile, ipBAMFiles, inputBAMFiles);
                }
                String gtfFilePath = paramMap.get("-gtf");
                if (!gtfFilePath.equalsIgnoreCase("null"))//use gene set file to annotate and filter predicted sites
                    sitePredictionPipeline.AnnotateSite(new File(gtfFilePath));
                sitePredictionPipeline.SavePredictedSiteInBED(new File(outputPath));
            }
            System.out.println("Sites prediction completed.");
        } else if(args[0].equalsIgnoreCase("-topology")) {
            //Drawing a topology graph for m6A sites or peaks
            HashMap<String, String> paramMap = getParameterMap(args);
            File bedFile = new File(paramMap.get("-bed"));
            File gtfFile = new File(paramMap.get("-gtf"));
            AnnotateBEDDistrubution annotateBEDDistrubution = new AnnotateBEDDistrubution(bedFile, gtfFile);
            annotateBEDDistrubution.Annotate(Boolean.parseBoolean("-strand"));
            annotateBEDDistrubution.EstimateDensity(Integer.parseInt(paramMap.get("-numPoint")));
            annotateBEDDistrubution.SaveDensity(new File(paramMap.get("-out")));
        } else if(args[0].equalsIgnoreCase("-quantification")){
            HashMap<String, String> paramMap = getParameterMap(args);
            String bedFile = paramMap.get("-bed");
            String gtfFile = paramMap.get("-gtf");
            String IPbamFile = paramMap.get("-ip");
            String InputbamFile = paramMap.get("-input");
            int iteration = Integer.parseInt(paramMap.get("-iteration"));
            int burn_in_time = Integer.parseInt(paramMap.get("-burn_in"));
            String outputfile = paramMap.get("-out");
            Quantify quantify = new Quantify(IPbamFile,InputbamFile,bedFile,gtfFile,iteration,burn_in_time,outputfile);
            quantify.QuantifyProcess();
            System.out.println("Quantification completed.");
        } else if(args[0].equalsIgnoreCase("-diff")){
            HashMap<String, String> paramMap = getParameterMap(args);
            String ControlbedFile = paramMap.get("-control_bed");
            String TreatedbedFile = paramMap.get("-treated_bed");
            String gtfFile = paramMap.get("-gtf");
            String ControlIPbamFile = paramMap.get("-control_ip");
            String ControlInputbamFile = paramMap.get("-control_input");
            String treatedIPbamFile = paramMap.get("-treated_ip");
            String treatedInputbamFile = paramMap.get("-treated_input");
            int iteration = Integer.parseInt(paramMap.get("-iteration"));
            int burn_in_time = Integer.parseInt(paramMap.get("-burn_in"));
            String outputfile = paramMap.get("-out");
            DifferentialAnalysis differentialAnalysis = new DifferentialAnalysis(ControlIPbamFile,ControlInputbamFile,treatedIPbamFile,treatedInputbamFile,ControlbedFile,TreatedbedFile,gtfFile,iteration,burn_in_time,outputfile);
            differentialAnalysis.DifferentialAnalysisProcess();
            System.out.println("Differential analysis completed.");
        }else{
            System.out.println("Unknown command " + args[0] + ", please check the help page.");
            PrintHelp();
        }
    }
}
