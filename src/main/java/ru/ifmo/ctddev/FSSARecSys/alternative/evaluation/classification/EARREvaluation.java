package ru.ifmo.ctddev.FSSARecSys.alternative.evaluation.classification;

import ru.ifmo.ctddev.FSSARecSys.ClassifierResult;
import ru.ifmo.ctddev.FSSARecSys.FSSResult;
import ru.ifmo.ctddev.FSSARecSys.db.DataSet;
import ru.ifmo.ctddev.FSSARecSys.db.internal.EARRParams;
import ru.ifmo.ctddev.FSSARecSys.db.internal.FSSAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.db.internal.MLAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.db.internal.Metrics;
import ru.ifmo.ctddev.FSSARecSys.db.manager.QueryManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Администратор on 10.08.2015.
 */
public class EARREvaluation {
    private FSSResult fssResult;
    private ClassifierResult classifierResult;
    private float alpha;
    private float betta;
    private QueryManager queryManager;

    public EARREvaluation(FSSResult fssResult, ClassifierResult classifierResult, float alpha, float betta, QueryManager queryManager) {
        this.fssResult = fssResult;
        this.classifierResult = classifierResult;
        this.alpha = alpha;
        this.betta = betta;
        this.queryManager = queryManager;
    }

    private Double EARR_ij(EARRParams params1, EARRParams params2) {
        return (params1.getAccuracy() / params2.getAccuracy()) / (1.0 +
                alpha * Math.log(params1.getSelectionTime() / params2.getSelectionTime()) +
                betta * (params1.getNumberOfSelectedFeatures() / params2.getNumberOfSelectedFeatures()));
    }

    public Double evaluate(FSSAlgorithm algo, ArrayList<FSSAlgorithm> allFSSAlgorithms, String datasetName, List<String> listOfFeatures, MLAlgorithm mlAlgorithm) {
        Double sum = 0.0;
        EARRParams newParams = queryManager.getEARRParams(new Metrics(algo, datasetName, listOfFeatures), mlAlgorithm);
        for (FSSAlgorithm fssAlgorithm : allFSSAlgorithms) {
            if (fssAlgorithm != algo) {
                EARRParams otherParams = queryManager.getEARRParams(new Metrics(fssAlgorithm, datasetName, listOfFeatures), mlAlgorithm);
                if (otherParams != null){
                    sum += EARR_ij(newParams, otherParams);
                }
            }
        }
        return sum / (allFSSAlgorithms.size() - 1);
    }
}
