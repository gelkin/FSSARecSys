package ru.ifmo.ctddev.FSSARecSys.calculators;

import ru.ifmo.ctddev.FSSARecSys.ClassifierResult;
import weka.core.Instances;

public interface ClassifierEvaluator {
    public ClassifierResult evaluate(String dataSetName, Instances train, Instances test);
}
