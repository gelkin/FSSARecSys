package ru.ifmo.ctddev.FSSARecSys.db.dbWraper;

import javax.sql.rowset.serial.SerialBlob;
import java.io.*;
import java.sql.*;
import java.lang.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ifmo.ctddev.FSSARecSys.db.manager.*;
import ru.ifmo.ctddev.FSSARecSys.db.internal.*;

/**
 * Created by sergey on 26.03.15.
 */

public class DBWrapper {

    private static Connection connect = null;

    private static String adress;
    private static String user;
    private static String pass;

    private static Thread evaluationManager;

    public DBWrapper(String adress, String user, String pass) {
        this.adress = adress;
        this.user = user;
        this.pass = pass;
    }

    public static void readDataBase() throws Exception {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connect = DriverManager
                    .getConnection(adress, user, pass);

            Statement statement = connect.createStatement();
            ResultSet resultSet = statement.executeQuery("use FSS");
        } catch (Exception e) {
            throw e;
        } finally {
        }

    }

    //Dataset

    public static boolean addDataset(Dataset dataset) throws Exception {
        readDataBase();

        if (!getDatasetRecord(dataset.getName()).next()){
            InputStream inputStream = new FileInputStream(dataset.getFile());
            PreparedStatement preparedStatement = connect
                    .prepareStatement("insert into  FSS.Dataset values (default, ?, ?, ?)");

            preparedStatement.setString(1, dataset.getName());
            preparedStatement.setBlob(2, inputStream);
            preparedStatement.setString(3, dataset.getTaskType());
            preparedStatement.executeUpdate();
            return true;
        }
        return false;
    }

    private static ResultSet getDatasetRecord(String name) throws Exception {
        readDataBase();

        PreparedStatement preparedStatement = connect
                .prepareStatement("select * from FSS.Dataset where name = ?");
        preparedStatement.setString(1, name);
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet;
    }

    public static Dataset getDataset(String name) throws Exception {
        ResultSet resultSet = getDatasetRecord(name);
        Dataset result = null;
        if (resultSet.next()) {
            Blob blob = resultSet.getBlob(3);
            InputStream in = blob.getBinaryStream();
            File file = new File(name + ".arff");
            OutputStream out = new FileOutputStream(file);
            byte[] buff = blob.getBytes(1, (int) blob.length());  // how much of the blob to read/write at a time
            int len = 0;
            while ((len = in.read(buff)) != -1) {
                out.write(buff, 0, len);
            }
            String taskType = resultSet.getString(4);
            result = new Dataset(name, file, taskType);
        }

        return result;
    }

    public static ArrayList<Dataset> getAllDatasets() throws Exception {
        readDataBase();

        PreparedStatement preparedStatement = connect
                .prepareStatement("select * from FSS.Dataset");
        ResultSet resultSet = preparedStatement.executeQuery();
        //resultSet.get
        return null;
    }

    private static int getDatasetIdByName(String name) throws Exception{
        ResultSet resultSet = getDatasetRecord(name);
        if (resultSet.next())
            return resultSet.getInt(1);
        else
            return -1;
    }

    private static String getDatasetNameById(int id) throws Exception {
        readDataBase();

        PreparedStatement preparedStatement = connect
                .prepareStatement("select * from FSS.Dataset where id = ?");
        preparedStatement.setInt(1, id);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next())
            return resultSet.getString(2);
        else
            return null;
    }

    //MetaFeature

    public static boolean addMetafeature(String name, String classPath) throws Exception{
        readDataBase();

        if (!getMetafeatureRecord(name, classPath).next()) {
            PreparedStatement preparedStatement = connect
                    .prepareStatement("insert into FSS.MetaFeatures values (default, ?, ?)");

            preparedStatement.setString(1, name);
            preparedStatement.setString(2, classPath);
            preparedStatement.executeUpdate();
            return true;
        }
        return false;
    }

    private static ResultSet getMetafeatureRecord(String name, String classPath) throws Exception{
        readDataBase();

        PreparedStatement preparedStatement = connect
                .prepareStatement("select MetaFeatures.name from FSS.MetaFeatures where name = ? " +
                        "and classPath = ?");

        preparedStatement.setString(1, name);
        preparedStatement.setString(2, classPath);
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet;
    }

    //DatasetFeature

    /*this function looks for the record of dataset's metafeature value*/
    private static ResultSet getMFRecord(String datasetName, String mfName) throws SQLException {
        PreparedStatement preparedStatement = connect
                .prepareStatement("select DatasetFeatures.value from DatasetFeatures where idDataset = " +
                        "(select Dataset.id from Dataset where Dataset.name = ?) and idMf = " +
                        "(select MetaFeatures.id from MetaFeatures where MetaFeatures.name = ?)");
        preparedStatement.setString(1, datasetName);
        preparedStatement.setString(2, mfName);
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet;
    }

    public static boolean addMFforDataset(String datasetName, String mfName, Double value) throws Exception {
        readDataBase();

        if (!getMFRecord(datasetName, mfName).next()) {
            PreparedStatement preparedStatement = connect
                    .prepareStatement("select Dataset.id from Dataset where name = ?");
            preparedStatement.setString(1, datasetName);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int idDataset = resultSet.getInt(1);

            if (idDataset == -1)
                throw new Exception("No dataset found!");

            preparedStatement = connect
                    .prepareStatement("select MetaFeatures.id from MetaFeatures where name = ?");
            preparedStatement.setString(1, mfName);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int idMf = resultSet.getInt(1);

            preparedStatement = connect
                    .prepareStatement("insert into DatasetFeatures values (?, ?, ?)");
            preparedStatement.setInt(1, idDataset);
            preparedStatement.setInt(2, idMf);
            preparedStatement.setDouble(3, value);
            preparedStatement.executeUpdate();
            return true;
        }
        return false;
    }

    private static Thread getThreadByName(String threadName) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals(threadName)) return t;
        }
        return null;
    }

    public static Double getMFforDataset(final String datasetName, final String mfName) throws Exception {
        readDataBase();
        ResultSet resultSet = getMFRecord(datasetName, mfName);
        if (resultSet.next())
            return resultSet.getDouble(1);
        else{
            PreparedStatement preparedStatement = connect
                    .prepareStatement("select MetaFeatures.classPath from MetaFeatures " +
                            "where name = ?");
            preparedStatement.setString(1, mfName);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            Double result = 0.0;
            final String classPath = resultSet.getString(1);
            Thread t = getThreadByName(datasetName + mfName);
            if (t == null) {
                evaluationManager = new Thread(new Runnable() {
                    public void run() {
                        //херовое место c вычислительным менеджером
                        Double value = null;
                        try {
                            value = EvaluationManager.evaluate(getDataset(datasetName), classPath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            addMFforDataset(datasetName, mfName, value);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                evaluationManager.setName(datasetName + mfName);
                evaluationManager.start();
                resultSet = getMFRecord(datasetName, mfName);
                resultSet.next();
                result = resultSet.getDouble(1);
            }
            else {
                try{
                    while (t.isAlive()){
                        Thread.sleep(1000);
                    }
                } catch(Exception e){}
                resultSet = getMFRecord(datasetName, mfName);
                resultSet.next();
                result = resultSet.getDouble(1);
            }
            return result;
        }
    }

    //MinMaxMF

    public Double getMaxMFValue(String mfName) throws Exception {
        readDataBase();
        PreparedStatement preparedStatement = connect
                .prepareStatement("select max (DatasetFeatures.value) as MaxMf from FSS.DatasetFeatures where " +
                        "idMf = (select MetaFeatures.id from MetaFeatures where MetaFeatures.name = ?)");
        preparedStatement.setString(1, mfName);
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet.getDouble(1);
    }

    public Double getMinMFValue(String mfName) throws Exception {
        readDataBase();
        PreparedStatement preparedStatement = connect
                .prepareStatement("select min (DatasetFeatures.value) as MinMf from FSS.DatasetFeatures where " +
                        "idMf = (select MetaFeatures.id from MetaFeatures where MetaFeatures.name = ?)");
        preparedStatement.setString(1, mfName);
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet.getDouble(1);
    }

    //MLAlgorithm

    public static boolean addMLAlgorithm(MLAlgorithm algo) throws Exception{
        readDataBase();
        if (!getMLAlgorithmRecord(algo).next()) {
            PreparedStatement preparedStatement = connect
                    .prepareStatement("insert into FSS.MLAlgorithm values (default, ?, ?, ?, ?)");

            preparedStatement.setString(1, algo.getName());
            preparedStatement.setString(2, algo.getClassPath());
            preparedStatement.setString(3, algo.getOptions());
            preparedStatement.setString(4, algo.getTaskType());
            preparedStatement.executeUpdate();
            return true;
        }
        return false;
    }

    private static ResultSet getMLAlgorithmRecord(MLAlgorithm algo) throws Exception {
        readDataBase();

        PreparedStatement preparedStatement = connect
                .prepareStatement("select * from FSS.MLAlgorithm where name = ? and " +
                        "options = ? and taskType = ?");
        preparedStatement.setString(1, algo.getName());
        preparedStatement.setString(2, algo.getOptions());
        preparedStatement.setString(3, algo.getTaskType());

        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet;
    }

    private static int getMLAlgorithmId(MLAlgorithm algo) throws Exception{
        ResultSet resultSet = getMLAlgorithmRecord(algo);
        if (resultSet.next())
            return resultSet.getInt(1);
        else
            return -1;
    }

    public static MLAlgorithm getMLAlgorithm(MLAlgorithm algo) throws Exception{
        ResultSet resultSet = getMLAlgorithmRecord(algo);
        MLAlgorithm result = null;
        if (resultSet.next())
            result = new MLAlgorithm(resultSet.getString(2), resultSet.getString(3),
                resultSet.getString(4), resultSet.getString(5));
        return result;
    }

    //Params

    public static boolean addParam(String name) throws Exception{
        readDataBase();

        if(!getParamRecord(name).next()) {
            PreparedStatement preparedStatement = connect
                    .prepareStatement("insert into FSS.Params values (default, ?)");

            preparedStatement.setString(1, name);
            preparedStatement.executeUpdate();
            return true;
        }
        return false;
    }

    private static ResultSet getParamRecord(String name) throws Exception{
        readDataBase();
        PreparedStatement preparedStatement = connect
                .prepareStatement("select * from FSS.Params where name = ?");

        preparedStatement.setString(1, name);
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet;
    }

    private static int getParamIdByName(String name) throws Exception{
        ResultSet resultSet = getParamRecord(name);
        if (resultSet.next())
            return resultSet.getInt(1);
        else
            return -1;
    }

    private static String getParamNameById(int id) throws Exception{
        readDataBase();
        PreparedStatement preparedStatement = connect
                .prepareStatement("select Params.name from FSS.Params where id = ?");

        preparedStatement.setInt(1, id);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next())
            return resultSet.getString(1);
        else
            return null;
    }

    //FSSAlgorithm
    public static boolean addFSAlgorithm(FSSAlgorithm fssAlgorithm) throws Exception{
        readDataBase();

        if(!getFSSAlgorithmRecord(fssAlgorithm).next()) {
            PreparedStatement preparedStatement = connect
                    .prepareStatement("insert into FSS.FSAlgorithm values (default, ?, ?, ?, ?, ?)");

            preparedStatement.setString(1, fssAlgorithm.getFssAlgoName());
            preparedStatement.setString(2, fssAlgorithm.getSearchClass());
            preparedStatement.setString(3, fssAlgorithm.getEvalClass());
            preparedStatement.setString(4, fssAlgorithm.getSearchOptions());
            preparedStatement.setString(5, fssAlgorithm.getEvalOptions());
            preparedStatement.executeUpdate();
            return true;
        }
        return false;
    }

    private static ResultSet getFSSAlgorithmRecord(FSSAlgorithm fssAlgorithm) throws Exception {
        readDataBase();
        PreparedStatement preparedStatement = connect
                .prepareStatement("select * from FSS.FSAlgorithm where name = ? " +
                        "and searchClass = ? and evalClass =  ? " +
                        "and searchOptions =  ? and evalOptions = ?");

        preparedStatement.setString(1, fssAlgorithm.getFssAlgoName());
        preparedStatement.setString(2, fssAlgorithm.getSearchClass());
        preparedStatement.setString(3, fssAlgorithm.getEvalClass());
        preparedStatement.setString(4, fssAlgorithm.getSearchOptions());
        preparedStatement.setString(5, fssAlgorithm.getEvalOptions());
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet;
    }

    private static int getFSSAlgorithmId(FSSAlgorithm fssAlgorithm) throws Exception {
        ResultSet resultSet = getFSSAlgorithmRecord(fssAlgorithm);
        if (resultSet.next())
            return resultSet.getInt(1);
        else
            return -1;
    }

    private static FSSAlgorithm getFSSAlgorithmById(int id) throws Exception {
        readDataBase();
        PreparedStatement preparedStatement = connect
                .prepareStatement("select * from FSS.FSAlgorithm where id = ?");
        preparedStatement.setInt(1, id);
        ResultSet resultSet = preparedStatement.executeQuery();
        FSSAlgorithm result = null;

        if (resultSet.next())
            result = new FSSAlgorithm(resultSet.getString(2), resultSet.getString(3),
                resultSet.getString(4), resultSet.getString(5), resultSet.getString(6));
        return result;
    }

    //Metrics

    public static boolean addMetrics(Metrics metrics) throws Exception {
        readDataBase();
        if (getMetrics(metrics) == null){
            int fssId = getFSSAlgorithmId(metrics.getFssAlgorithm());
            int datasetId = getDatasetIdByName(metrics.getDatasetName());

            if (fssId == -1)
                throw new Exception("No FSAlgorithm found!");
            if (datasetId == -1)
                throw new Exception("No dataset found!");


            PreparedStatement preparedStatement = connect
                    .prepareStatement("insert into FSS.Metrics values (default, ?, ?, ?)");

            preparedStatement.setInt(1, fssId);
            preparedStatement.setInt(2, datasetId);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = null;
            oos = new ObjectOutputStream(bos);
            oos.writeObject(metrics.getListOfFeatures());
            byte[] buff = bos.toByteArray();
            Blob blob = new SerialBlob(buff);

            preparedStatement.setBlob(3, blob);
            preparedStatement.executeUpdate();
            return true;
        }
        return false;
    }

    private static ResultSet getMetricsRecord(Metrics metrics) throws Exception{
        readDataBase();
        int fssId = getFSSAlgorithmId(metrics.getFssAlgorithm());
        int datasetId = getDatasetIdByName(metrics.getDatasetName());

        PreparedStatement preparedStatement = connect
                .prepareStatement("select * from FSS.Metrics where idFSAlgo = ? " +
                        "and idDataset = ?");
        preparedStatement.setInt(1, fssId);
        preparedStatement.setInt(2, datasetId);
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet;
    }

    private static int getMetricsId(Metrics metrics) throws Exception {
        ResultSet resultSet = getMetricsRecord(metrics);
        if (resultSet.next())
            return resultSet.getInt(1);
        else
            return -1;
    }

    private static boolean compareLists(List<?> l1, List<?> l2){
        ArrayList<?> cp = new ArrayList<>(l1);
        for (Object o : l2) {
            if (!cp.remove(o)) {
                return false;
            }
        }
        return cp.isEmpty();
    }

    public static Metrics getMetrics(Metrics metrics) throws Exception {
        ResultSet resultSet = getMetricsRecord(metrics);

        Metrics result = null;
        while (resultSet.next()){
            Blob blob = resultSet.getBlob(4);
            byte[] buff = blob.getBytes(1, (int) blob.length());
            ByteArrayInputStream b = new ByteArrayInputStream(buff);
            ObjectInputStream o = new ObjectInputStream(b);
            List<String> mfList = (List<String>)o.readObject();
            if (compareLists(mfList, metrics.getListOfFeatures())){
                FSSAlgorithm algo = getFSSAlgorithmById(resultSet.getInt(2));
                String datasetName = getDatasetNameById(resultSet.getInt(3));

                if (datasetName == null)
                    throw new Exception("No dataset found!");
                if (algo == null)
                    throw new Exception("No FS algorithm found!");

                result = new Metrics(algo, datasetName, mfList);
                break;
            }
        }
        return result;
    }

    //MetricParams

    public static boolean addMetricsParams(Metrics metrics, String paramName, MLAlgorithm algo, Double value) throws Exception {
        readDataBase();

        int metricsId = getMetricsId(metrics);
        int mlAlgoId = getMLAlgorithmId(algo);
        int paramId = getParamIdByName(paramName);

        if (!getMetricParamRecord(metricsId, mlAlgoId, paramId).next()){
            PreparedStatement preparedStatement = connect
                    .prepareStatement("insert into FSS.MetricParams values (?, ?, ?, ?)");

            preparedStatement.setInt(1, metricsId);
            preparedStatement.setInt(2, mlAlgoId);
            preparedStatement.setInt(3, paramId);
            preparedStatement.setDouble(4, value);
            preparedStatement.executeUpdate();
            return true;
        }
        return false;
    }

    private static ResultSet getMetricParamRecord(int metricsId, int mlAlgoId, int paramId) throws Exception{
        PreparedStatement preparedStatement = connect
                .prepareStatement("select * from FSS.MetricParams where idMetric = ? " +
                        "and idMLAlgo = ? and idParams = ?");
        preparedStatement.setInt(1, metricsId);
        preparedStatement.setInt(2, mlAlgoId);
        preparedStatement.setInt(3, paramId);
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet;
    }

    public static Map<String, Double> getMetricParams(Metrics metrics, MLAlgorithm algo) throws Exception{
        readDataBase();

        int metricsId = getMetricsId(metrics);
        int mlAlgoId = getMLAlgorithmId(algo);

        PreparedStatement preparedStatement = connect
                .prepareStatement("select * from FSS.MetricParams " +
                        "where idMetric = ? and idMLAlgo = ?");
        preparedStatement.setInt(1, metricsId);
        preparedStatement.setInt(2, mlAlgoId);
        ResultSet resultSet = preparedStatement.executeQuery();

        Map<String, Double> result = new HashMap<String, Double>();
        while (resultSet.next()){
            result.put(getParamNameById(resultSet.getInt(3)), resultSet.getDouble(4));
        }
        return result;
    }

}
