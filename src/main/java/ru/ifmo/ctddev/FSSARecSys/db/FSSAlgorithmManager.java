package ru.ifmo.ctddev.FSSARecSys.db;

import ru.ifmo.ctddev.FSSARecSys.calculators.FSSAlgorithm;

import java.io.File;
import java.util.List;

public interface FSSAlgorithmManager {
    public List<String> getAvailableFSSAlgorithms();

    public FSSAlgorithm get(String name);
    public List<FSSAlgorithm> get(List<String> names);

    public boolean register(String name, String algorithmClassName, String evaluationClassName);
    public boolean register(String name, String algorithmClassName, String[] algorithmOptions, String evaluationClassName, String[] evaluationOptions);
    public boolean register(String name, String algorithmClassName, String evaluationClassName, File jarFile);
    public boolean register(String name, String algorithmClassName, String[] algorithmOptions, String evaluationClassName, String[] evaluationOptions, File jarFile);
}
