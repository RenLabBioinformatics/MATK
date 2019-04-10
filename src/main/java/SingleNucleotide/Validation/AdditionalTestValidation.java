package SingleNucleotide.Validation;

import SingleNucleotide.CNN.CNNModel;
import SingleNucleotide.Data.CNNCSVReader;
import SingleNucleotide.Data.DataPair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class AdditionalTestValidation {
    private CNNModel cnnModel;
    private LinkedList<DataPair> positiveTestDataList, negativeTestDataList;
    private LinkedList<Double> positiveScoreList, negativeScoreList;
    private LinkedList<Performance> performanceList;

    public AdditionalTestValidation(File modelFile, File testDataFile) {
        cnnModel = new CNNModel();
        cnnModel.LoadModelFromFile(modelFile);

        CNNCSVReader cnncsvReader = new CNNCSVReader(testDataFile);
        positiveTestDataList = cnncsvReader.getPositiveDataList();
        negativeTestDataList = cnncsvReader.getNegativeDataList();
    }

    public void Validation(int channels) {
        positiveScoreList = new LinkedList<>();
        negativeScoreList = new LinkedList<>();
        //Predict positive test data
        for(DataPair dataPair : positiveTestDataList) {
            double score = cnnModel.Predict(dataPair.getFeatures(), channels);
            positiveScoreList.add(score);
        }
        //Predict negative test data
        for(DataPair dataPair : negativeTestDataList) {
            double score = cnnModel.Predict(dataPair.getFeatures(), channels);
            negativeScoreList.add(score);
        }
        //Calculate performance
        PerformanceCalculator perfCal = new PerformanceCalculator(positiveScoreList, negativeScoreList);
        perfCal.FullPerformance();
        System.out.println("AUC = " + perfCal.CalculateAUC());
        performanceList = perfCal.getPerformanceList();
    }

    public LinkedList<Double> getPositiveScoreList() {
        return positiveScoreList;
    }

    public LinkedList<Double> getNegativeScoreList() {
        return negativeScoreList;
    }

    public LinkedList<Performance> getPerformanceList() {
        return performanceList;
    }

    public void SavePerformance(File saveFile) {
        try {
            FileWriter fw = new FileWriter(saveFile);
            fw.write("Ac\tSn\tSp\tMcc\tPr\n");
            for(Performance perf : performanceList) {
                fw.write(perf + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        AdditionalTestValidation additionalTestValidation = new AdditionalTestValidation(new File("E:\\工作文档\\MATK\\SingleNucleotide\\TrainingResult\\Sequence\\Mouse_Sequence_Model"),
                new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\TrainingData\\Sequence\\Zebrafish_PSSM_Test.csv"));
        additionalTestValidation.Validation(1);
        additionalTestValidation.SavePerformance(new File("E:\\工作文档\\MATK\\SingleNucleotide\\TrainingResult\\Sequence\\AdditionalTestPerformance_Mouse_Sequence.txt"));
    }
}
