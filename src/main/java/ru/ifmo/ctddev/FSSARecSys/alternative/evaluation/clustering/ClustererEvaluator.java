package ru.ifmo.ctddev.FSSARecSys.alternative.evaluation.clustering;

import ru.ifmo.ctddev.FSSARecSys.alternative.internal.Clusterisation;
import ru.ifmo.ctddev.FSSARecSys.utils.PictureManagement;
import weka.classifiers.Classifier;
import weka.clusterers.*;
import weka.core.Instance;
import weka.core.Instances;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

/**
 * Created by Администратор on 12.08.2015.
 */
public class ClustererEvaluator {
    private final String name;
    private final Clusterer clusterer;

    private Clusterisation clusterisation;

    private Double DBIndex = Double.NEGATIVE_INFINITY;
    private Double DunnIndex  = Double.NEGATIVE_INFINITY;
    private Double SilhouetteIndex = Double.NEGATIVE_INFINITY;
    private Double CHIndex = Double.NEGATIVE_INFINITY;
    private Double SDbwIndex = Double.NEGATIVE_INFINITY;
    private Double SFIndex = Double.NEGATIVE_INFINITY;


    public ClustererEvaluator(String name, Clusterer clusterer) {
        this.name = Objects.requireNonNull(name);
        this.clusterer = Objects.requireNonNull(clusterer);
    }

    public String getName() {
        return name;
    }

    public Double getDBIndex() throws Exception {
        if (DBIndex == Double.NEGATIVE_INFINITY)
            return clusterisation.DaviesBouldinIndex();
        else
            return DBIndex;
    }

    public Double getDunnIndex(){
        if (DunnIndex == Double.NEGATIVE_INFINITY)
            DunnIndex = clusterisation.DunnIndex();
        return DunnIndex;
    }

    public Double getSilhouetteIndex(){
        if (SilhouetteIndex == Double.NEGATIVE_INFINITY)
            SilhouetteIndex = clusterisation.silhouetteIndex();
        return SilhouetteIndex;
    }

    public Double getCHIndex(){
        if (CHIndex == Double.NEGATIVE_INFINITY)
            CHIndex =  clusterisation.CalinskiHarabaszIndex();
        return CHIndex;
    }

    public Double  getSDbw() throws Exception {
        if (SDbwIndex == Double.NEGATIVE_INFINITY)
            SDbwIndex = clusterisation.SDbw();
        return SDbwIndex;
    }

    public Double getSF(){
        if (SFIndex ==  Double.NEGATIVE_INFINITY)
            SFIndex = clusterisation.ScoreFunction();
        return SFIndex;
    }

    public Double getCombinedMetric() throws Exception {
        return Math.sqrt(Math.pow(getDBIndex(), 2.0) + Math.pow((1 / getDunnIndex()), 2.0) + Math.pow((1 / getSilhouetteIndex()), 2.0));
    }

    public ClustererResult evaluate(String datasetName, Instances data) {

        try {
            //Clusterer abstractClusterer = AbstractClusterer.makeCopy(clusterer);
            clusterer.buildClusterer(data);

            ClusterEvaluation eval = new ClusterEvaluation();
            eval.setClusterer(clusterer);
            eval.getClusterAssignments();
            eval.evaluateClusterer(new Instances(data));
            double[] tmpDistribution = eval.getClusterAssignments();
            int[] clusterAssignments = new int[data.numInstances()];

            for (int i = 0; i < data.numInstances(); i++) {
                clusterAssignments[i] = (int) tmpDistribution[i];
            }

            int numClusters = eval.getNumClusters();

            PictureManagement pm = new PictureManagement(numClusters, data, clusterAssignments);
            pm.DrawPicture(datasetName + "_" + name);

            // Instances in = new Instances()
            ArrayList<Instances> clusters = new ArrayList<>(numClusters);

            for (int i = 0; i < numClusters; i++)
                clusters.add(new Instances(data, 0));

            for (int i = 0; i < data.numInstances(); i++) {
                if (clusterAssignments[i] != -1)
                    clusters.get(clusterAssignments[i]).add(data.instance(i));
            }

            clusterisation = new Clusterisation(clusters, clusterer);

//            DBIndex = clusterisation.DaviesBouldinIndex();
//            DunnIndex = clusterisation.DunnIndex();
//            SilhouetteIndex = clusterisation.silhouetteIndex();

//            System.out.println("Davies-Bouldin [min]: " + getDBIndex());
//            System.out.println("Dunn index [max]: " + getDunnIndex());
//            System.out.println("Silhouette [max]: " + getSilhouetteIndex());
//            System.out.println("Calinski-Harabasz [min]: " + getCHIndex());
//            System.out.println("SDbw [min]: " + getSDbw());
//            System.out.println("Score func [max]: " + getSF());
//            System.out.println("Combination index (new) [min]: " + getCombinedMetric());

            return new ClustererResult(getDBIndex(), getDunnIndex(), getSilhouetteIndex());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
