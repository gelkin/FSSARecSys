package ru.ifmo.ctddev.FSSARecSys.db;

import weka.attributeSelection.ASSearch;

import java.io.File;


public class FSSAlgorithm extends ORM {
    public FSSAlgorithm(String name, String absoluteClassName) {
        this(name, absoluteClassName, (String[])null);
    }

    public FSSAlgorithm(String name, String absoluteClassName, String[] arguments) {
        this(name, absoluteClassName, arguments, null);
        // fill the fields
        // Note check that is this: java.util.jar.JarFile
    }

    public FSSAlgorithm(String name, String absoluteClassName, File jarFile) {
        this(name, absoluteClassName, null, jarFile);
        // fill the fields
        // Note check that is this: java.util.jar.JarFile
    }

    public FSSAlgorithm(String name, String absoluteClassName, String[] arguments, File jarFile) {
        // fill the fields
        // Note check that is this: java.util.jar.JarFile
    }

    public String getName() {return null;}

    /**
     * load jar if it needed
     * create instance by class name
     * if it has arguments and implements weka.core.OptionHandler
     * then apply argument
     * and return result
     *
     * This should be implemented lazy way with caching!
     */
    public ASSearch getSearcher() {
        return null;
    }
}
