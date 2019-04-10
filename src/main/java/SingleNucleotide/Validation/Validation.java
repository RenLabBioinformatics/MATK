package SingleNucleotide.Validation;

import SingleNucleotide.CNN.CNNModel;
import SingleNucleotide.Data.CNNCSVReader;
import SingleNucleotide.Data.DataPair;
import SingleNucleotide.Data.TrainingDataCreation;
import org.nd4j.linalg.dataset.DataSet;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by Ben on 2018/5/5.
 */
public class Validation {
    LinkedList<DataPair> positiveDataList, negativeDataList;
    LinkedList<Double> positiveScoreList, negativeScoreList;
    private File modelTopologyFile;

    public Validation(LinkedList<DataPair> positiveDataList, LinkedList<DataPair> negativeDataList, File modelTopologyFile) {
        this.positiveDataList = positiveDataList;
        this.negativeDataList = negativeDataList;
        this.modelTopologyFile = modelTopologyFile;
    }

    public void nFold(int fold, int repeat, int batchSize, int epoch, int channels) {
        positiveScoreList = new LinkedList<Double>();
        negativeScoreList = new LinkedList<Double>();

        for(int i=1; i<=repeat; i++) {
            Collections.shuffle(positiveDataList);
            Collections.shuffle(negativeDataList);
            //split train and test
            LinkedList<DataPair> positiveTrainList = new LinkedList<DataPair>();
            LinkedList<DataPair> positiveTestList = new LinkedList<DataPair>();
            LinkedList<DataPair> negativeTrainList = new LinkedList<DataPair>();
            LinkedList<DataPair> negativeTestList = new LinkedList<DataPair>();
            for(int curFold = 1; curFold <= fold; curFold++) {
                positiveTestList.clear();
                positiveTrainList.clear();
                negativeTestList.clear();
                negativeTrainList.clear();
                double startPercentage = (double)(curFold - 1)/(double)fold;
                double endPercentage = (double)(curFold)/(double)fold;
                int startPositiveIndex = (int)(positiveDataList.size() * startPercentage);
                int endPositiveIndex = (int)(positiveDataList.size() * endPercentage);
                int startNegativeIndex = (int)(negativeDataList.size() * startPercentage);
                int endNegativeIndex = (int)(negativeDataList.size() * endPercentage);

                int count = 0;
                for(DataPair dataPair : positiveDataList) {
                    if( (count >= startPositiveIndex) && (count < endPositiveIndex) )
                        positiveTestList.add(dataPair);
                    else
                        positiveTrainList.add(dataPair);
                    count++;
                }

                count = 0;
                for(DataPair dataPair : negativeDataList) {
                    if( (count >= startNegativeIndex) && (count < endNegativeIndex) )
                        negativeTestList.add(dataPair);
                    else
                        negativeTrainList.add(dataPair);
                    count++;
                }

                //Train and predict
                TrainingDataCreation trainingDataCreation = new TrainingDataCreation();
                LinkedList<DataSet> batchList =  trainingDataCreation.CreateBatchList(positiveTrainList, negativeTrainList, batchSize, channels);
                CNNModel cnnModelTraining = new CNNModel(modelTopologyFile);
                cnnModelTraining.BuildModel();
                cnnModelTraining.Train(batchList, epoch);

                for(DataPair dataPair : positiveTestList) {
                    double score = cnnModelTraining.Predict(dataPair.getFeatures(), channels);
                    positiveScoreList.add(score);
                }
                for(DataPair dataPair : negativeTestList) {
                    double score = cnnModelTraining.Predict(dataPair.getFeatures(), channels);
                    negativeScoreList.add(score);
                }
            }
        }
    }

    public void Self(int batchSize, int epoch, int channels) {
        positiveScoreList = new LinkedList<Double>();
        negativeScoreList = new LinkedList<Double>();

        TrainingDataCreation trainingDataCreation = new TrainingDataCreation();
        LinkedList<DataSet> batchList =  trainingDataCreation.CreateBatchList(positiveDataList, negativeDataList, batchSize, channels);
        CNNModel cnnModelTraining = new CNNModel(modelTopologyFile);
        cnnModelTraining.BuildModel();
        cnnModelTraining.Train(batchList, epoch);

        for(DataPair dataPair : positiveDataList) {
            double score = cnnModelTraining.Predict(dataPair.getFeatures(), channels);
            positiveScoreList.add(score);
        }
        for(DataPair dataPair : negativeDataList) {
            double score = cnnModelTraining.Predict(dataPair.getFeatures(), channels);
            negativeScoreList.add(score);
        }
    }

    public LinkedList<Double> getPositiveScoreList() {
        return positiveScoreList;
    }

    public LinkedList<Double> getNegativeScoreList() {
        return negativeScoreList;
    }

    public static void main(String[] args) {
        CNNCSVReader cnncsvReader = new CNNCSVReader(new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\TrainingData\\Sequence\\Mouse_mm10_Training.csv"));
        LinkedList<DataPair> positiveDataList = cnncsvReader.getPositiveDataList();
        LinkedList<DataPair> negativeDataList = cnncsvReader.getNegativeDataList();

        Validation validation = new Validation(positiveDataList, negativeDataList, new File("E:\\工作文档\\MATK\\SingleNucleotide\\OneHot_PSSM_Model.json"));
//        validation.nFold(4, 5,100,10);
        for(int i=4; i<=8; i=i+2) {
            System.out.println(i + "-fold validation.");

            validation.nFold(i, 1, 80, 50, 1);

            LinkedList<Double> positiveScoreList = validation.getPositiveScoreList();
            LinkedList<Double> negativeScoreList = validation.getNegativeScoreList();

            PerformanceCalculator performanceCalculator = new PerformanceCalculator(positiveScoreList, negativeScoreList);
            performanceCalculator.FullPerformance();
            performanceCalculator.SavePerformance(new File("E:\\工作文档\\MATK\\SingleNucleotide\\TrainingResult\\Sequence\\Mouse_nFold\\"+ i +"-fold.txt"));
        }
    }
}
