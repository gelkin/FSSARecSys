package ru.ifmo.ctddev.FSSARecSys;


/**
 * read-only structure
 */
public class EvaluationResult {
    public EvaluationResult(float featureSelectionTime, int numberOfSelectedFeatures, int[] selectedFeatures, float accuracy, float F1Measure) {
        this.featureSelectionTime = featureSelectionTime;
        this.numberOfSelectedFeatures = numberOfSelectedFeatures;
        this.selectedFeatures = selectedFeatures;
        this.accuracy = accuracy;
        this.F1Measure = F1Measure;
    }

    /**
     * time of selecting features
     */
    public final float featureSelectionTime;

    /**
     * number of features selected by FSSAlgorithm
     */
    public final int numberOfSelectedFeatures;

    /**
     * list of selected features
     * NOTE: different for each algorithm but independent of classifiers
     * NOTE2: order is essential here
     */
    public final int[] selectedFeatures;

    /**
     * Accuracy computed on given DataSet with given Classifier
     */
    public final float accuracy;

    /**
     * F1 measure computed on given DataSet with given Classifier
     */
    public final float F1Measure;
}
