import BAMProcess.LocationFileReader;
import BAMProcess.LocationRecord;
import PeakCalling.BinResult;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by Ben on 2018/1/12.
 */
public class AutoEncoderPeakCalling {
    File trainingDataFile;
    private MultiLayerNetwork autoEncoderModel;
    private HashMap<String, LinkedList<BinResult>> binResMap;

    public AutoEncoderPeakCalling(File trainingDataFile) {
        this.trainingDataFile = trainingDataFile;

        Nd4j.getRandom().setSeed(12345);
        MultiLayerConfiguration multiLayerConfiguration = new NeuralNetConfiguration.Builder()
                .seed(12345)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam.Builder().learningRate(10e-2).build())
                .l2(1e-4)
                .list()
                .layer(0, new DenseLayer.Builder()
                        .activation(Activation.RELU)
                        .nIn(5).nOut(15)
                        .l1(0.002).l2(0.002)
                        .build())
                .layer(1,new DenseLayer.Builder()
                        .activation(Activation.RELU)
                        .nIn(15).nOut(15)
                        .dropOut(0.5)
                        .l1(0.002).l2(0.002)
                        .build())
                .layer(2, new OutputLayer.Builder()
                        .activation(Activation.RELU)
                        .lossFunction(LossFunctions.LossFunction.MSE)
                        .nIn(15).nOut(5)
                        .l1(0.002).l2(0.002)
                        .build())
                .pretrain(false).backprop(true).build();

        autoEncoderModel = new MultiLayerNetwork(multiLayerConfiguration);
        autoEncoderModel.init();
    }

    public void Train(int batchSize, int nEpoch) {
        try {
            int numLinesToSkip = 0;
            String delimiter = ",";
            RecordReader recordReader = new CSVRecordReader(numLinesToSkip, delimiter);
            recordReader.initialize(new FileSplit(trainingDataFile));
            DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, batchSize);
            autoEncoderModel.setListeners(new ScoreIterationListener(500));

            ArrayList<INDArray> features = new ArrayList<INDArray>();
            while(iterator.hasNext()) {
                DataSet dataSet = iterator.next();
                features.add(dataSet.getFeatureMatrix());
            }
//            autoEncoderModel.fit(features.get(0), features.get(0));
            for (int i = 1; i <= nEpoch; i++) {
                for(int j=0; j<features.size(); j++)
                    autoEncoderModel.fit(features.get(j), features.get(j));
            }
            recordReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void Predict(File locationFile, int batchSize) {
        try {
            //Construct result data structure
            binResMap = new HashMap<String, LinkedList<BinResult>>();
            //Read location file
            LocationFileReader locationFileReader = new LocationFileReader(locationFile);
            LinkedList<LocationRecord> locRecList = locationFileReader.getLocationList();
            Iterator<LocationRecord> locRecItr = locRecList.iterator();
            //Read training data (feature data)
            int numLinesToSkip = 0;
            String delimiter = ",";
            RecordReader recordReader = new CSVRecordReader(numLinesToSkip, delimiter);
            recordReader.initialize(new FileSplit(trainingDataFile));
            DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, batchSize);

            org.deeplearning4j.nn.layers.variational.VariationalAutoencoder vae = (org.deeplearning4j.nn.layers.variational.VariationalAutoencoder) autoEncoderModel.getLayer(0);
            while (iterator.hasNext()) {
                DataSet trainDataSet = iterator.next();
                INDArray features = trainDataSet.getFeatures();
                int nRow = features.rows();

                INDArray reconstructionError = vae.reconstructionLogProbability(features, 16);
                for (int i = 0; i < nRow; i++) {
                    INDArray feature = features.getRow(i);
                    double score = reconstructionError.getDouble(i);
                    if(feature.getDouble(2) > 0)
                        score = -1 * score;
                    LocationRecord locRec = locRecItr.next();
                    LinkedList<BinResult> binResList;
                    BinResult binRes = new BinResult(locRec.getChrName(), locRec.getIndex(), score);
                    if(binResMap.containsKey(locRec.getChrName())) {
                        binResList = binResMap.get(locRec.getChrName());
                        binResList.add(binRes);
                    } else {
                        binResList = new LinkedList<BinResult>();
                        binResList.add(binRes);
                        binResMap.put(locRec.getChrName(), binResList);
                    }
                }
            }
            recordReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, LinkedList<BinResult>> getBinResMap() {
        return binResMap;
    }

    public void Predict(String saveFile, int batchSize) {
        try {
            FileWriter fw = new FileWriter(saveFile);

            int numLinesToSkip = 0;
            String delimiter = ",";
            RecordReader recordReader = new CSVRecordReader(numLinesToSkip, delimiter);
            recordReader.initialize(new FileSplit(trainingDataFile));
            DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, batchSize);

            org.deeplearning4j.nn.layers.variational.VariationalAutoencoder vae = (org.deeplearning4j.nn.layers.variational.VariationalAutoencoder) autoEncoderModel.getLayer(0);
            while (iterator.hasNext()) {
                DataSet trainDataSet = iterator.next();
                INDArray features = trainDataSet.getFeatures();
                int nRow = features.rows();

                INDArray reconstructionError = vae.reconstructionLogProbability(features, 16);
                for (int i = 0; i < nRow; i++) {
                    INDArray feature = features.getRow(i);
                    double score = reconstructionError.getDouble(i);
                    fw.write("[" + feature.getDouble(0));
                    for (int j = 1; j < feature.columns(); j++) {
                        fw.write("," + feature.getDouble(j));
                    }
                    if (feature.getDouble(2) > 0)
                        fw.write("]\t" + (-1 * score) + "\n");
                    else
                        fw.write("]\t" + score + "\n");
                }
            }
            recordReader.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        AutoEncoderPeakCalling autoEncoderPeakCalling = new AutoEncoderPeakCalling(new File("E:\\DeepRIPTest\\KeBam\\IPFeature.csv"));
        autoEncoderPeakCalling.Train(500, 10);
//        autoEncoderPeakCalling.Predict("E:\\DeepRIPTest\\KeBam\\IPRes.txt", 500);
    }
}
