package ru.ifmo.ctddev.FSSARecSys.alternative.internal;

import weka.clusterers.SimpleKMeans;
import weka.core.EuclideanDistance;
import weka.core.Instances;
import weka.clusterers.Clusterer;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Администратор on 19.08.2015.
 */
public class Clusterisation {

    private ArrayList<Instances> clusters;
    private Clusterer clusterer;

    public Clusterisation(ArrayList<Instances> clusters, Clusterer clusterer) {
        this.clusters = clusters;
        this.clusterer = Objects.requireNonNull(clusterer);
    }

    public Double averageInterClusterDistance(int clusterNum) {
        Instances cluster = clusters.get(clusterNum);
        EuclideanDistance distance = new EuclideanDistance(cluster);
        Double avg = 0.0;

        if (!(clusterer instanceof SimpleKMeans)) {
            for (int i = 0; i < cluster.numInstances(); i++)
                for (int j = 0; j < cluster.numInstances(); j++){
                    if (i != j) {
                        avg += distance.distance(cluster.instance(i), cluster.instance(j));
                    }
                }
            avg /= (Math.pow(cluster.numInstances(), 2.0) - cluster.numInstances());
        } else {

        }
        return avg;
    }

    public Double averageIntraClusterDistance() {

        return null;
    }
}
