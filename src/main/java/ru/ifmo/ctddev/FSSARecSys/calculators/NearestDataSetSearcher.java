package ru.ifmo.ctddev.FSSARecSys.calculators;

import ru.ifmo.ctddev.FSSARecSys.db.DataSet;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

/**
 * This interface provide method for searching nearest {@link DataSet} to given
 */
public interface NearestDataSetSearcher {
    /**
     * @param n count of nearest DataSet to return
     * @return ordered Pairs of distances and {@link DataSet}
     */
    Pair<Double, DataSet>[] search(DataSet dataSet, int n);
}
