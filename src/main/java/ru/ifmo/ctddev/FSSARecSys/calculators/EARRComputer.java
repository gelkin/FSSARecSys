package ru.ifmo.ctddev.FSSARecSys.calculators;

import ru.ifmo.ctddev.FSSARecSys.db.DataSet;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

public interface EARRComputer {
    public Pair<Double, DataSet>[] calculate(ClassifierEvaluator classifierEvaluator, FSSAlgorithm[] algorithms, Pair<Double, DataSet>[] dataSets);
}
