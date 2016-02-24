package ru.ifmo.ctddev.FSSARecSys.db.internal;

import ru.ifmo.ctddev.FSSARecSys.FSSResult;
import ru.ifmo.ctddev.FSSARecSys.calculators.internal.FSSAlgorithmImpl;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.core.Instances;

/**
 * Created by sergey on 28.03.15.
 */
public class FSSAlgorithm implements ru.ifmo.ctddev.FSSARecSys.calculators.FSSAlgorithm {
    String fssAlgoName;
    String searchClass;
    String evalClass;
    String searchOptions;
    String evalOptions;


    public FSSAlgorithm (String fssAlgoName, String searchClass, String evalClass,
                         String searchOptions, String evalOptions) {
        this.fssAlgoName = fssAlgoName;
        this.searchClass = searchClass;
        this.evalClass = evalClass;
        this.searchOptions = searchOptions;
        this.evalOptions = evalOptions;
    }



    public String getFssAlgoName() {
        return fssAlgoName;
    }

    public String getSearchClass() {
        return searchClass;
    }

    public String getEvalClass() {
        return evalClass;
    }

    public String getEvalOptions() {
        return evalOptions;
    }

    public String getSearchOptions() {
        return searchOptions;
    }

    @Override
    public String getName() {
        return fssAlgoName;
    }

    @Override
    public FSSResult run(String dataSetName, Instances instances) {
        return null;
    }

    @Override
    public FSSAlgorithm toDbObject() {
        return null;
    }


}
