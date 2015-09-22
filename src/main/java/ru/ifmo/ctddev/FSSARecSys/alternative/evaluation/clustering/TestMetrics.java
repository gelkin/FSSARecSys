package ru.ifmo.ctddev.FSSARecSys.alternative.evaluation.clustering;

import ru.ifmo.ctddev.FSSARecSys.db.internal.Dataset;
import ru.ifmo.ctddev.FSSARecSys.db.internal.MLAlgorithm;
import weka.clusterers.AbstractClusterer;
import weka.clusterers.Clusterer;

import java.io.File;
import java.util.*;

/**
 * Created by Администратор on 21.09.2015.
 */
public class TestMetrics {

    public static ArrayList<ArrayList<Double>> resultMatrix = new ArrayList<ArrayList<Double>>();
    public static Map<Integer, String> algoMap = new HashMap<Integer, String>();
    public static Map<Integer, String> metricsMap = new HashMap<Integer, String>();

    public static ClustererEvaluator clustererEvaluator = null;

    public static ArrayList<Double> evaluateWithMetrics(Dataset dataset, MLAlgorithm mlAlgorithm) throws Exception {

//        System.out.println("==================================================");
//        System.out.println(mlAlgorithm.getName());
//        System.out.println("--------------------------------------------------");

        Clusterer as = AbstractClusterer.forName(mlAlgorithm.getClassPath(), weka.core.Utils.splitOptions(mlAlgorithm.getOptions()));
        clustererEvaluator = new ClustererEvaluator(mlAlgorithm.getName(), as);
        ClustererResult clustererResult = clustererEvaluator.evaluate(dataset.getName(), dataset.getInstances());

        ArrayList<Double> t = new ArrayList<>();

        t.add(clustererEvaluator.getDBIndex());
        metricsMap.put(t.size() - 1, "Davies-Bouldin");

        t.add(clustererEvaluator.getDunnIndex());
        metricsMap.put(t.size() - 1, "Dunn");

        t.add(clustererEvaluator.getSilhouetteIndex());
        metricsMap.put(t.size() - 1, "Silhouette");

        t.add(clustererEvaluator.getCHIndex());
        metricsMap.put(t.size() - 1, "CH");

        t.add(clustererEvaluator.getSDbw());
        metricsMap.put(t.size() - 1, "SDbw");

        t.add(clustererEvaluator.getSF());
        metricsMap.put(t.size() - 1, "SF");

        t.add(clustererEvaluator.getCombinedMetric());
        metricsMap.put(t.size() - 1, "Combined");

        return t;
    }

