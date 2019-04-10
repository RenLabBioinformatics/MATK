package SingleNucleotide.Data;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by Ben on 2018/4/28.
 */
public class CNNCSVReader {
    LinkedList<DataSet> batchList;
    LinkedList<DataPair> positiveDataList, negativeDataList;

    public CNNCSVReader(File csvFile) {
        try {
            positiveDataList = new LinkedList<DataPair>();
            negativeDataList = new LinkedList<DataPair>();

            BufferedReader br = new BufferedReader(new FileReader(csvFile));
            String strLine;
            String[] rowArr, colArr;

            while (br.ready()) {
                strLine = br.readLine();
                rowArr = strLine.split(",");
                double[][] value = null;
                for(int i=0; i<rowArr.length-1; i++) {
                    String tmpCol = rowArr[i].substring(1);//remove the left bracket
                    tmpCol = tmpCol.substring(0, tmpCol.length() - 1);//remove the right bracket
                    colArr = tmpCol.split("\\s+");

                    if(i==0)
                        value = new double[rowArr.length-1][colArr.length];

                    for(int j=0; j<colArr.length; j++) {
                        value[i][j] = Double.parseDouble(colArr[j]);
                    }
                }

                int labelVal = Integer.parseInt(rowArr[rowArr.length - 1]);
                double[] labelArr;

                if(labelVal == 0)
                    labelArr = new double[]{0,1};
                else
                    labelArr = new double[]{1,0};

                DataPair dataPair = new DataPair();
                dataPair.setFeatures(value);
                dataPair.setLabels(labelArr);
                if(labelVal == 0)//Negative data
                    negativeDataList.add(dataPair);
                else//Positive data
                    positiveDataList.add(dataPair);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LinkedList<DataPair> getPositiveDataList() {
        return positiveDataList;
    }

    public LinkedList<DataPair> getNegativeDataList() {
        return negativeDataList;
    }

    //balanceRatio comparing to positive data set, 0 indicate the use of full negative data set
    public void SaveDataInCSV(File saveFile, double balanceRatio) {
        try {
            FileWriter fw = new FileWriter(saveFile);
            Collections.shuffle(positiveDataList);
            for(DataPair dataPair : positiveDataList) {
                double[][] feature = dataPair.getFeatures();
                double[] label = dataPair.getLabels();
                //write feature
                for(int i=0; i<feature.length; i++) {
                    fw.write("[");
                    for(int j=0; j<feature[i].length; j++) {
                        fw.write(feature[i][j] + " ");
                    }
                    fw.write("],");
                }
                fw.write("1\n");
            }
            System.out.println("Saving positive data with size of " + positiveDataList.size());

            Collections.shuffle(negativeDataList);
            int index = negativeDataList.size();
            int curIndex = 0;
            if(balanceRatio > 0)
                index = (int) (positiveDataList.size() * balanceRatio);
            for(DataPair dataPair : negativeDataList) {
                curIndex++;
                if(curIndex > index)
                    break;
                else {
                    double[][] feature = dataPair.getFeatures();
                    double[] label = dataPair.getLabels();
                    //write feature
                    for(int i=0; i<feature.length; i++) {
                        fw.write("[");
                        for(int j=0; j<feature[i].length; j++) {
                            fw.write(feature[i][j] + " ");
                        }
                        fw.write("],");
                    }
                    fw.write("0\n");
                }
            }
            System.out.println("Saving negative data with size of " + curIndex +", Positive to negative ratio is " + (double)positiveDataList.size()/(double) curIndex);

            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DataSet CreateBatchDataSet(LinkedList<DataPair> batchDataPairList, int channels) {
        int featureLen = batchDataPairList.getFirst().getFeatures()[0].length / channels;
        INDArray featureINDArr = Nd4j.create(batchDataPairList.size(), channels, batchDataPairList.getFirst().getFeatures().length, featureLen);
        INDArray labelINDArr = Nd4j.create(batchDataPairList.size(),2);
        int index = 0;
        for(DataPair tmpDataPair : batchDataPairList) {
            double[][] features = tmpDataPair.getFeatures();
            double[] labels = tmpDataPair.getLabels();
            //setup features
            for(int i=0; i<features.length; i++) {
                for(int j=0; j<features[i].length; j++) {
                    int[] scalarIndex = new int[4];
                    scalarIndex[0] = index;
                    scalarIndex[1] = (int)((double)j / (double)featureLen);
                    scalarIndex[2] = i;
                    scalarIndex[3] = j - scalarIndex[1]*featureLen;
                    featureINDArr.putScalar(scalarIndex, features[i][j]);
                }
            }
            //setup labels
            for(int i=0; i<labels.length; i++) {
                int[] scalarIndex = new int[2];
                scalarIndex[0] = index;
                scalarIndex[1] = i;
                labelINDArr.putScalar(scalarIndex, labels[i]);
            }
            //add index
            index++;
        }
        DataSet batchDataSet = new DataSet(featureINDArr, labelINDArr);

        return batchDataSet;
    }

    public CNNCSVReader(File csvFile, int batchSize, int channels) {
        try {
            batchList = new LinkedList<DataSet>();

            BufferedReader br = new BufferedReader(new FileReader(csvFile));
            String strLine;
            String[] rowArr, colArr;

            LinkedList<DataPair> dataPairList = new LinkedList<DataPair>();
            while (br.ready()) {
                strLine = br.readLine();
                rowArr = strLine.split(",");
                double[][] value = null;
                for(int i=0; i<rowArr.length-1; i++) {
                    String tmpCol = rowArr[i].substring(1);//remove the left bracket
                    tmpCol = tmpCol.substring(0, tmpCol.length() - 1);//remove the right bracket
                    colArr = tmpCol.split("\\s+");

                    if(i==0)
                        value = new double[rowArr.length-1][colArr.length];

                    for(int j=0; j<colArr.length; j++) {
                        value[i][j] = Double.parseDouble(colArr[j]);
                    }
                }

                int labelVal = Integer.parseInt(rowArr[rowArr.length - 1]);
                double[] labelArr;

                if(labelVal == 0)
                    labelArr = new double[]{0,1};
                else
                    labelArr = new double[]{1,0};

                DataPair dataPair = new DataPair();
                dataPair.setFeatures(value);
                dataPair.setLabels(labelArr);
                dataPairList.add(dataPair);
            }
            //Batches
            Collections.shuffle(dataPairList);
            int curCount = 0;
            boolean isLeft = true;
            LinkedList<DataPair> tmpDataPairList = new LinkedList<DataPair>();
            for(DataPair dataPair : dataPairList) {
                curCount++;
                if(curCount < batchSize) {
                    tmpDataPairList.add(dataPair);
                    isLeft = true;
                } else {
                    DataSet batchDataSet = CreateBatchDataSet(tmpDataPairList, channels);
                    batchList.add(batchDataSet);
                    curCount = 0;
                    tmpDataPairList.clear();
                    isLeft = false;
                }
            }
            if(isLeft) {
                DataSet batchDataSet = CreateBatchDataSet(tmpDataPairList, channels);
                batchList.add(batchDataSet);
                tmpDataPairList.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LinkedList<DataSet> getBatchList() {
        return batchList;
    }

    public static void main(String[] args) {
        CNNCSVReader cnncsvReader = new CNNCSVReader(new File("E:\\DeepRIPTest\\CNN\\RawTrainingData\\HumanAll.csv"));
//        cnncsvReader.getBatchList();
        cnncsvReader.SaveDataInCSV(new File("E:\\DeepRIPTest\\CNN\\RawTrainingData\\HumanAll_Balanced.csv"), 1);
    }
}
