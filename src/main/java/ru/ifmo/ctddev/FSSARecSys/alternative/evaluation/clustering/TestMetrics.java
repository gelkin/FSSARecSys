package ru.ifmo.ctddev.FSSARecSys.alternative.evaluation.clustering;

import ru.ifmo.ctddev.FSSARecSys.alternative.evaluation.Evaluator;
import ru.ifmo.ctddev.FSSARecSys.db.internal.Dataset;
import ru.ifmo.ctddev.FSSARecSys.db.internal.MLAlgorithm;
import weka.clusterers.AbstractClusterer;
import weka.clusterers.Clusterer;

import java.io.File;

/**
 * Created by Администратор on 21.09.2015.
 */
public class TestMetrics {


    public static void evaluateWithMetrics(Dataset dataset, MLAlgorithm mlAlgorithm) throws Exception {

        System.out.println("==================================================");
        System.out.println(mlAlgorithm.getName());
        System.out.println("--------------------------------------------------");

        Clusterer as = AbstractClusterer.forName(mlAlgorithm.getClassPath(), weka.core.Utils.splitOptions(mlAlgorithm.getOptions()));
        ClustererEvaluator clustererEvaluator = new ClustererEvaluator(mlAlgorithm.getName(), as);
        ClustererResult clustererResult = clustererEvaluator.evaluate(dataset.getName(), dataset.getInstances());

    }

    public static void main(String [] args) throws Exception {
        File f = new File("simple.arff");
        Dataset dataset = new Dataset("car", f, "clusterisation");
//        Instances tmp = dataset.getInstances();
//        tmp.deleteStringAttributes();
//        dataset.


        MLAlgorithm mlkMeans = new MLAlgorithm("K-means", "weka.clusterers.SimpleKMeans", "-N 5 -A \"weka.core.EuclideanDistance -R first-last\" -I 500 -S 10", "clusterisation");
        MLAlgorithm mlDBSCAN = new MLAlgorithm("DBSCAN", "weka.clusterers.DBSCAN", "-E 0.1 -M 6 -I weka.clusterers.forOPTICSAndDBScan.Databases.SequentialDatabase -D weka.clusterers.forOPTICSAndDBScan.DataObjects.EuclideanDataObject", "clusterisation");
        //MLAlgorithm mlOPTICS = new MLAlgorithm("OPTICS", "weka.clusterers.OPTICS", "", "clusterisation");
        MLAlgorithm mlEM = new MLAlgorithm("EM", "weka.clusterers.EM", "-I 100 -N -1 -M 1.0E-6 -S 100", "clusterisation");
        MLAlgorithm mlFarthestFirst = new MLAlgorithm("mlFarthestFirst", "weka.clusterers.FarthestFirst", "-N 5 -S 1", "clusterisation");
        MLAlgorithm mlHieracical = new MLAlgorithm("Hieracical", "weka.clusterers.HierarchicalClusterer", "-N 5 -L MEAN -P -A \"weka.core.EuclideanDistance -R first-last\"", "clusterisation");
        MLAlgorithm mlXmeans = new MLAlgorithm("X-Means", "weka.clusterers.XMeans", "-I 1 -M 1000 -J 1000 -L 2 -H 6 -B 1.0 -C 0.5 -D \"weka.core.EuclideanDistance -R first-last\" -S 10", "clusterisation");


        evaluateWithMetrics(dataset, mlkMeans);
        evaluateWithMetrics(dataset, mlEM);
        evaluateWithMetrics(dataset, mlDBSCAN);
        evaluateWithMetrics(dataset, mlFarthestFirst);
        evaluateWithMetrics(dataset, mlHieracical);
        evaluateWithMetrics(dataset, mlXmeans);


        //QueryManager queryManager = null;
        //List<String> listOfFeatures = new ArrayList<String>();




    }
}
