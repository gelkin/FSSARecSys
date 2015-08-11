package ru.ifmo.ctddev.FSSARecSys.alternative.evaluation;

import ru.ifmo.ctddev.FSSARecSys.alternative.evaluation.classification.*;
import ru.ifmo.ctddev.FSSARecSys.db.internal.*;
import ru.ifmo.ctddev.FSSARecSys.db.manager.*;

import java.util.*;

/**
 * Created by Сергей on 03.08.2015.
 */
public class Evaluator {
    private QueryManager queryManager;
    private List<String> listOfMetaFeatures;

    //for classification tasks
    private float alpha;
    private float betta;

    public Evaluator(QueryManager queryManager, List<String> listOfFeatures) {
        this.queryManager = queryManager;
        this.listOfMetaFeatures = listOfFeatures;
    }

    public void setAlphaAndBeta(float alpha, float betta) {
        this.alpha = alpha;
        this.betta = betta;
    }

    public Double evaluate(FSSAlgorithm fssAlgorithm, Dataset dataset, MLAlgorithm mlAlgorithm) throws Exception {
        switch (dataset.getTaskType()){
            case "classification":
                if (queryManager.getDataset(dataset.getName()) == null) {
                    queryManager.addDataset(dataset);
                }
                ArrayList<FSSAlgorithm> availableFSSAlgorithms = queryManager.getAvailableFssAlgorithms(); //todo: get from db

                EARREvaluation  earrEvaluation = new EARREvaluation(alpha, betta, queryManager);
                return earrEvaluation.evaluate(fssAlgorithm, availableFSSAlgorithms, dataset, listOfMetaFeatures, mlAlgorithm);
            case "clusterisation":
                break;
        }
        return null;
    }
}
