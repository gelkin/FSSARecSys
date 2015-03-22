package ru.ifmo.ctddev.FSSARecSys.calculators.mfextraction;

import ru.ifmo.ctddev.FSSARecSys.db.DataSet;


public interface MetaFeatureExtractor {
    public String getName();

    /**
     * This method should return the result of computing value for meta feature with name.
     * Any specific arguments?
     */
    public double extract(DataSet dataSet);

    /**
     * Creates a new instance of a meta feature extractor given it's class name.
     *
     * @param className the fully qualified class name of the meta feature extractor
     * @return new instance of the meta feature extractor. If the extractor name is invalid return null
     */
    public static MetaFeatureExtractor forName(String className) {
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        if (!MetaFeatureExtractor.class.isAssignableFrom(clazz)) {
            return null;
        }
        Object newInstance;
        try {
            newInstance = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
        return (MetaFeatureExtractor) newInstance;
    }
}
