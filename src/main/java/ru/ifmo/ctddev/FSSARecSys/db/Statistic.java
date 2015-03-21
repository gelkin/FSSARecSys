package ru.ifmo.ctddev.FSSARecSys.db;

import ru.ifmo.ctddev.FSSARecSys.utils.MetaFeaturesVector;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

/**
 * returns statistic data from DataBase
 */
public interface Statistic {
    /**
     * @param metaFeatures get values only for this meta-features
     * @return pairs of minimum and maximum values for given meta-features across all stored DataSets
     */
    Pair<Double, Double>[] getMinMaxMetaFeaturesValues(MetaFeaturesVector metaFeatures);
}
