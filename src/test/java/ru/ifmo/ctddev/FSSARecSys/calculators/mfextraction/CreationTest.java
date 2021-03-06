package ru.ifmo.ctddev.FSSARecSys.calculators.mfextraction;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by warrior on 22.03.15.
 */
public class CreationTest {

    private static final String[] CLASS_NAMES = {
            "ru.ifmo.ctddev.FSSARecSys.calculators.mfextraction.general.NumberOfInstances",
            "ru.ifmo.ctddev.FSSARecSys.calculators.mfextraction.general.NumberOfFeatures",
            "ru.ifmo.ctddev.FSSARecSys.calculators.mfextraction.general.NumberOfClasses",
            "ru.ifmo.ctddev.FSSARecSys.calculators.mfextraction.general.DataSetDimensionality",
            "ru.ifmo.ctddev.FSSARecSys.calculators.mfextraction.statistical.MeanLinearCorrelationCoefficient",
            "ru.ifmo.ctddev.FSSARecSys.calculators.mfextraction.statistical.MeanSkewness",
            "ru.ifmo.ctddev.FSSARecSys.calculators.mfextraction.statistical.MeanKurtosis",
            "ru.ifmo.ctddev.FSSARecSys.calculators.mfextraction.informationtheoretic.NormalizedClassEntropy",
            "ru.ifmo.ctddev.FSSARecSys.calculators.mfextraction.informationtheoretic.MeanNormalizedFeatureEntropy",
            "ru.ifmo.ctddev.FSSARecSys.calculators.mfextraction.informationtheoretic.MeanMutualInformation",
            "ru.ifmo.ctddev.FSSARecSys.calculators.mfextraction.informationtheoretic.MaxMutualInformation",
            "ru.ifmo.ctddev.FSSARecSys.calculators.mfextraction.informationtheoretic.EquivalentNumberOfFeatures",
            "ru.ifmo.ctddev.FSSARecSys.calculators.mfextraction.informationtheoretic.NoiseSignalRatio",
    };

    @Test
    public void creationTest() {
        for (String className : CLASS_NAMES) {
            MetaFeatureExtractor extractor = MetaFeatureExtractor.forName(className);
            Assert.assertNotNull(extractor);
        }
    }
}
