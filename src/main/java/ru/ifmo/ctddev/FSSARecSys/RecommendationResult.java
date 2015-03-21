package ru.ifmo.ctddev.FSSARecSys;

import ru.ifmo.ctddev.FSSARecSys.calculators.FSSAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.db.DataSet;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

public interface RecommendationResult {
    /**
     * @return ordered array of {@link Pair}s of weight and {@link FSSAlgorithm}s.
     */
    Pair<Double, FSSAlgorithm>[] getWeightedResults();

    /**
     * @return ordered array of {@link FSSAlgorithm}.
     */
    FSSAlgorithm[] getResults();

    /**
     * @return best {@link FSSAlgorithm} for this {@link DataSet}.
     */
    FSSAlgorithm getBestResult();

    /**
     * @return {@link DataSet} for which recommendation was executed
     */
    DataSet getDataSet();
}
