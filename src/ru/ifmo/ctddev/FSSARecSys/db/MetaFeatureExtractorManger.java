package ru.ifmo.ctddev.FSSARecSys.db;

import ru.ifmo.ctddev.FSSARecSys.calculators.mfextraction.MetaFeatureExtractor;

public interface MetaFeatureExtractorManger {
    public String[] getAvailableExtractorNames();

    public MetaFeatureExtractor get(String name);

    public void register(String name, String className);
}
