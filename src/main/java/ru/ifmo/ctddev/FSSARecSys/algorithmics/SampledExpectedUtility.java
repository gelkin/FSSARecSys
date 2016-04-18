package ru.ifmo.ctddev.FSSARecSys.algorithmics;

import ru.ifmo.ctddev.FSSARecSys.alternative.evaluation.Evaluator;
import ru.ifmo.ctddev.FSSARecSys.db.internal.Dataset;
import ru.ifmo.ctddev.FSSARecSys.db.internal.FSSAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.db.internal.MLAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.db.manager.QueryManager;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

/**
*
* Assumed that all features are nominal (i.e. they can take on values from a
* finite set of values).
*
*/
public class SampledExpectedUtility {
    private ArrayList<FSSAlgorithm> algorithms;

    private ArrayList<Dataset> dataSets;
    private ArrayList<Integer> bestFSSAlgorithms; // indexes of best FSSAlgorithms for given datasets

    private double[][] Q; // table Q[algorithm_ind][dataset_ind] = EARR
    private MLAlgorithm mlAlgo;
    private QueryManager queryManager;

    ArrayList<ArrayList<Double>> featureValues; // get all possible values of feature by id

    private final int b; // size of query batch

    private final int alpha; // param which controls the complexity of the search, random sub-sample of
                             // alpha * b queries is selected from the available pool

    private double[][] C; // cost matrix for all instance-feature pairs

    private static final double DEFAULT_WEIGHT = 1.0; // weight for instances, not used and so equal for all


    private ArrayList<J48> featureClassifiers; // for getProb(..) computing

    public SampledExpectedUtility(ArrayList<FSSAlgorithm> algorithms,
                                  ArrayList<Dataset> dataSets,
                                  ArrayList<Integer> bestFSSAlgorithms,
                                  double[][] Q,
                                  MLAlgorithm mlAlgo,
                                  QueryManager queryManager,
                                  ArrayList<ArrayList<Double>> featureValues,
                                  int b,
                                  int alpha) {
        this.algorithms = algorithms;
        this.dataSets = dataSets;
        this.bestFSSAlgorithms = bestFSSAlgorithms;
        this.Q = Q;
        this.mlAlgo = mlAlgo;
        this.queryManager = queryManager;
        this.featureValues = featureValues;
        this.b = b;
        this.alpha = alpha;
    }

    public J48 performSEU() throws Exception {
        Set<Pair<Integer, Integer>> curMissingCells = new HashSet<>();
        for (int i = 0; i < Q.length; ++i) {
            for (int j = 0; j < Q[0].length; ++j) {
                if (isValueMissing(Q[i][j])) {
                    curMissingCells.add(new Pair<>(j, i));
                }
            }
        }

//        curMissingCells = chooseSubsetOfMissingCells(curMissingCells);

        J48 classifier;
        boolean needToStop = false;
        while (!needToStop) {
            classifier = makeMainClassifier();
            featureClassifiers = new ArrayList<>(algorithms.size());
            List<Pair<Double, Pair<Integer, Integer>>> scores = new ArrayList<>();
            for (Pair<Integer, Integer> query : curMissingCells) {
                double score = getScore(query, classifier);
                scores.add(new Pair<>(score, query));
            }

            // Choose best 'b' queries to acquire:
            Collections.sort(scores);
            ArrayList<Pair<Integer, Integer>> bestQueries = scores.subList(0, b).stream()
                    .map(pair -> pair.second)
                    .collect(Collectors.toCollection(ArrayList::new));

            for (Pair<Integer, Integer> query : bestQueries) {
                acquireQuery(query);
            }
        }

        classifier = makeMainClassifier();
        return classifier;
    }

    private void acquireQuery(Pair<Integer, Integer> query) throws Exception {
        int algoInd = query.second;
        int datasetInd = query.first;
        Evaluator newEvaluator = new Evaluator(queryManager);
        Q[query.second][query.first] = newEvaluator.evaluate(algorithms.get(algoInd).toDbObject(),
                                                             dataSets.get(datasetInd),
                                                             mlAlgo);
    }

    /**
     * Get expected utility of possible query.
     *
     * @return
     */
    private double getScore(Pair<Integer, Integer> query, J48 classifier) throws Exception {
        double score = 0.0;
        ArrayList<Double> oneFeatureValues = featureValues.get(query.second);
        for (int i = 0; i < oneFeatureValues.size(); ++i) {
            score += getProb(query, i) * getUtility(query, oneFeatureValues.get(i), classifier);
        }
        return score;
    }

    /**
     * The probability that 'query' has the value 'value'.
     *
     * @param query
     * @param valueInd
     * @return
     */
    private double getProb(Pair<Integer, Integer> query, int valueInd) throws Exception {
        int featId = query.second;
        if (featureClassifiers.get(featId) == null) {
            featureClassifiers.set(featId, makeClassifierForFeature(featId));
        }
        Instance inst = makeInstanceForFeature(query.second, query.first);
        inst.setClassMissing(); // not classified yet

        // class probabilities for an instance:
        double[] probs = featureClassifiers.get(featId).distributionForInstance(inst);
        return probs[valueInd];
    }


