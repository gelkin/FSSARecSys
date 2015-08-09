package ru.ifmo.ctddev.FSSARecSys.alternative.internal;

import ru.ifmo.ctddev.FSSARecSys.calculators.NearestDataSetSearcher;
import ru.ifmo.ctddev.FSSARecSys.db.DataSet;
import ru.ifmo.ctddev.FSSARecSys.db.internal.Dataset;
import ru.ifmo.ctddev.FSSARecSys.db.manager.QueryManager;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * Created by Сергей on 03.08.2015.
 */
public class NearestDataset {
    private DistanceCounter distanceCounter;
    private ArrayList<Dataset> datasets;
    private QueryManager queryManager;

    public NearestDataset(DistanceCounter distanceCounter, QueryManager queryManager){
        this.distanceCounter = distanceCounter;
        this.queryManager = queryManager;
    }

    public Pair<Double, Dataset>[] search(Dataset dataset, int n) throws Exception {
        ArrayList<Dataset> others = queryManager.getAvailableDatasets();
        PriorityQueue<Pair<Double, DataSet>> pq = new PriorityQueue<>(others.size(),
                (o1, o2) -> Double.compare(o1.first, o2.first)
        );

        for (Dataset otherDataSet : others) {
            if (!otherDataSet.equals(dataset)) {
                pq.add(Pair.of(distanceCounter.countDistances(dataset, otherDataSet), otherDataSet));
            }
        }
        return Arrays.copyOf((Pair<Double, Dataset>[]) pq.toArray(), n);
    }
}
