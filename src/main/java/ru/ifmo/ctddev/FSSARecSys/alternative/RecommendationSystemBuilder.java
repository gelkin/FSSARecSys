package ru.ifmo.ctddev.FSSARecSys.alternative;

import ru.ifmo.ctddev.FSSARecSys.db.internal.Dataset;
import ru.ifmo.ctddev.FSSARecSys.db.internal.FSSAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.db.internal.MLAlgorithm;

import java.util.ArrayList;

// todo: stub for recommendation system builder. Main method: 'makeRecommendationSystem'

public class RecommendationSystemBuilder {

    public static TestRecSystem makeRecommendationSystem(ArrayList<FSSAlgorithm> algorithms,
                                                        ArrayList<Dataset> dataSets,
                                                        double[][] Q,
                                                        MLAlgorithm mlAlgo) {
        return new TestRecSystem(algorithms, dataSets, Q, mlAlgo);
    }
}