    /**
     * The utility of knowing that the 'query' has the value 'value'.
     * Formula:
     * U(query = value) = A(Q, query = value) − A(Q) /  C[query],
     * where A(..) - accuracy.
     *
     * @param query
     * @param value
     * @return
     */
    private double getUtility(Pair<Integer, Integer> query, double value, J48 classifier) throws Exception {
        double oldAcc = getAccuracyOfClassifier(classifier);

        // make classifier for which Q[query] = value
        Q[query.second][query.first] = value;
        J48 newClassifier = makeMainClassifier();
        double newAcc = getAccuracyOfClassifier(newClassifier);
        Q[query.second][query.first] = 0;

        return (newAcc - oldAcc) / C[query.second][query.first];
    }


    // TODO: 1. Is it ok way to calc accuracy for multi-label classification?
    // TODO: 2. Optimize creating of instances!
    private double getAccuracyOfClassifier(J48 classifier) throws Exception {
        int positive = 0;

        Instances instances = makeInstances();
        for (int i = 0; i < instances.numInstances(); ++i) {
            if (classifier.classifyInstance(instances.instance(i)) == bestFSSAlgorithms.get(i)) {
                ++positive;
            }
        }

        return (double) positive / instances.numInstances();
    }

    /**
     * Given current matrix 'Q' and labels 'bestFSSAlgorithms' return C4.5
     * classifier.
     *
     * @return
     * @throws Exception
     */
    private J48 makeMainClassifier() throws Exception {
        Instances instances = makeInstances();
        J48 classifier = new J48();
        classifier.setUseLaplace(true); // todo as in paper
        classifier.buildClassifier(instances);
        return classifier;
    }

    private Instances makeInstances() throws Exception {
        ArrayList<Attribute> attrs = makeAttributesForMainClass();
        Instances instances = new Instances("Features of all instances",
                                            attrs,
                                            Q[0].length);

        for (int i = 0; i < Q[0].length; ++i) {
            instances.add(makeInstance(i));
        }

        return instances;
    }

    private Instance makeInstance(int datasetInd) {
        Set<Integer> missingCells = new HashSet<>();

        double[] attValues = new double[Q.length];
        for (int i = 0; i < Q.length; ++i) {
            if (isValueMissing(Q[i][datasetInd])) {
                missingCells.add(i);
            } else {
                attValues[i] = Q[i][datasetInd];
            }
        }

        Instance inst = new Instance(DEFAULT_WEIGHT, attValues);
        inst.setClassValue(bestFSSAlgorithms.get(datasetInd));
        missingCells.forEach(inst::setMissing);
        return inst;
    }


    private J48 makeClassifierForFeature(int algoInd) throws Exception {
        ArrayList<Attribute> attrs = makeAttributesForFeatureClass(algoInd);

        Instances instances = new Instances("Instances for feature #" + algoInd,
                                            attrs,
                                            Q.length);

        for (int i = 0; i < Q[0].length; ++i) {
            if (Q[algoInd][i] == 0.0) {
                continue;
            }
            instances.add(makeInstanceForFeature(algoInd, i));
        }

        J48 classifier = new J48();
        classifier.setUseLaplace(true); // todo as in paper
        classifier.buildClassifier(instances);
        return classifier;
    }

    private Instance makeInstanceForFeature(int algoInd, int datasetInd) {
        Set<Integer> missingCells = new HashSet<>();

        double[] attValues = new double[Q.length];
        for (int j = 0; j < algoInd; ++j) {
            if (isValueMissing(Q[j][datasetInd])) {
                missingCells.add(j);
            } else {
                attValues[j] = Q[j][datasetInd];
            }
        }

        for (int j = algoInd + 1; j < Q.length; ++j) {
            if (isValueMissing(Q[j][datasetInd])) {
                missingCells.add(j - 1);
            } else {
                attValues[j - 1] = Q[j][datasetInd];
            }
        }

        attValues[Q.length - 1] = bestFSSAlgorithms.get(datasetInd); // real label is now also a predictor
        Instance inst = new Instance(DEFAULT_WEIGHT, attValues);
        missingCells.forEach(inst::setMissing);
        inst.setClassValue(Q[algoInd][datasetInd]);

        return inst;
    }

    // todo optimize attributes
    private ArrayList<Attribute> makeAttributesForMainClass() {
        ArrayList<Attribute> attrs = new ArrayList<>(Q.length);
        for (int j = 0; j < Q.length; ++j) {
            attrs.add(new Attribute("Feature #" + j));
        }

        return attrs;
    }

    // todo optimize attributes
    private ArrayList<Attribute> makeAttributesForFeatureClass(int algoInd) {
        ArrayList<Attribute> attrs = new ArrayList<>(Q.length);
        for (int j = 0; j < algoInd; ++j) {
            attrs.add(new Attribute("Feature #" + j));
        }

        for (int j = algoInd + 1; j < Q.length; ++j) {
            attrs.add(new Attribute("Feature #" + j));
        }

        attrs.add(new Attribute("Label"));

        return attrs;
    }


    //  TODO generalize
    public static boolean isValueMissing(double value) {
        return value == 0.0;
    }
}























