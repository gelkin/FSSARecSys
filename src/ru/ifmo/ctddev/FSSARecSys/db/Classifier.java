package ru.ifmo.ctddev.FSSARecSys.db;

import weka.attributeSelection.CfsSubsetEval;

import java.io.File;


public class Classifier extends ORM {
    public Classifier(String name, String absoluteClassName) {
        this(name, absoluteClassName, null);
    }

    public Classifier(String name, String absoluteClassName, File jarFile) {
        // fill the fields
        // Note check that is this: java.util.jar.JarFile
    }

    public String getName() {return null;}

    /**
     * load jar if it needed
     * create instance by class name
     * and return result
     *
     * This should be implemented lazy way with caching!
     */
    public CfsSubsetEval getClassifier() {
        return null;
    }
}
