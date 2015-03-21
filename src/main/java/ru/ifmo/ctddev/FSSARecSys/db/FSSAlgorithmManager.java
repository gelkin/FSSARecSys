package ru.ifmo.ctddev.FSSARecSys.db;

import ru.ifmo.ctddev.FSSARecSys.calculators.FSSAlgorithm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public interface FSSAlgorithmManager {
    public List<String> getAvailableFSSAlgorithms();

    public FSSAlgorithm get(String name);

    public default List<FSSAlgorithm> get(List<String> names) {
        List<FSSAlgorithm> algorithms = new ArrayList<>();
        for (String name : names) {
            FSSAlgorithm algorithm = get(name);
            if (algorithm != null) {
                algorithms.add(algorithm);
            }
        }
        return algorithms;
    }

    /**
     * @see #register(String, String, String[], String, String[])
     */
    public default boolean register(String name, String searchClassName, String evaluationClassName) {
        return register(name, searchClassName, null, evaluationClassName, null);
    }

    /**
     * @param name algorithm name
     * @param searchClassName the fully qualified class name of the search which extends <code>{@link weka.attributeSelection.ASSearch}</code>.
     *                        <code>{@link weka.attributeSelection.ASSearch#forName(String, String[])}</code> method is used to create search object.
     *
     * @param searchOptions an array of options suitable for passing to <code>{@link weka.attributeSelection.ASSearch#forName(String, String[])}</code>.
     *                      May be null.
     * @param evaluationClassName the fully qualified class name of the search which extends <code>{@link weka.attributeSelection.ASSearch}</code>.
     *                            <code>{@link weka.attributeSelection.ASEvaluation#forName(String, String[])} method is used to create evaluation object</code>
     * @param evaluationOptions an array of options suitable for passing to <code>{@link weka.attributeSelection.ASEvaluation#forName(String, String[])}</code>.
     *                          May be null.
     * @return <tt>true</tt> if algorithm was successfully registered, <tt>false</tt> otherwise
     */
    public boolean register(String name, String searchClassName, String[] searchOptions, String evaluationClassName, String[] evaluationOptions);

    /**
     * @see #register(String, String, String[], String, String[], java.io.File)
     */
    public default boolean register(String name, String searchClassName, String evaluationClassName, File jarFile) {
        return register(name, searchClassName, null, evaluationClassName, null, jarFile);
    }

    /**
     * @param name algorithm name
     * @param searchClassName the fully qualified class name of the search which extends <code>{@link weka.attributeSelection.ASSearch}</code>.
     *                        <code>{@link weka.attributeSelection.ASSearch#forName(String, String[])}</code> method is used to create search object.
     *
     * @param searchOptions an array of options suitable for passing to <code>{@link weka.attributeSelection.ASSearch#forName(String, String[])}</code>.
     *                      May be null.
     * @param evaluationClassName the fully qualified class name of the search which extends <code>{@link weka.attributeSelection.ASSearch}</code>.
     *                            <code>{@link weka.attributeSelection.ASEvaluation#forName(String, String[])} method is used to create evaluation object</code>
     * @param evaluationOptions an array of options suitable for passing to <code>{@link weka.attributeSelection.ASEvaluation#forName(String, String[])}</code>.
     *                          May be null.
     * @param jarFile file with compiled code of search and evaluation
     * @return <tt>true</tt> if algorithm was successfully registered, <tt>false</tt> otherwise
     */
    public boolean register(String name, String searchClassName, String[] searchOptions, String evaluationClassName, String[] evaluationOptions, File jarFile);
}
