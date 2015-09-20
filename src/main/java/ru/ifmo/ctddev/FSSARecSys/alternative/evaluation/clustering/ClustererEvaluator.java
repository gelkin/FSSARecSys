package ru.ifmo.ctddev.FSSARecSys.alternative.evaluation.clustering;

import ru.ifmo.ctddev.FSSARecSys.alternative.internal.Clusterisation;
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

    public ClustererEvaluator(String name, Clusterer clusterer) {
        this.name = Objects.requireNonNull(name);
        this.clusterer = Objects.requireNonNull(clusterer);
    }

    public String getName() {
        return name;
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

            // Instances in = new Instances()
            ArrayList<Instances> clusters = new ArrayList<>(numClusters);

            for (int i = 0; i < numClusters; i++)
                clusters.add(new Instances(data, 0));

            for (int i = 0; i < data.numInstances(); i++) {
                if (clusterAssignments[i] != -1)
                    clusters.get(clusterAssignments[i]).add(data.instance(i));
            }

            Clusterisation clusterisation = new Clusterisation(clusters, clusterer);
            System.out.println("Davies-Bouldin: " + clusterisation.DaviesBouldinIndex());
            System.out.println("Dunn index: " + clusterisation.DunnIndex());
            System.out.println("Silhouette: " + clusterisation.silhouetteIndex());
            System.out.println("Calinski-Harabasz: " + clusterisation.CalinskiHarabaszIndex());
            System.out.println("SDbw: " + clusterisation.SDbw());
            System.out.println("Score func: " + clusterisation.ScoreFunction());

            return new ClustererResult(clusterisation.DaviesBouldinIndex(), clusterisation.DunnIndex(), clusterisation.silhouetteIndex());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
