package ru.ifmo.ctddev.FSSARecSys.calculators;

import ru.ifmo.ctddev.FSSARecSys.db.DataSet;

/**
 * This interface provide {@link DataSet} metric computer
 */
public interface DataSetDistancesComputer {
    /**
     * @return Distance between given {@link DataSet}s
     */
    double calculate(DataSet dataSetFirst, DataSet dataSetSecond);
}
