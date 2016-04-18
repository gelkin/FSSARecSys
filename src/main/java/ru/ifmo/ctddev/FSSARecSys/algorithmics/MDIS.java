package ru.ifmo.ctddev.FSSARecSys.algorithmics;

import ru.ifmo.ctddev.FSSARecSys.alternative.TestRecSystem;
import ru.ifmo.ctddev.FSSARecSys.alternative.RecommendationSystem;
import ru.ifmo.ctddev.FSSARecSys.alternative.RecommendationSystemBuilder;
import ru.ifmo.ctddev.FSSARecSys.alternative.evaluation.Evaluator;
import ru.ifmo.ctddev.FSSARecSys.db.internal.Dataset;
import ru.ifmo.ctddev.FSSARecSys.db.internal.FSSAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.db.internal.MLAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.db.manager.QueryManager;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;
import weka.estimators.KernelEstimator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Maximum distance instance selection algorithm.
 *
 * Let's consider a given table of form instance/feature (or dataset/FSSAlgorithm)
 * with missing cells. We have an ability to fill arbitrary 'k' instances (i.e.
 * fill all missing cells for them). MDIS algorithm chooses such 'k' instances,
 * that classification algorithm (or recommendation system) based on the table
 * updated with these 'k' complete instances will be best among all other
 * classification algorithms, based on any other 'k' complete instances.
 * In this implementation of MDIS, after choosing 'k' instances, it also
 * completes them by itself.
 */

public class MDIS {
    private ArrayList<FSSAlgorithm> algorithms;

    private ArrayList<Dataset> dataSets;
    private ArrayList<Integer> bestFSSAlgorithms; // indexes of best FSSAlgorithms for given datasets

    private double[][] Q; // table Q[algorithm_ind][dataset_ind] = EARR
    private MLAlgorithm mlAlgo;
    private QueryManager queryManager;

    private Set<Integer> completeDatasets = new HashSet<>(); // set of (indexes of) datasets with complete Q table
    private Set<Integer> incompleteDatasets = new HashSet<>(); // all (indexes of) datasets without completeDatasets

    private static final double AMB_COEF = 2.0; // tunable multiplier of |C i−1 | depends on how ambitious one
                                                // wants to be especially early on when |C i−1 | is not too large

    private static final double PRECISION = 0.01; // precision for density estimation

    public MDIS(ArrayList<FSSAlgorithm> algorithms,
                ArrayList<Dataset> dataSets,
                ArrayList<Integer> bestFSSAlgorithms,
                double[][] Q,
                MLAlgorithm mlAlgo,
                QueryManager queryManager) {

        this.algorithms = algorithms;
        this.dataSets = dataSets;
        this.bestFSSAlgorithms = bestFSSAlgorithms;
        this.Q = Q;
        this.mlAlgo = mlAlgo;
        this.queryManager = queryManager;

        initCompleteAndIncompleteDatasets();
    }

    private void initCompleteAndIncompleteDatasets() {
        assert Q.length > 0;

        datasetLoop:
        for (int i = 0; i < Q[0].length; ++i) {
            for (int j = 0; j < Q.length; ++i) {
                if (isValueMissing(Q[j][i])) {
                    incompleteDatasets.add(i);
                    continue datasetLoop;
                }
            }
            completeDatasets.add(i);
        }
    }

    /**
     * TODO
     *
     * @param recSystem - recommendation system for first iteration (step)
     * @param k - number of instances to acquire
     * @param g - number of iterations (steps)
     * @return Pair:
     * 1. First element of pair: list of set of dataset indexes. Each set contains
     *    indexes of datasets to be queried
     * 2. Second element of pair: list of recommendation systems. I'th recommendation
     *    system corresponds
     *
     */
    public Pair<ArrayList<List<Integer>>,
            ArrayList<TestRecSystem>> preformMDIS(TestRecSystem recSystem, int k, int g) throws Exception {
        ArrayList<List<Integer>> allQueriedInstances = new ArrayList<>(g);
        ArrayList<TestRecSystem> allRecSystems = new ArrayList<>(g);
        allRecSystems.add(recSystem);

        for (int i = 0; i < g; ++i) {
            Pair<List<Integer>, TestRecSystem> curResults = performOneIterationMDIS(recSystem, k);
            allQueriedInstances.add(curResults.first);
            allRecSystems.add(curResults.second);
        }

        return new Pair<>(allQueriedInstances, allRecSystems);
    }

