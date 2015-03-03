package ru.ifmo.ctddev.FSSARecSys;


import java.util.List;

public class ClassifierResult {
    /**
     * Accuracy computed on given DataSet with given Classifier
     */
    public double accuracy;

    /**
     * F1 measure computed on given DataSet with given Classifier
     */
    public double f1Measure;

    public ClassifierResult(double accuracy, double f1Measure) {
        this.accuracy = accuracy;
        this.f1Measure = f1Measure;
    }

    public static ClassifierResult averageClassifierResult(List<ClassifierResult> results) {
        double averageAccuracy = 0;
        double averageF1Measure = 0;
        for (ClassifierResult result : results) {
            averageAccuracy += result.accuracy;
            averageF1Measure += result.f1Measure;
        }
        averageAccuracy /= results.size();
        averageF1Measure /= results.size();
        return new ClassifierResult(averageAccuracy, averageF1Measure);
    }
}
