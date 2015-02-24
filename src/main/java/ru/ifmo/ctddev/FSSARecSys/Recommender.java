package ru.ifmo.ctddev.FSSARecSys;

import ru.ifmo.ctddev.FSSARecSys.calculators.Evaluator;
import ru.ifmo.ctddev.FSSARecSys.calculators.FSSAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.db.DataSet;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.Collection;

public class Recommender {
    private float alpha = 0;
    private float betta = 0;
    private Evaluator evaluator;  // todo = Classifier.DEFAULT
    private Collection<FSSAlgorithm> algorithms;  // todo = FSSAlgorithms.ALL

    public RecommendationResult recommend(DataSet dataSet) {
//        Pair<Double, DataSet>[] dataSetDistances = NearestDataSetSearcher.search(dataSet, 10);
//        Pair<Double, DataSet>[] dataSetsEARRs = EARRComputer.calculate(evaluator, algorithms, dataSetDistances);
//        return FSSAlgorithmRanker.rank(dataSetDistances, dataSetsEARRs, alpha, betta);
        return null;
    }

    private Pair<Double, DataSet>[] EARRComputer(Pair<Double, DataSet>[] dataSetDistances) {
        return null;
    }

    public Recommender setClassifier(Evaluator evaluator) {
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
//        System.out.println(new Recommender().setParameters(0, 0).recommend(new DataSet("Test", new File("path to arff"))));
    }
}
