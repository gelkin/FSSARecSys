package ru.ifmo.ctddev.FSSARecSys.algorithmics;

import ru.ifmo.ctddev.FSSARecSys.FSSResult;
import ru.ifmo.ctddev.FSSARecSys.alternative.evaluation.Evaluator;
import ru.ifmo.ctddev.FSSARecSys.alternative.evaluation.classification.EARREvaluation;
import ru.ifmo.ctddev.FSSARecSys.calculators.*;
import ru.ifmo.ctddev.FSSARecSys.calculators.internal.ClassifierEvaluatorImpl;
import ru.ifmo.ctddev.FSSARecSys.calculators.internal.FSSAlgorithmImpl;
import ru.ifmo.ctddev.FSSARecSys.db.DataSet;
import ru.ifmo.ctddev.FSSARecSys.db.MLAlgorithManager;
import ru.ifmo.ctddev.FSSARecSys.db.internal.*;
import ru.ifmo.ctddev.FSSARecSys.db.internal.FSSAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.db.manager.QueryManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Kirill on 07.01.2016.
 */
public class LearningSystem {


    //Base data getting from db
    private static ArrayList<FSSAlgorithm> algorithms;
    private static ArrayList<FSSAlgorithmImpl> algorithmsImpl;
    private static EARREvaluation    evaluator;
    private static ArrayList<Dataset> dataSets;
    private static QueryManager queryManager;
    private static double[][] Q;

    private static List<String> features;
    private static MLAlgorithm mlAlg;

    //Helpful
    private static double[] algSum;
    private static int[] algCount;
    private static int[] dataCount;
    private static int totalCount;
    private static List<MLAlgorithm> mlAlgorithmList;


    //Resulting datas
    private static ArrayList<Double> algorithmLearning;
    private static ArrayList<Double> dataLearning;
    private  static  ArrayList<Double> hybridLearning;

public static void main(String[] args) throws Exception


    {

    queryManager = new QueryManager("jdbc:mysql://localhost/fss", "newuser", "Sudarikov94");
    System.out.println("Clearing schema...");
    DataGathererHelper.ClearSchema();
    evaluator =
             new EARREvaluation(0.1f, 0.1f, queryManager);
    System.out.println("Getting algorithms...");


    SimpleFSSAlgorithmManager algorithmManager = new SimpleFSSAlgorithmManager();
    System.out.println(algorithmManager.getAvailableFSSAlgorithms().size());
  try {
      for (String s : algorithmManager.getInstance().getAvailableFSSAlgorithms()) {
          queryManager.addFSSAlgorithm(algorithmManager.getInstance().getDb(s));
      }
  }
  catch (Exception e)
  {
      e.printStackTrace();
  }
    algorithms=
    DataGathererHelper.GetRandomlyTossedAlgorithms();
    algorithmsImpl = algorithmManager.implementations;
    System.out.println("FSSA Algorithms count: "+ algorithms.size());


    System.out.println("Getting ml algorithms...");



    System.out.println("Getting Datasets...");




    dataSets = DataGathererHelper.GetDataSets();


    System.out.println("Datasets count: "+dataSets.size());

    algSum = new double[algorithms.size()];
    algCount = new int[algorithms.size()];
    dataCount = new int[dataSets.size()];
    algorithmLearning = new ArrayList<>();
    dataLearning = new ArrayList<>();
    hybridLearning = new ArrayList<>();

    Q = new double[algorithms.size()][dataSets.size()];


   // mlAlg = new MLAlgorithm();
    System.out.println("Evaluating Q matrix...");
        MLAlgorithm ml = new MLAlgorithm("first_blood", "weka.clusterers.SimpleKMeans", "", "clusterisation");

        int i =0;
    for (FSSAlgorithmImpl alg: algorithmsImpl)
    {
        int ds = selectDataset(i);
        for (Dataset dataset:dataSets
             ) {
try {
    Evaluator newEvaluator = new Evaluator(queryManager);
    Q[i][ds] =
            newEvaluator.evaluate(alg.toDbObject(), dataset, ml);
    Thread.sleep(1000);
}
        catch (Exception ex)
        {
            ex.printStackTrace();
            Thread.sleep(1000);
        }

        }
        algSum[i] = Q[i][ds];
       algCount[i]++;
        totalCount++;
        dataCount[ds]++;
        i++;
       }

//    for (FSSAlgorithmImpl alg: algorithmsImpl
//         ) {
//        int ds = selectDataset(i);
//        try {
//            Q[i][ds] =
//                    alg.run()
//                    //evaluator.evaluate(alg.toDbObject(), algorithms, dataSets.get(ds), features, mlAlg);
//            Thread.sleep(1000);
//        }
//        catch (Exception ex)
//        {
//          ex.printStackTrace();
//        }
//        algSum[i] = Q[i][ds];
//        algCount[i]++;
//        totalCount++;
//        dataCount[ds]++;
//        i++;
//    }

    for (int t=0; t<1000; t++)
    {
        for (int iterator=0; iterator<=Q.length; iterator++) {
            algorithmLearning.add(LearningBasedOnAlgorithmics.function(Q[iterator][0], t, algCount[iterator]));
            for (int j = 0; j <= dataSets.size(); j++) {
                dataLearning.add(LearningBasedOnData.function(Q[iterator][j], t, algCount[iterator]));
                hybridLearning.add(HybridLearning.sumFunction(algorithmLearning.get(t), dataLearning.get(t)));
            }
        }
    }

//    while(true) {
//        int alg = getBestAlgOnAlgorithmicLearning();
//
//        // on some moment we should stop
//        // for example when no rarly used dataset found for out best alg selected below
//        int ds = selectDataset(alg);
//
//        try {
//        Q[alg][ds] = evaluator.evaluate(algorithms.get(alg), algorithms, dataSets.get(ds), features, mlAlg);
//        }
//        catch (Exception ex)
//        {
//            System.out.println("Evaluator exception.."+ex.getMessage());
//        }
//
//        algSum[alg] += Q[alg][ds];
//        algCount[alg]++;
//        totalCount++;
//        dataCount[ds]++;
//    }







}




    public static int getBestAlgOnAlgorithmicLearning() {
        double maxV = 0;
        int maxI = 0;
        for(int i = 0; i < algCount.length; i++) {
            double v = algSum[i] + Math.sqrt(2 * Math.log(totalCount) / algCount[i]);
            if (maxV < v) {
                maxV = v;
                maxI = i;
            }
        }
        return maxI;
    }


    private static int selectDataset(int algNum) {
        int ds = 0, dsMin;
        int max = 0;
        for (int i = 0; i < dataCount.length; i++)
            if (dataCount[i] > max) {
                max = dataCount[i];
                ds = i;
            }
        dsMin = ds;


        int cnt = 0;
        Random rnd = new Random();
        while((Q[algNum][ds] != 0) && (cnt < 10)){
            ds = rnd.nextInt();
        }

        if(cnt == 10)
            ds = dsMin;

        return ds;
    }


}
