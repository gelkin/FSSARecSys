package ru.ifmo.ctddev.FSSARecSys.alternative.internal.FSSClustering;

/**
 * Created by Администратор on 07.09.2015.
 */
public abstract class FSSClusteringAlgorithm {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract Double getFeatureWeight(int featureNum);
}
