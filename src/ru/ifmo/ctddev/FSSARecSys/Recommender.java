package ru.ifmo.ctddev.FSSARecSys;

import javafx.util.Pair;
import ru.ifmo.ctddev.FSSARecSys.calculators.EARRComputer;
import ru.ifmo.ctddev.FSSARecSys.calculators.FSSAlgorithmRanker;
import ru.ifmo.ctddev.FSSARecSys.calculators.NearestDataSetSearcher;
import ru.ifmo.ctddev.FSSARecSys.db.Classifier;
import ru.ifmo.ctddev.FSSARecSys.db.DataSet;
import ru.ifmo.ctddev.FSSARecSys.db.FSSAlgorithm;

import java.io.File;
import java.util.Collection;

public class Recommender {
    private float alpha = 0;
    private float betta = 0;
    private Classifier classifier;  // todo = Classifier.DEFAULT
    private Collection<FSSAlgorithm> algorithms;  // todo = FSSAlgorithms.ALL

    public RecommendationResult recommend(DataSet dataSet) {
        Pair<Double, DataSet>[] dataSetDistances = NearestDataSetSearcher.search(dataSet, 10);
        Pair<Double, DataSet>[] dataSetsEARRs = EARRComputer.calculate(classifier, algorithms, dataSetDistances);
        return FSSAlgorithmRanker.rank(dataSetDistances, dataSetsEARRs, alpha, betta);
    }

    private Pair<Double, DataSet>[] EARRComputer(Pair<Double, DataSet>[] dataSetDistances) {
        return null;
    }

    public Recommender setClassifier(Classifier classifier) {
        return this;
    }

    /**
     * setting set of algorithms that are used for recomendation
     */
    public Recommender setAlgorithmsSet(Collection<FSSAlgorithm> algorithms) {
        return this;
    }

    public Recommender setParameters(float alpha, float betta) {
        return this;
    }

    public static void main(String[] ignore) {
        System.out.println(new Recommender().setParameters(0, 0).recommend(new DataSet("Test", new File("path to arff"))));
    }
}
