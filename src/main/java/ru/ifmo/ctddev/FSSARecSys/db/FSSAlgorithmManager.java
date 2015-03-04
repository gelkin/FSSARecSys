package ru.ifmo.ctddev.FSSARecSys.db;

import ru.ifmo.ctddev.FSSARecSys.calculators.FSSAlgorithm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public interface FSSAlgorithmManager {
    public List<String> getAvailableFSSAlgorithms();

    public FSSAlgorithm get(String name);

    public default List<FSSAlgorithm> get(List<String> names) {
        List<FSSAlgorithm> algorithms = new ArrayList<>();
        for (String name : names) {
            FSSAlgorithm algorithm = get(name);
            if (algorithm != null) {
                algorithms.add(algorithm);
            }
        }
        return algorithms;
    }

    public default boolean register(String name, String algorithmClassName, String evaluationClassName) {
        return register(name, algorithmClassName, null, evaluationClassName, null);
    }

    public boolean register(String name, String algorithmClassName, String[] algorithmOptions, String evaluationClassName, String[] evaluationOptions);

    public default boolean register(String name, String algorithmClassName, String evaluationClassName, File jarFile) {
        return register(name, algorithmClassName, null, evaluationClassName, null, jarFile);
    }

    public boolean register(String name, String algorithmClassName, String[] algorithmOptions, String evaluationClassName, String[] evaluationOptions, File jarFile);
}
