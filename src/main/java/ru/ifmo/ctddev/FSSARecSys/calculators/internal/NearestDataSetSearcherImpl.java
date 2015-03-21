package ru.ifmo.ctddev.FSSARecSys.calculators.internal;

import ru.ifmo.ctddev.FSSARecSys.calculators.DataSetDistancesComputer;
import ru.ifmo.ctddev.FSSARecSys.calculators.NearestDataSetSearcher;
import ru.ifmo.ctddev.FSSARecSys.db.DataSet;
import ru.ifmo.ctddev.FSSARecSys.db.DataSetManager;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * Class for searching nearest {@link DataSet} to given one
 */
public class NearestDataSetSearcherImpl implements NearestDataSetSearcher {
    private final DataSetDistancesComputer dataSetDistancesComputer;
    private final DataSetManager dataSetManager;

    /**
     * @param dataSetManager           used for getting others {@link DataSet}
     * @param dataSetDistancesComputer will compute distances between  {@link DataSet}
     */
    public NearestDataSetSearcherImpl(DataSetManager dataSetManager,
                                      DataSetDistancesComputer dataSetDistancesComputer) {
        this.dataSetManager = dataSetManager;
        this.dataSetDistancesComputer = dataSetDistancesComputer;
    }

    /**
     * This method counting distances between other and given {@link DataSet}.
     * Then adds Pairs of distances and DataSets to PriorityQueue
     * Finally returning top <code>n</code> of Pairs
     *
     * @param n count of nearest DataSet to return
     * @return ordered Pairs of distances and {@link DataSet} truncated or padded with <code>nulls</code>
     * to obtain the specified length
     */
    @Override
    public Pair<Double, DataSet>[] search(DataSet dataSet, int n) {
        DataSet[] others = dataSetManager.getAllDataSets();
        PriorityQueue<Pair<Double, DataSet>> pq = new PriorityQueue<>(others.length,
                (o1, o2) -> Double.compare(o1.first, o2.first)
        );

        for (DataSet otherDataSet : others) {
            if (!otherDataSet.equals(dataSet)) {
                pq.add(Pair.of(dataSetDistancesComputer.calculate(dataSet, otherDataSet), otherDataSet));
            }
        }

        //noinspection unchecked
        return Arrays.copyOf((Pair<Double, DataSet>[]) pq.toArray(), n);
    }
}
