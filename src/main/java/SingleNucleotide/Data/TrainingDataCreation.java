package SingleNucleotide.Data;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by Ben on 2018/5/5.
 */
public class TrainingDataCreation {

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

    public LinkedList<DataSet> CreateBatchList(LinkedList<DataPair> positiveDataList, LinkedList<DataPair> negativeDataList, int batchSize, int channels) {
        LinkedList<DataSet> batchList = new LinkedList<DataSet>();

        LinkedList<DataPair> combineList = new LinkedList<DataPair>();
        for(DataPair dataPair : positiveDataList) {
            combineList.add(dataPair);
        }
        for(DataPair dataPair : negativeDataList) {
            combineList.add(dataPair);
        }
        Collections.shuffle(combineList);
        //Batch
        int curCount = 0;
        boolean isLeft = true;
        LinkedList<DataPair> tmpDataPairList = new LinkedList<DataPair>();
        for(DataPair dataPair : combineList) {
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
        return batchList;
    }
}
