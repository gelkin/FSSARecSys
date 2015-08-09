package ru.ifmo.ctddev.FSSARecSys.calculators.internal;

import ru.ifmo.ctddev.FSSARecSys.FSSResult;
import ru.ifmo.ctddev.FSSARecSys.calculators.FSSAlgorithm;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.core.Instances;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Created by warrior on 04.03.15.
 */
public class FSSAlgorithmImpl implements FSSAlgorithm {

    private final String name;
    private final ASSearch search;
    private final ASEvaluation evaluation;

    public FSSAlgorithmImpl(String name, ASSearch search, ASEvaluation evaluation) {
        this.name = Objects.requireNonNull(name);
        this.search = Objects.requireNonNull(search);
        this.evaluation = Objects.requireNonNull(evaluation);
//        try {
//            Object a = Class.forName("weka.attributeSelection.ASSearch");
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public FSSResult run(String dataSetName, Instances instances) {
        ASSearch localSearch = null;
        ASEvaluation localEvaluation = null;
        try {
            localSearch = ASSearch.makeCopies(search, 1)[0];
            localEvaluation = ASEvaluation.makeCopies(evaluation, 1)[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (localSearch != null && localEvaluation != null) {
            try {
                Instant start = Instant.now();
                int[] selectedAttrs = localSearch.search(localEvaluation, instances);
                Instant end = Instant.now();
                return new FSSResult(dataSetName, name, selectedAttrs, Duration.between(start, end).toMillis());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new FSSResult(dataSetName, name, new int[0], 0);
    }
}
