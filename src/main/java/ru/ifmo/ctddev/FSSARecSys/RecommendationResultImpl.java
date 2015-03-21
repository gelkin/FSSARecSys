package ru.ifmo.ctddev.FSSARecSys;

import ru.ifmo.ctddev.FSSARecSys.calculators.FSSAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.db.DataSet;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.Arrays;

public class RecommendationResultImpl implements RecommendationResult {
    private final DataSet dataSet;
    private final Pair<Double, FSSAlgorithm>[] results;

    /**
     * @param dataSet for which recommendation was executed
     * @param results ordered runked array of {@link FSSAlgorithm}s
     */
    public RecommendationResultImpl(DataSet dataSet, Pair<Double, FSSAlgorithm>[] results) {
        this.dataSet = dataSet;
        this.results = results;
    }

    /**
     * @return ordered array of {@link Pair}s of weight and {@link FSSAlgorithm}s.
     */
    @Override
    public Pair<Double, FSSAlgorithm>[] getWeightedResults() {
        return results;
    }

    /**
     * @return ordered array of {@link FSSAlgorithm}.
     */
    @Override
    public FSSAlgorithm[] getResults() {
        return (FSSAlgorithm[]) Arrays.stream(results).map(pair -> pair.second).toArray();
    }

    /**
     * @return best {@link FSSAlgorithm} for this {@link DataSet}.
     */
    @Override
    public FSSAlgorithm getBestResult() {
        return results[0].second;
    }

    /**
     * @return {@link DataSet} for which recommendation was executed
     */
    @Override
    public DataSet getDataSet() {
        return dataSet;
    }
}
