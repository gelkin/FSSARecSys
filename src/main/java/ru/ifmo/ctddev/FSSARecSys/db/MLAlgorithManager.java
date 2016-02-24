package ru.ifmo.ctddev.FSSARecSys.db;

import ru.ifmo.ctddev.FSSARecSys.calculators.FSSAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.db.internal.MLAlgorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Server on 03.02.2016.
 */
public interface MLAlgorithManager {
    public List<String> getAvailableMLAlgorithms();

    public MLAlgorithm get(String name);

    public default List<MLAlgorithm> get(List<String> names) {
        List<MLAlgorithm> algorithms = new ArrayList<>();
        for (String name : names) {
            MLAlgorithm algorithm = get(name);
            if (algorithm != null) {
                algorithms.add(algorithm);
            }
        }
        return algorithms;
    }

}
