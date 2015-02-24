package ru.ifmo.ctddev.FSSARecSys.calculators;

import ru.ifmo.ctddev.FSSARecSys.FSSResult;
import ru.ifmo.ctddev.FSSARecSys.db.DataSet;

public interface FSSAlgorithm {
    public String getName();
    public FSSResult run(DataSet dataSet);
}
