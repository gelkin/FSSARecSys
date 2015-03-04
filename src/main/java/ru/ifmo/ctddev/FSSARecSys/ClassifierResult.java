package ru.ifmo.ctddev.FSSARecSys;

import java.util.List;
import java.util.Objects;

public class ClassifierResult {

    public String dataSetName;

    public String classifierName;

    /**
     * Accuracy computed on given DataSet with given Classifier
     */
    public double accuracy;

    /**
     * F1 measure computed on given DataSet with given Classifier
     */
    public double f1Measure;

    public ClassifierResult(String dataSetName, String classifierName, double accuracy, double f1Measure) {
        this.dataSetName = Objects.requireNonNull(dataSetName);
        this.classifierName = Objects.requireNonNull(classifierName);
        this.accuracy = accuracy;
        this.f1Measure = f1Measure;
    }

    public static ClassifierResult averageClassifierResult(List<ClassifierResult> results) {
        if (results == null || results.size() == 0) {
            throw new IllegalArgumentException("results list must be non empty");
        }
        double averageAccuracy = 0;
        double averageF1Measure = 0;
        for (ClassifierResult result : results) {
            averageAccuracy += result.accuracy;
            averageF1Measure += result.f1Measure;
        }
        averageAccuracy /= results.size();
        averageF1Measure /= results.size();
        String dataSetName = results.get(0).dataSetName;
        String classifierName = results.get(0).classifierName;
        return new ClassifierResult(dataSetName, classifierName, averageAccuracy, averageF1Measure);
    }
}
