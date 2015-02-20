package ru.ifmo.ctddev.FSSARecSys.db;

import ru.ifmo.ctddev.FSSARecSys.EvaluationResult;
import weka.core.Instances;

import java.io.File;


public class DataSet extends ORM {
    public DataSet(String name, File pathToARRFFile) {
        this(name, pathToARRFFile, 0);
    }

    public DataSet(String name, File pathToARRFFile, int classIndex) {

    }

    public String getName() {return null;}

    public int getClassIndex() {  // or it's useless because it's always 0?
        return 0;
    }

    /**
     * This should be implemented lazy way with caching!
     */
    public Instances getDataSet() {
        return null;
    }


    // think about return type for this 2 methods:
    // Do MFs always doubles?

    /**
     * load it value from DB
     */
    public double getMetaFeature(String name) {
        return 0;
    }

    /**
     * load their values from DB
     */
    public double[] getMetaFeatures(String[] names) {
        return null;
    }

    /**
     * save it value to DB
     */
    public void setMetaFeature(String name, double value) {
    }

    /**
     * save their values to DB
     */
    public void setMetaFeatures(String[] names, double[] values) {
    }

    public EvaluationResult getEvaluationResult(Classifier classifier, FSSAlgorithm algorithm) {
        return null;
    }

    public double[] extractMetaFeatures(String[] metaFeaturesNames) {
        // Should we save result here?
        // Where gets generated metaFeaturesNames?
        // How it gets transformed to MetaFeatureExtractor?
        return null;
    }

    public void updateFeaturesExtractionResult(FSSAlgorithm algorithm, float featureSelectionTime, int numberOfSelectedFeatures, int[] selectedFeatures) {

    }

    public void updateFeaturesClassificationResult(FSSAlgorithm algorithm, Classifier classifier, float accuracy, float F1Measure) {

    }
}
