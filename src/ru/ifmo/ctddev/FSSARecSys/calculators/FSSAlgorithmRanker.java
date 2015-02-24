package ru.ifmo.ctddev.FSSARecSys.calculators;

import ru.ifmo.ctddev.FSSARecSys.utils.Pair;
import ru.ifmo.ctddev.FSSARecSys.db.DataSet;
import ru.ifmo.ctddev.FSSARecSys.RecommendationResult;

public interface FSSAlgorithmRanker {
    public RecommendationResult rank(Pair<Double, DataSet>[] dataSetDistances, Pair<Double, DataSet>[] dataSetsEARRs, float alpha, float betta);
}
