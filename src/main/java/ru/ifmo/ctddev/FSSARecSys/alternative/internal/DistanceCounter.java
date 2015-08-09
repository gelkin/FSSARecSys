package ru.ifmo.ctddev.FSSARecSys.alternative.internal;

import ru.ifmo.ctddev.FSSARecSys.db.internal.Dataset;
import ru.ifmo.ctddev.FSSARecSys.db.manager.QueryManager;
import ru.ifmo.ctddev.FSSARecSys.utils.MFVector;
import ru.ifmo.ctddev.FSSARecSys.utils.MetaFeaturesVector;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Сергей on 03.08.2015.
 */
public class DistanceCounter {
    private List<String> mfNames;
    private QueryManager queryManager;
    private Map<String, Pair<Double, Double>> minMaxValues;

    public DistanceCounter(List<String> mfNames, QueryManager queryManager) {
        this.mfNames = mfNames;
        this.queryManager = queryManager;
    }

    public Double countDistances(Dataset first, Dataset second) throws Exception {
        MFVector mfFirst = new MFVector(mfNames, queryManager, first);
        MFVector mfSecond = new MFVector(mfNames, queryManager, first);

        Map<String, Double> valuesFirst = mfFirst.getValues();
        Map<String, Double> valuesSecond = mfSecond.getValues();

        //getMinMaxValues
        for (String mfName: mfNames){
            minMaxValues.put(mfName, new Pair<Double, Double>(queryManager.getMaxMFValue(mfName),
                    queryManager.getMinMFValue(mfName)));
        }

        Double distance = 0.0;

        for (String mfName: mfNames){
            if (!Objects.equals(minMaxValues.get(mfName).first, minMaxValues.get(mfName).second)) {
                distance += Math.abs(valuesFirst.get(mfName) - valuesSecond.get(mfName)) /
                        (minMaxValues.get(mfName).second - minMaxValues.get(mfName).first);
            }
        }
        return distance;
    }
}
