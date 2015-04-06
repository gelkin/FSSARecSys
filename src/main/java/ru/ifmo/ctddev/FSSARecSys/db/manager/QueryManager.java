package ru.ifmo.ctddev.FSSARecSys.db.manager;

//import ru.ifmo.ctddev.FSSARecSys.calculators.*;
import ru.ifmo.ctddev.FSSARecSys.db.internal.*;
import ru.ifmo.ctddev.FSSARecSys.db.dbWraper.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.applet.Applet;
import java.awt.*;
import java.sql.*;
import java.util.Map;

public class QueryManager {
    //DBWrapper dbwarpper;

    private static DBWrapper dbHandler;

    public static final String DB_URL = "jdbc:mysql://localhost/";

    //  Database credentials
    public static final String USER = "root";
    public static final String PASS = "let'srock";

    public QueryManager(String DB_URL, String USER, String PASS) {
        this.dbHandler = new DBWrapper(DB_URL, USER, PASS);
    }


    //Dataset
    public void addDataset(Dataset dataset) throws Exception {
        DBWrapper.addDataset(dataset);

//        Thread tread = new Thread(new Runnable() {
//            public void run () {
//                try {
//                    DBWrapper.addDataset(dataset);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        tread.start();
    }

    public Dataset getDataset(String name) throws Exception {
        return dbHandler.getDataset(name);
    }

    //Meta Features
    public void addMetaFeature(String name, String classPath) throws Exception {
        dbHandler.addMetafeature(name, classPath);
    }

    //Dataset Features
    public void addMFforDataset(String datasetName, String metaFeatureName, double value) throws Exception {
        dbHandler.addMFforDataset(datasetName, metaFeatureName, value);
    }

    public Double getMFforDataset(String datasetName, String metaFeature) throws Exception {
        return DBWrapper.getMFforDataset(datasetName, metaFeature);
    }

    //MLAlgorithm
    public void addMLAlgorithm(MLAlgorithm algo) throws Exception {
        dbHandler.addMLAlgorithm(algo);
    }

    public MLAlgorithm getMLAlgorithm(MLAlgorithm algo) throws Exception {
        return dbHandler.getMLAlgorithm(algo);
    }

    // Params
    public void addParam(String paramName) throws Exception {
        dbHandler.addParam(paramName);
    }

    //FSS Algorithm
    public void addFSSAlgorithm(FSSAlgorithm fssAlgorithm) throws Exception {
        dbHandler.addFSAlgorithm(fssAlgorithm);
    }


    //Metric Params
    public void addMetricParam(Metrics metrics, String paramName, MLAlgorithm algo, Double value) throws Exception {
        dbHandler.addMetricsParams(metrics, paramName, algo, value);
    }

    public Map<String, Double> getMetricParams(Metrics metrics, MLAlgorithm algo) throws Exception {
       return dbHandler.getMetricParams(metrics, algo);
    }

    // Metrics
    public void addMetrics(Metrics metrics) throws Exception {
        dbHandler.addMetrics(metrics);
    }

    public Metrics getMetrics(Metrics metrics) throws Exception {
        return DBWrapper.getMetrics(metrics);
    }

}