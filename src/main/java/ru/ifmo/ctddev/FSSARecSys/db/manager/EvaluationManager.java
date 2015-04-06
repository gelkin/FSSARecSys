package ru.ifmo.ctddev.FSSARecSys.db.manager;

import ru.ifmo.ctddev.FSSARecSys.calculators.mfextraction.*;
import ru.ifmo.ctddev.FSSARecSys.db.internal.*;

import java.io.File;
import java.util.List;
import java.applet.Applet;
import java.awt.*;
import java.sql.*;

public class EvaluationManager {
    private Double result;
    private void writeValue(Double val) {
        result = val;
    }

    public static Double evaluate(Dataset dataset, String className){
        MetaFeatureExtractor evaluator = MetaFeatureExtractor.forName(className);
        return evaluator.extract(dataset);
    }
}