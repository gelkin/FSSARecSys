package ru.ifmo.ctddev.FSSARecSys.calculators.mfextraction;

import ru.ifmo.ctddev.FSSARecSys.db.DataSet;


/**
 * some there here need to be created method that can return all Classes that implement ths interface(use Reflection library)
 * or it should be converted to abstract class?
 */
public interface MetaFeatureExtractor {
    public String getName();

    /**
     * This method should return the result of computing value for meta feature with name.
     * Any specific arguments?
     */
    public double extract(DataSet dataSet);
}
