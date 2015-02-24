package ru.ifmo.ctddev.FSSARecSys.db;

import java.io.File;

public interface DataSetManager {
    public DataSet[] getAllDataSets();
    public DataSet get(String name);

    public void registerARFF(String name, File pathToARRFFile);
    public void registerARFF(String name, File pathToARRFFile, int classIndex);
}
