package ru.ifmo.ctddev.FSSARecSys.db;

import ru.ifmo.ctddev.FSSARecSys.calculators.ClassifierEvaluator;

import java.io.File;
import java.util.List;

public interface ClassifierManager {
    public List<String> getAvailableClassifierNames();

    public ClassifierEvaluator get(String name);

    /**
     * @see #register(String, String, String[], java.io.File)
     */
    public boolean register(String name, String className);

    /**
     * @param name classifier's name
     * @param className the fully qualified class name of the classifier which extends <code>{@link weka.classifiers.Classifier}</code>.
     *                  <code>{@link weka.classifiers.Classifier#forName(String, String[])}</code> method is used to create classifier object.
     * @param options an array of options suitable for passing to <code>{@link weka.classifiers.Classifier#forName(String, String[])}</code>.
     *                  May be null.
     * @return <tt>true</tt> if classifier was successfully registered, <tt>false</tt> otherwise
     */
    public boolean register(String name, String className, String[] options);

    /**
     * @see #register(String, String, String[], java.io.File)
     */
    public boolean register(String name, String className, File jarFile);

    /**
     * @param name classifier's name
     * @param className the fully qualified class name of the classifier which extends <code>{@link weka.classifiers.Classifier}</code>.
     *                  <code>{@link weka.classifiers.Classifier#forName(String, String[])}</code> method is used to create classifier object.
     * @param options an array of options suitable for passing to <code>{@link weka.classifiers.Classifier#forName(String, String[])}</code>.
     *                  May be null.
     * @param jarFile file with compiled code of classifier
     * @return <tt>true</tt> if classifier was successfully registered, <tt>false</tt> otherwise
     */
    public boolean register(String name, String className, String[] options, File jarFile);
}
