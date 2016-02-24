package ru.ifmo.ctddev.FSSARecSys.algorithmics;

import ru.ifmo.ctddev.FSSARecSys.FSSResult;
import ru.ifmo.ctddev.FSSARecSys.alternative.evaluation.Evaluator;
import ru.ifmo.ctddev.FSSARecSys.alternative.evaluation.classification.EARREvaluation;
import ru.ifmo.ctddev.FSSARecSys.calculators.internal.FSSAlgorithmImpl;
import ru.ifmo.ctddev.FSSARecSys.db.DataSet;
import ru.ifmo.ctddev.FSSARecSys.db.internal.Dataset;
import ru.ifmo.ctddev.FSSARecSys.db.internal.FSSAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.db.manager.QueryManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;

/**
 * Created by Kirill on 26.12.2015.
 */
public class LearningBasedOnAlgorithmics extends AbstractAlgorithm implements Runnable {



public static double function(double Qi, int t, int n)
{
return Qi+ Math.sqrt(2 * Math.log(t) / n);

}




//
//    private static ArrayList<FSSAlgorithm> algorithms;
//
//    private static ArrayList<DataSet> dataSets;
//    private static QueryManager db ;
//    //=  new QueryManager("jdbc:mysql://localhost/", "fss", "954476");
//
////    private HashMap<ComparisionResults,DataSet> dictionary;
////    private ArrayList<ComparisionResults> list;
////    Evaluator evaluator = new Evaluator();





    @Override
    public void run() {

//        //  dictionary = new HashMap<ComparisionResults, DataSet>();
//        list = new ArrayList<>();
//        for (DataSet ds : dataSets) {
//
//            for (FSSAlgorithmImpl alg : algorithms) {
//                FSSResult res = alg.run(ds);
//                //        dictionary.put(new ComparisionResults())
//                list.add(new ComparisionResults(ds, new AlgorithmResultRelation(alg, res)));
//
//            }
//        }


    }
//
//    public static void Initialize()
//    {
//
//        algorithms = new ArrayList<>();
//        dataSets = new ArrayList<>();
//        try {
//        algorithms = db.getAvailableFssAlgorithms();
//    }
//      catch (Exception ex)
//      {
//          System.out.println(ex.getMessage());
//      }
//try {
//    for (String st : dataSetNames
//            ) {
//        dataSets.add(db.getDataset(st));
//    }
//}
//    catch (Exception ex)
//    {
//System.out.println(ex.getMessage());
//    }
//}




//    public static void main(String[] args)
//    {
//
////        if (args.length>=1) {
////
////            System.out.println("Getting dataset...");
////            try {
////                File datasetFile = new File(args[0]);
////                Dataset dataset = new Dataset(datasetFile.getName(), datasetFile, "clusterisation");
////            }
////            catch (Exception exception)
////            {
////                System.out.println("Something went bad "+exception.getMessage());
////            }
////        }
////        else
////        {
////            System.out.println("Less than 2 args specified");
////        }
//
//        System.out.print("Establishing database connection...");
//        db = new  QueryManager("jdbc:mysql://localhost/", "root", "954476");
//        if (db!=null)
//        {
//            System.out.println("Ok");
//        }
//        System.out.println("Initializing system...");
//
//        Initialize();
//
//        System.out.println("Algorithms count:"+algorithms.size());
//        System.out.println("DataSets count:"+dataSets.size());
//
//
//    }


    public class AlgorithmResultRelation
    {
        private FSSAlgorithmImpl algorithm;
        private FSSResult result;

        public AlgorithmResultRelation(FSSAlgorithmImpl _algorithm, FSSResult _result)
        {
            this.algorithm = _algorithm;
            this.result = _result;

        }

    }

    public class  ComparisionResults
    {
        private DataSet dataset;
        private AlgorithmResultRelation relation;

        public ComparisionResults(DataSet _set, AlgorithmResultRelation _relation)
        {
            this.dataset = _set;
            this.relation = _relation;
        }
    }


}
