package SingleNucleotide.CNN;

import SingleNucleotide.Data.CNNCSVReader;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

/**
 * Created by Ben on 2018/5/9.
 */
public class CNNModel {
    private MultiLayerNetwork cnnModel;
    private LinkedList<NetworkTopology> networkTopologyList;

    public CNNModel() {
        cnnModel = null;
        networkTopologyList = null;
    }

    public CNNModel(File jsonFile) {
        TopologyReader topologyReader = new TopologyReader(jsonFile);
        networkTopologyList = topologyReader.getTopologyList();
    }

    public CNNModel(String jsonFilePath) {
        TopologyReader topologyReader = new TopologyReader(jsonFilePath);
        networkTopologyList = topologyReader.getTopologyList();
    }

    public void LoadModelFromFile(File modelFile) {
        try {
            cnnModel = ModelSerializer.restoreMultiLayerNetwork(modelFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void LoadModelFromResource(String modelResource) {
        try {
            InputStream is = CNNModel.class.getResourceAsStream(modelResource);
            cnnModel = ModelSerializer.restoreMultiLayerNetwork(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void BuildModel() {
        NeuralNetConfiguration.ListBuilder listBuilder = new NeuralNetConfiguration.Builder()
                .seed(12345)
                .l1(10e-04)
                .l2(10e-04)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(0.001, 0.9))
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .list();
        //Build layer from topology list
        int index = 0;
        int channels = networkTopologyList.getFirst().getChannels();
        int height = networkTopologyList.getFirst().getHeight();
        int width = networkTopologyList.getFirst().getWidth();
        for (NetworkTopology networkTopology : networkTopologyList) {
            int layerType = networkTopology.getLayerType();
            if (layerType == NetworkTopology.Convolution) {
                int[] kernelSize = networkTopology.getKernelSize();
                int[] stride = networkTopology.getStride();
                int[] padding = networkTopology.getPadding();
                if (networkTopology.getChannels() == 0) {
                    //hidden CNN layer, no need to specify input channel
                    listBuilder = listBuilder.layer(index, new ConvolutionLayer.Builder(kernelSize[0], kernelSize[1])
                            .stride(stride[0], stride[1])
                            .padding(padding[0], padding[1])
                            .nOut(networkTopology.getNumFilters())
                            .biasInit(0)
                            .activation(Activation.IDENTITY)
                            .build());
                } else {
                    //input CNN layer
                    listBuilder = listBuilder.layer(index, new ConvolutionLayer.Builder(kernelSize[0], kernelSize[1])
                            .stride(stride[0], stride[1])
                            .padding(padding[0], padding[1])
                            .nIn(channels)
                            .nOut(networkTopology.getNumFilters())
                            .biasInit(0)
                            .activation(Activation.IDENTITY)
                            .build());
                }
            } else if (layerType == NetworkTopology.MaxPooling) {
                int[] kernelSize = networkTopology.getKernelSize();
                int[] stride = networkTopology.getStride();
                int[] padding = networkTopology.getPadding();
                listBuilder = listBuilder.layer(index, new SubsamplingLayer.Builder(PoolingType.MAX)
                        .kernelSize(kernelSize[0], kernelSize[1]).stride(stride[0], stride[1])
                        .padding(padding[0], padding[1])
                        .build());
            } else if (layerType == NetworkTopology.DenseLayer) {
                DenseLayer.Builder denseLayerBuilder = new DenseLayer.Builder().activation(Activation.RELU).nOut(networkTopology.getOutputSize());
                if(networkTopology.getDropout() > 0)
                    denseLayerBuilder = denseLayerBuilder.dropOut(networkTopology.getDropout());
                listBuilder = listBuilder.layer(index, denseLayerBuilder.build());
            } else if (layerType == NetworkTopology.OutputLayer) {
                listBuilder = listBuilder.layer(index, new OutputLayer.Builder()
                        .lossFunction(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .activation(Activation.SOFTMAX)
                        .nOut(networkTopology.getOutputSize()).build());
            } else {
                System.out.println("Unsupported layer type, construction skipped!");
                index--;
            }
            index++;
        }
        MultiLayerConfiguration cnnConfigure = listBuilder.backprop(true)
                .pretrain(false)
                .setInputType(InputType.convolutional(height, width, channels)).build();
        cnnModel = new MultiLayerNetwork(cnnConfigure);
        cnnModel.init();
    }

    public void Train(LinkedList<DataSet> batchDataSetList, int epoch) {
//        UIServer uiServer = UIServer.getInstance();
//        StatsStorage statsStorage = new InMemoryStatsStorage();
//        uiServer.attach(statsStorage);

        cnnModel.setListeners(new ScoreIterationListener(500));
        for(int i=1; i<=epoch; i++) {
//            System.out.println("Epoch " + i);
            for(DataSet trainData : batchDataSetList) {
                trainData.shuffle();
                cnnModel.fit(trainData);
            }
        }

        System.out.println("Model training finished!");
    }

    public double Predict(double[][] feature, int channels) {
        int featureLen = feature[0].length / channels;
        INDArray inputFeature = Nd4j.create(1, channels, feature.length, featureLen);
        for(int i=0; i<feature.length; i++) {
            for(int j=0; j<feature[i].length; j++) {
                int[] index = new int[4];
                index[0] = 0;
                index[1] = (int)((double)j / (double)featureLen);;
                index[2] = i;
                index[3] = j - index[1]*featureLen;
                inputFeature.putScalar(index, feature[i][j]);
            }
        }

        INDArray outputArr = cnnModel.output(inputFeature);
        return outputArr.getDouble(0,0);//return the probability of positive label as score
    }

    public void SaveModel(File modelFile) {
        try {
            Nd4j.getRandom().setSeed(12345);
            ModelSerializer.writeModel(cnnModel, modelFile, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CNNCSVReader cnncsvReader = new CNNCSVReader(new File("E:\\工作文档\\MATK\\SingleNucleotide\\SiteData\\TrainingData\\Sequence\\Mouse_mm10_Training.csv"), 80, 1);
        LinkedList<DataSet> batchDataList = cnncsvReader.getBatchList();

        CNNModel cnnModel = new CNNModel(new File("E:\\工作文档\\MATK\\SingleNucleotide\\OneHot_PSSM_Model.json"));
        cnnModel.BuildModel();
        System.out.println("Begin training.");
        cnnModel.Train(batchDataList, 50);

        cnnModel.SaveModel(new File("E:\\工作文档\\MATK\\SingleNucleotide\\TrainingResult\\Sequence\\Mouse_Sequence_Model"));
    }
}
