package ru.ifmo.ctddev.FSSARecSys.db;

import ru.ifmo.ctddev.FSSARecSys.calculators.Evaluator;

import java.io.File;

public interface EvaluatorManager {
    public String[] getAvailableClassifierNames();

    public Evaluator get(String name);

    public void register(String name, String className);
    public void register(String name, String className, String[] arguments);
    public void register(String name, String className, File jarFile);
    public void register(String name, String className, String[] arguments, File jarFile);
}
