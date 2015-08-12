package ru.ifmo.ctddev.FSSARecSys.alternative.evaluation.clustering;

import weka.classifiers.Classifier;
import weka.clusterers.AbstractClusterer;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.Instance;
import weka.core.Instances;

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

    public ClustererResult evaluate(String datasetName, Instances train, Instances test) {

        //ClusterEvaluation clusterEvaluation = new ClusterEvaluation();
        try {
            Clusterer abstractClusterer = AbstractClusterer.makeCopy(clusterer);
            abstractClusterer.buildClusterer(train);

            //abstractClusterer.
            for (int i = 0; i < train.numInstances(); i++) {
                Instance instance = train.instance(i);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //localClusterer.buildClusterer();

        return null;
    }
}
