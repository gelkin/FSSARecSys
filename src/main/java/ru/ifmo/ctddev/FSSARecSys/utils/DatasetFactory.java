package ru.ifmo.ctddev.FSSARecSys.utils;

import weka.core.Instance;
import weka.core.Instances;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DatasetFactory {


    /**
     * Get deep copy of 'original' with random (percent * original.numAttributes())
     * attributes for each instance missing.
     *
     * @param original original instances set
     * @param percent percent of all attributes that will be assumed missing.
     *                'percent' value has to belong to interval (0, 1) (exclusively).
     * @return
     */
    public static Instances getInstancesWithMissingAttrs(Instances original, double percent) {
        if (percent >= 1.0 || percent <= 0.0) {
            throw new IllegalArgumentException("'percent' value has to belong to interval (0, 1) (exclusively)");
        }

        Instances resInstances = new Instances(original);

        int classIndex = resInstances.classIndex();
        List<Integer> indexesList;
        if (classIndex < 0) {
            // no class attribute
            indexesList = IntStream.range(0, resInstances.numAttributes())
                          .boxed().collect(Collectors.toList());
        } else {
            // exclude class attribute
            indexesList = IntStream.concat(IntStream.range(0, classIndex),
                                           IntStream.range(classIndex + 1, resInstances.numAttributes()))
                          .boxed().collect(Collectors.toList());
        }

        int numOfAttrsToSetMissing = (int) (indexesList.size() * percent);
        for (int i = 0; i < resInstances.numInstances(); ++i) {
            Collections.shuffle(indexesList);
            Instance inst = resInstances.instance(i);
            indexesList.subList(0, numOfAttrsToSetMissing).forEach(inst::setMissing);
        }

        return resInstances;
    }
}
