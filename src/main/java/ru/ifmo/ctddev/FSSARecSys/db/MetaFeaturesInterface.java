package ru.ifmo.ctddev.FSSARecSys.db;

public interface MetaFeaturesInterface {
    public double getMetaFeature(String name);
    public double[] getMetaFeatures(String[] names);
    public void setMetaFeature(String name, double value);
    public void setMetaFeatures(String[] names, double[] values);
}
