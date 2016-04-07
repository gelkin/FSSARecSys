package ru.ifmo.ctddev.FSSARecSys.alternative;

import ru.ifmo.ctddev.FSSARecSys.db.internal.Dataset;
import ru.ifmo.ctddev.FSSARecSys.db.internal.FSSAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.db.internal.MLAlgorithm;

import java.util.ArrayList;

// todo: stub for recommendation system. Main method: 'getBestFSSAlgorithm'

public class TestRecSystem extends RecommendationSystem {
    private ArrayList<FSSAlgorithm> algorithms;
    private ArrayList<Dataset> dataSets;
    private double[][] Q;
    private MLAlgorithm mlAlgo;

    public TestRecSystem(ArrayList<FSSAlgorithm> algorithms,
                         ArrayList<Dataset> dataSets,
                         double[][] Q,
                         MLAlgorithm mlAlgo) {
        this.algorithms = algorithms;
        this.dataSets = dataSets;
        this.Q = Q;
        this.mlAlgo = mlAlgo;
    }

    // Always returns first FSSAlgorithm
    public FSSAlgorithm getBestFSSAlgorithm(Dataset dataset) {
        assert algorithms.size() > 0;

        return algorithms.get(0);
    }
}
