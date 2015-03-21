package ru.ifmo.ctddev.FSSARecSys.calculators.internal;

import ru.ifmo.ctddev.FSSARecSys.calculators.DataSetDistancesComputer;
import ru.ifmo.ctddev.FSSARecSys.db.DataSet;
import ru.ifmo.ctddev.FSSARecSys.db.Statistic;
import ru.ifmo.ctddev.FSSARecSys.utils.MetaFeaturesVector;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.Objects;


/**
 * This class implements original {@link DataSet} distance computer.
 */
public class DataSetDistancesComputerImpl implements DataSetDistancesComputer {
    private final MetaFeaturesVector metaFeatures;
    private final Pair<Double, Double>[] minMaxValues;
    private final int numberOfMetaFeatures;

    /**
     * @param metaFeatures The set of meta-features that will be used for calculating distance
     *                     This class need {@link Statistic} for getting min and max values of meta-features
     */
    public DataSetDistancesComputerImpl(Statistic statistic, MetaFeaturesVector metaFeatures) {
        this.numberOfMetaFeatures = metaFeatures.getNumberOfMetaFeatures();
        this.metaFeatures = metaFeatures;
        this.minMaxValues = statistic.getMinMaxMetaFeaturesValues(metaFeatures);
    }

    /**
     * @return Distance between given DataSets
     */
    @Override
    public double calculate(DataSet dataSetFirst, DataSet dataSetSecond) {
        double[] valuesFirst = metaFeatures.getValues(dataSetFirst);
        double[] valuesSecond = metaFeatures.getValues(dataSetSecond);

        double distance = 0;

        for (int i = 0; i < numberOfMetaFeatures; i++) {
            if (!Objects.equals(minMaxValues[i].first, minMaxValues[i].second)) {
                distance += Math.abs(valuesFirst[i] - valuesSecond[i]) / (minMaxValues[i].second - minMaxValues[i].first);
            }
        }

        return distance;  // #todo: should we normalize this value by division by numberOfMetaFeatures?
    }
}
