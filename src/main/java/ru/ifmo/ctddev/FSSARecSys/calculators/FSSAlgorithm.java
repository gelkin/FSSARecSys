package ru.ifmo.ctddev.FSSARecSys.calculators;

import ru.ifmo.ctddev.FSSARecSys.FSSResult;
import ru.ifmo.ctddev.FSSARecSys.db.DataSet;
import weka.core.Instances;

public interface FSSAlgorithm {

    public String getName();
    public FSSResult run(String dataSetName, Instances instances);

    public default FSSResult run(DataSet dataSet) {

        return run(dataSet.getName(), dataSet.getInstances());
    }

    public ru.ifmo.ctddev.FSSARecSys.db.internal.FSSAlgorithm toDbObject();
}
