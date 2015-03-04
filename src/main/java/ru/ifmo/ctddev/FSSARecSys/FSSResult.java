package ru.ifmo.ctddev.FSSARecSys;

import java.util.Objects;

public class FSSResult {

    public String dataSetName;
    public String algorithmName;

    public int numberOfSelectedFeatures;
    public int[] selectedFeatures;
    public long selectionTime;

    public FSSResult(String dataSetName, String algorithmName, int[] selectedFeatures, long selectionTime) {
        this.dataSetName = Objects.requireNonNull(dataSetName);
        this.algorithmName = Objects.requireNonNull(algorithmName);
        this.selectedFeatures = Objects.requireNonNull(selectedFeatures);
        this.numberOfSelectedFeatures = selectedFeatures.length;
        this.selectionTime = selectionTime;
    }
}
