package ru.ifmo.ctddev.FSSARecSys.alternative.evaluation;

import ru.ifmo.ctddev.FSSARecSys.calculators.FSSAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.db.internal.Dataset;

/**
 * Created by Сергей on 03.08.2015.
 */
public class Evaluator {
    public Double evaluate(FSSAlgorithm fssAlgorithm, Dataset dataset){
        switch (dataset.getTaskType()){
            case "classification":
                break;
            case "clusterisation":
                break;
        }
        return null;
    }
}
