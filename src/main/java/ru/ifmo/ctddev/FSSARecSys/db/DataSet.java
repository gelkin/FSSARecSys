package ru.ifmo.ctddev.FSSARecSys.db;

import ru.ifmo.ctddev.FSSARecSys.EvaluationResult;
import ru.ifmo.ctddev.FSSARecSys.FSSResult;
import ru.ifmo.ctddev.FSSARecSys.calculators.Evaluator;
import ru.ifmo.ctddev.FSSARecSys.calculators.FSSAlgorithm;
import weka.core.Instances;

public interface DataSet {
    public String getName();

    /**
     * if it has class index it should be already applied
     */
    public Instances getInstances();

    public String[] getAvailableMeatFeatureNames();
    public double getMetaFeature(String name);
    public double[] getMetaFeatures(String[] names);
    public void updateMetaFeature(String name, double value);
    public void updateMetaFeatures(String[] names, double[] values);

    /**
     * extract and save using results:
     * selection time
     * count of selected features
     * selected features in order
     */
    public void selectFeatures(FSSAlgorithm algorithm);
    public FSSResult getFSSResult(FSSAlgorithm algorithm);
    public void updateSelectionResult(FSSAlgorithm algorithm, FSSResult result);

    /**
     * evaluate(classify or cluster) selected by algorithm features and store result
     */
    public void evaluate(FSSAlgorithm algorithm, Evaluator evaluator);
    public EvaluationResult getEvaluationResult(Evaluator evaluator, FSSAlgorithm algorithm);
    public void updateEvaluationResult(Evaluator evaluator, FSSAlgorithm algorithm, EvaluationResult result);
}
