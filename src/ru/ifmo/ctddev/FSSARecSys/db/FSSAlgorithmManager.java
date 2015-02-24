package ru.ifmo.ctddev.FSSARecSys.db;

import ru.ifmo.ctddev.FSSARecSys.calculators.FSSAlgorithm;

import java.io.File;

public interface FSSAlgorithmManager {
    public String[] getAvailableFSSAlgorithms();

    public FSSAlgorithm get(String name);
    public FSSAlgorithm[] get(String[] names);

    public void register(String name, String className);
    public void register(String name, String className, String[] arguments);
    public void register(String name, String className, File jarFile);
    public void register(String name, String className, String[] arguments, File jarFile);
}