    private Pair<List<Integer>, TestRecSystem> performOneIterationMDIS(TestRecSystem recSystem, int k) throws Exception {
        List<Pair<Double, Integer>> scores = new ArrayList<>();

        // Asymmetric uncertainty:
        double b = Math.min(0.5 + k / (AMB_COEF * completeDatasets.size()), 1.0);
        for (Integer i : incompleteDatasets) {
            double p = getProbability(recSystem, i, bestFSSAlgorithms.get(i));
            double score = p * (1 - p) / ((-2 * b + 1) * p + Math.pow(b, 2.0));
            scores.add(new Pair<>(score, i));
        }

        // Choose best 'k' datasets to acquire:
        Collections.sort(scores);
        ArrayList<Integer> bestDatasets = scores.subList(0, k).stream()
                .map(pair -> pair.second)
                .collect(Collectors.toCollection(ArrayList::new));

        for (Integer i : bestDatasets) {
            acquireDataset(i);
        }

        for (Integer i : bestDatasets) {
            completeDatasets.add(i);
            incompleteDatasets.remove(i);
        }


        return new Pair<>(bestDatasets, RecommendationSystemBuilder.makeRecommendationSystem(algorithms, dataSets, Q, mlAlgo));
    }

    private double getProbability(RecommendationSystem recSystem, int datasetIndex, int resAlgoIndex) {
        List<Integer> missingCells = getMissingCells(datasetIndex);

        double[] curDataset = new double[Q.length];
        // Fill not missing features for curDataset
        for (int i = 0; i < Q.length; ++i) {
            if (Q[i][datasetIndex] != 0.0) {
                curDataset[i] = Q[i][datasetIndex];
            }
        }

        // Univariate density estimator
        KernelEstimator estimator = new KernelEstimator(PRECISION);

        // Count all complete
        for (Integer datasetId : completeDatasets) {
            for (Integer cellId : missingCells) {
                curDataset[cellId] = Q[cellId][datasetId];
            }

            int label = getLabelOnEARRs(curDataset);
            estimator.addValue((double) label, 1.0); // TODO should I normalize labels from 0 to 1, which weight to choose? (if not 1.0)
        }

        // Count incomplete
        for (Integer datasetId : incompleteDatasets) {
            boolean isDatasetMatched = true;
            for (Integer cellId : missingCells) {
                if (isValueMissing(Q[cellId][datasetId])) {
                    isDatasetMatched = false;
                    break;
                }
            }
            if (isDatasetMatched) {
                for (Integer cellId : missingCells) {
                    curDataset[cellId] = Q[cellId][datasetId];
                }

                int label = getLabelOnEARRs(curDataset);
                estimator.addValue((double) label, 1.0); // TODO should I normalize labels from 0 to 1, which weight to choose? (if not 1.0)
            }
        }

        return 1 - estimator.getProbability(resAlgoIndex); // TODO prob( xi(dataset) != resAlgo )
    }

    private List<Integer> getMissingCells(int datasetIndex) {
        List<Integer> missingCells = new ArrayList<>();
        for (int i = 0; i < Q.length; ++i) {
            if (isValueMissing(Q[i][datasetIndex])) {
                missingCells.add(i);
            }
        }

        return missingCells;
    }

    private void acquireDataset(int datasetIndex) throws Exception {
        for (int i = 0; i < Q.length; ++i) {
            if (isValueMissing(Q[i][datasetIndex])) {
                Evaluator newEvaluator = new Evaluator(queryManager);
                Q[i][datasetIndex] = newEvaluator.evaluate(algorithms.get(i).toDbObject(),
                        dataSets.get(datasetIndex),
                        mlAlgo);
            }
        }
    }

    // TODO: пока не очень понятно, как оценивать результат классификатора
    // TODO: будем брать алгоритм с максимальным EARR
    // TODO: считаем, что индекс fssAlgorithm'а - это и есть label
    private int getLabelOnEARRs(double[] datasetEARRs) {
        double curMax = Double.MIN_VALUE;
        int fssAlgoIndex = -1;
        for (int i = 0; i < datasetEARRs.length; ++i) {
            if (datasetEARRs[i] > curMax) {
                curMax = datasetEARRs[i];
                fssAlgoIndex = i;
            }
        }

        return fssAlgoIndex;
    }


    public static boolean isValueMissing(double value) {
        return value == 0.0;
    }
}





















