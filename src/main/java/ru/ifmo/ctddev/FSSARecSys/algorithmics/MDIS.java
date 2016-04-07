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

    private double[][] Q;
    private MLAlgorithm mlAlgo;
    private QueryManager queryManager;

    private Set<Integer> completeDatasets = new HashSet<>(); // set of dataset with complete Q table
    private Set<Integer> incompleteDatasets = new HashSet<>(); // all datasets without completeDatasets

    private static final double AMB_COEF = 2.0; // tunable multiplier of |C i−1 | depends on how ambitious one
                                                // wants to be especially early on when |C i−1 | is not too large

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
                if (Q[j][i] == 0.0) {
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
     * @param k - number of instances to query
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
            double p = getProbability(recSystem, i, algorithms.get(bestFSSAlgorithms.get(i)));
            double score = p * (1 - p) / ((-2 * b + 1) * p + Math.pow(b, 2.0));
            scores.add(new Pair<>(score, i));
        }

        // Choose best 'k' datasets to query:
        Collections.sort(scores);
        ArrayList<Integer> datasetsToQuery = scores.subList(0, k).stream()
                .map(pair -> pair.second)
                .collect(Collectors.toCollection(ArrayList::new));

        for (Integer i : datasetsToQuery) {
            queryDataset(i);
        }

        for (Integer i : datasetsToQuery) {
            completeDatasets.add(i);
            incompleteDatasets.remove(i);
        }


        return new Pair<>(datasetsToQuery, RecommendationSystemBuilder.makeRecommendationSystem(algorithms, dataSets, Q, mlAlgo));
    }

    private double getProbability(RecommendationSystem recSystem, int datasetIndex, FSSAlgorithm resAlgo) {
        List<Integer> missingCells = getMissingCells(datasetIndex);
        // TODO
        return 0.0;
    }

    private List<Integer> getMissingCells(int datasetIndex) {
        List<Integer> missingCells = new ArrayList<>();
        for (int i = 0; i < Q.length; ++i) {
            if (Q[i][datasetIndex] == 0.0) {
                missingCells.add(i);
            }
        }

        return missingCells;
    }

    private void queryDataset(int datasetIndex) throws Exception { // todo throw Exception?
        for (int i = 0; i < Q.length; ++i) {
            if (Q[i][datasetIndex] == 0.0) {
                Evaluator newEvaluator = new Evaluator(queryManager);
                Q[i][datasetIndex] = newEvaluator.evaluate(algorithms.get(i).toDbObject(),
                        dataSets.get(datasetIndex),
                        mlAlgo);
            }
        }
    }
}





















