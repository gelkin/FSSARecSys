package ru.ifmo.ctddev.FSSARecSys.calculators;

import ru.ifmo.ctddev.FSSARecSys.db.DataSet;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

public interface NearestDataSetSearcher {
    /**
     * @param n count of nearest DataSet to return
     */
    public Pair<Double, DataSet>[] search(DataSet dataSet, int n);
}
