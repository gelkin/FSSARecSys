package ru.ifmo.ctddev.FSSARecSys.alternative.evaluation;

import cern.colt.map.AbstractIntDoubleMap;
import cern.colt.map.OpenIntDoubleHashMap;
import ru.ifmo.ctddev.FSSARecSys.alternative.evaluation.classification.*;
import ru.ifmo.ctddev.FSSARecSys.alternative.evaluation.clustering.ClustererEvaluator;
import ru.ifmo.ctddev.FSSARecSys.alternative.evaluation.clustering.ClustererResult;
import ru.ifmo.ctddev.FSSARecSys.alternative.internal.FSSClustering.LaplasianScore;
import ru.ifmo.ctddev.FSSARecSys.alternative.internal.FSSClustering.SPEC;
import ru.ifmo.ctddev.FSSARecSys.db.internal.*;
import ru.ifmo.ctddev.FSSARecSys.db.manager.*;
import weka.clusterers.*;
import weka.core.EuclideanDistance;
import weka.core.Instances;

import java.io.File;
import java.util.*;

/**
 * Created by Сергей on 03.08.2015.
 */
public class Evaluator {
    private QueryManager queryManager;
    private List<String> listOfMetaFeatures;

    //for classification tasks
    private float alpha;
    private float betta;

    public Evaluator() {

    }

    public Evaluator(QueryManager queryManager, List<String> listOfFeatures) {
        this.queryManager = queryManager;
        this.listOfMetaFeatures = listOfFeatures;
    }

    public void setAlphaAndBeta(float alpha, float betta) {
        this.alpha = alpha;
        this.betta = betta;
    }

    public Double evaluate(FSSAlgorithm fssAlgorithm, Dataset dataset, MLAlgorithm mlAlgorithm) throws Exception {
        switch (dataset.getTaskType()){
            case "classification":
                if (queryManager.getDataset(dataset.getName()) == null) {
                    queryManager.addDataset(dataset);
                }
                ArrayList<FSSAlgorithm> availableFSSAlgorithms = queryManager.getAvailableFssAlgorithms();

                EARREvaluation  earrEvaluation = new EARREvaluation(alpha, betta, queryManager);
                return earrEvaluation.evaluate(fssAlgorithm, availableFSSAlgorithms, dataset, listOfMetaFeatures, mlAlgorithm);
            case "clusterisation":
//                if (queryManager.getDataset(dataset.getName()) == null) {
//                    queryManager.addDataset(dataset);
//                }

                Clusterer as = AbstractClusterer.forName(mlAlgorithm.getClassPath(), weka.core.Utils.splitOptions(mlAlgorithm.getOptions()));
                ClustererEvaluator clustererEvaluator = new ClustererEvaluator(mlAlgorithm.getName(), as);

                /*todo: add fss-shit for clustering (orly?)*/
                //SpectralClusterer sp = new SpectralClusterer();

                //DoubleMatrix2D d = DoubleFactory2D.dense.make(3864025, 3864025);
                System.out.println("Laplasian scores:");
                LaplasianScore laplasianScore = new LaplasianScore(dataset.getInstances(), new EuclideanDistance());

                Map<Double, Integer> laplas = new TreeMap<>();
                for (int i = 0; i < dataset.getInstances().numAttributes(); i++) {
                    Double val = laplasianScore.getFeatureWeight(i);
                    //System.out.println(val);
                    laplas.put(val, i);
                }

                for (Map.Entry<Double, Integer> entry : laplas.entrySet()) {
                    System.out.println(entry.getKey() + " - " + entry.getValue());
                }

                System.out.println("SPEC scores:");
                SPEC spec = new SPEC(dataset.getInstances(), new EuclideanDistance());

                Map<Double, Integer> specMap = new TreeMap<>();
                for (int i = 0; i < dataset.getInstances().numAttributes(); i++) {
                    Double val = spec.getFeatureWeight(i);
                    specMap.put(val, i);
                    //System.out.println(spec.getFeatureWeight(i));
                }

                for (Map.Entry<Double, Integer> entry : specMap.entrySet()) {
                    System.out.println(entry.getKey() + " - " + entry.getValue() );
                }

                ClustererResult clustererResult = clustererEvaluator.evaluate(dataset.getName(), dataset.getInstances());

                return clustererResult.countSquareDistance();

        }
        return null;
    }

    public static void main(String [] args)
    {
        File f = new File("car.arff");
        Dataset dataset = new Dataset("car", f, "clusterisation");

        MLAlgorithm ml = new MLAlgorithm("first_blood", "weka.clusterers.SimpleKMeans", "", "clusterisation");

        //QueryManager queryManager = null;
        //List<String> listOfFeatures = new ArrayList<String>();

        Evaluator e = new Evaluator();

        try {
            System.out.print("My evaluator: " + e.evaluate(null, dataset, ml));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
