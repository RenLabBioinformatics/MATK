package SingleNucleotide.CNN;

import Basic.FileExtensionFilter;
import SingleNucleotide.Data.CNNCSVReader;
import SingleNucleotide.Validation.PerformanceCalculator;
import SingleNucleotide.Validation.Validation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Ben on 2018/5/10.
 */
public class ModelOptimizationConsole {
    private HashMap<String , Double> aucMap, snMap;

    private void BatchOptimized(File trainingDataFile, File jsonPath, int batchSize, int epoch, double sp) {
        aucMap = new HashMap<>();
        snMap = new HashMap<>();

        File[] jsonFiles = jsonPath.listFiles(new FileExtensionFilter("json"));
        CNNCSVReader cnncsvReader = new CNNCSVReader(trainingDataFile);
        for(int i=0; i<jsonFiles.length; i++) {
            System.out.println("Validating topology file: " + jsonFiles[i].getName());

            Validation validation = new Validation(cnncsvReader.getPositiveDataList(), cnncsvReader.getNegativeDataList(), jsonFiles[i]);
            validation.nFold(4,1,batchSize,epoch,1);
            LinkedList<Double> positiveScoreList = validation.getPositiveScoreList();
            LinkedList<Double> negativeScoreList = validation.getNegativeScoreList();

            PerformanceCalculator performanceCalculator = new PerformanceCalculator(positiveScoreList, negativeScoreList);
            performanceCalculator.FullPerformance();
            double auc = performanceCalculator.CalculateAUC();
            double sn = performanceCalculator.getSn(sp);

            aucMap.put(jsonFiles[i].getName(), auc);
            snMap.put(jsonFiles[i].getName(), sn);

            System.out.println(jsonFiles[i].getName() + " validated.");
        }
    }

    private void SaveValidationResult(File saveFile) {
        try {
            FileWriter fw = new FileWriter(saveFile);
            fw.write("FileName\tSn\tAUC\n");
            for(String key : snMap.keySet()) {
                double sn = snMap.get(key);
                double auc = aucMap.get(key);
                fw.write(key + "\t" + sn + "\t" + auc + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //java -jar DeepRIP.jar -optimized TrainingSet JSONFile BatchSize Epoch Specificity
        if(args[0].equals("-h") || args[0].equals("-help")) {
            System.out.println("java -jar DeepRIP.jar -optimized TrainingSet JSONFile BatchSize Epoch Specificity SaveFile");
        } else if(args[0].equals("-optimized")) {
            ModelOptimizationConsole modelOptimizationConsole = new ModelOptimizationConsole();
            modelOptimizationConsole.BatchOptimized(new File(args[1]), new File(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Double.parseDouble(args[5]));
            modelOptimizationConsole.SaveValidationResult(new File(args[6]));
        }
    }
}