    public static void main(String [] args) throws Exception {
        File f = new File("simple.arff");
        Dataset dataset = new Dataset(f.getName(), f, "clusterisation");
        System.out.println(f.getName());

        MLAlgorithm mlkMeans = new MLAlgorithm("K-means", "weka.clusterers.SimpleKMeans", "-N 5 -A \"weka.core.EuclideanDistance -R first-last\" -I 500 -S 10", "clusterisation");
        MLAlgorithm mlDBSCAN = new MLAlgorithm("DBSCAN", "weka.clusterers.DBSCAN", "-E 0.1 -M 6 -I weka.clusterers.forOPTICSAndDBScan.Databases.SequentialDatabase -D weka.clusterers.forOPTICSAndDBScan.DataObjects.EuclideanDataObject", "clusterisation");
        //MLAlgorithm mlOPTICS = new MLAlgorithm("OPTICS", "weka.clusterers.OPTICS", "", "clusterisation");
        MLAlgorithm mlEM = new MLAlgorithm("EM", "weka.clusterers.EM", "-I 100 -N -1 -M 1.0E-6 -S 100", "clusterisation");
        MLAlgorithm mlFarthestFirst = new MLAlgorithm("FarthestFirst", "weka.clusterers.FarthestFirst", "-N 5 -S 1", "clusterisation");
        MLAlgorithm mlHieracical = new MLAlgorithm("Hieracical", "weka.clusterers.HierarchicalClusterer", "-N 5 -L MEAN -P -A \"weka.core.EuclideanDistance -R first-last\"", "clusterisation");
        MLAlgorithm mlXmeans = new MLAlgorithm("X-Means", "weka.clusterers.XMeans", "-I 1 -M 1000 -J 1000 -L 2 -H 6 -B 1.0 -C 0.5 -D \"weka.core.EuclideanDistance -R first-last\" -S 10", "clusterisation");

        ArrayList<Double> tmp = null;

        tmp = evaluateWithMetrics(dataset, mlkMeans);
        algoMap.put(resultMatrix.size(), mlkMeans.getName());
        resultMatrix.add(tmp);

        tmp = evaluateWithMetrics(dataset, mlEM);
        algoMap.put(resultMatrix.size(), mlEM.getName());
        resultMatrix.add(tmp);

        tmp = evaluateWithMetrics(dataset, mlDBSCAN);
        algoMap.put(resultMatrix.size(), mlDBSCAN.getName());
        resultMatrix.add(tmp);

        tmp = evaluateWithMetrics(dataset, mlFarthestFirst);
        algoMap.put(resultMatrix.size(), mlFarthestFirst.getName());
        resultMatrix.add(tmp);

        tmp = evaluateWithMetrics(dataset, mlHieracical);
        algoMap.put(resultMatrix.size(), mlHieracical.getName());
        resultMatrix.add(tmp);

        tmp = evaluateWithMetrics(dataset, mlXmeans);
        algoMap.put(resultMatrix.size(), mlXmeans.getName());
        resultMatrix.add(tmp);


        System.out.printf("%-20s", "               ");
        for (int i = 0; i < metricsMap.size(); i++)
            System.out.printf("%-20s", metricsMap.get(i));
        System.out.println();
        for (int i = 0; i < resultMatrix.size(); i++) {
            ArrayList<Double> a = resultMatrix.get(i);
            System.out.printf( "%-20s", algoMap.get(i));
            for (Double v : a) {
                System.out.printf("%-20s", v);
            }
            System.out.println();
        }


        List<Dataset> datasetArray = new ArrayList<>();
        List<File> fileSet = new ArrayList<>();

        File stripesFile = new File("stripes.arff");
        fileSet.add(stripesFile);

        File centerFile = new File("center.arff");
        fileSet.add(centerFile);

        //** add more files for different structures

        for (File fs: fileSet)
            datasetArray.add(new Dataset(fs.getName(), fs, "clusterisation"));

        for (Dataset d : datasetArray) {
            System.out.println("******************************************************");
            System.out.println(d.getName());

            resultMatrix = new ArrayList<ArrayList<Double>>();

            MLAlgorithm imlkMeans = new MLAlgorithm("K-means", "weka.clusterers.SimpleKMeans", "-N 2 -A \"weka.core.EuclideanDistance -R first-last\" -I 500 -S 10", "clusterisation");
            MLAlgorithm imlDBSCAN = new MLAlgorithm("DBSCAN", "weka.clusterers.DBSCAN", "-E 0.1 -M 6 -I weka.clusterers.forOPTICSAndDBScan.Databases.SequentialDatabase -D weka.clusterers.forOPTICSAndDBScan.DataObjects.EuclideanDataObject", "clusterisation");
            MLAlgorithm imlEM = new MLAlgorithm("EM", "weka.clusterers.EM", "-I 100 -N -1 -M 1.0E-6 -S 100", "clusterisation");
            MLAlgorithm imlFarthestFirst = new MLAlgorithm("FarthestFirst", "weka.clusterers.FarthestFirst", "-N 2 -S 15", "clusterisation");
            MLAlgorithm imlHieracical = new MLAlgorithm("Hieracical", "weka.clusterers.HierarchicalClusterer", "-N 2 -L AVERAGE -P -A \"weka.core.EuclideanDistance -R first-last\"", "clusterisation");
            MLAlgorithm imlXmeans = new MLAlgorithm("X-Means", "weka.clusterers.XMeans", "-I 1 -M 1000 -J 1000 -L 2 -H 4 -B 1.0 -C 0.6 -D \"weka.core.EuclideanDistance -R first-last\" -S 100", "clusterisation");

            //ArrayList<Double> tmp = null;

            tmp = evaluateWithMetrics(d, imlkMeans);
            algoMap.put(resultMatrix.size(), imlkMeans.getName());
            resultMatrix.add(tmp);

            tmp = evaluateWithMetrics(d, imlEM);
            algoMap.put(resultMatrix.size(), imlEM.getName());
            resultMatrix.add(tmp);

            tmp = evaluateWithMetrics(d, imlDBSCAN);
            algoMap.put(resultMatrix.size(), imlDBSCAN.getName());
            resultMatrix.add(tmp);

            tmp = evaluateWithMetrics(d, imlFarthestFirst);
            algoMap.put(resultMatrix.size(), imlFarthestFirst.getName());
            resultMatrix.add(tmp);

            tmp = evaluateWithMetrics(d, imlHieracical);
            algoMap.put(resultMatrix.size(), imlHieracical.getName());
            resultMatrix.add(tmp);

            tmp = evaluateWithMetrics(d, imlXmeans);
            algoMap.put(tmp.size() - 1, imlXmeans.getName());
            resultMatrix.add(tmp);

            System.out.printf("%-20s", "               ");
            for (int i = 0; i < metricsMap.size(); i++)
                System.out.printf("%-20s", metricsMap.get(i));
            System.out.println();
            for (int i = 0; i < resultMatrix.size(); i++) {
                ArrayList<Double> a = resultMatrix.get(i);
                System.out.printf("%-20s", algoMap.get(i));
                for (Double v : a) {
                    System.out.printf("%-20s", v);
                }
                System.out.println();
            }
        }
    }
}
