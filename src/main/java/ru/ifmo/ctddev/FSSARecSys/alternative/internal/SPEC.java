package ru.ifmo.ctddev.FSSARecSys.alternative.internal;

import cern.colt.function.DoubleDoubleFunction;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import cern.colt.matrix.linalg.SingularValueDecomposition;
import cern.jet.math.Functions;
import weka.core.DistanceFunction;
import weka.core.Instances;

import java.util.function.Function;

/**
 * Created by Администратор on 31.08.2015.
 */
public abstract class SPEC {
    private Instances data;

    private DistanceFunction distanceFunction;

    private int numOfInstances;

    private boolean useSparseMatrix = false;

    /**
     * The sigma scaling factor
     */
    protected double sigma = 1.0;

    /**
     * The distance cut factor
     */
    protected double r = -1;

    public SPEC(Instances instances, DistanceFunction df) {
        this.data = new Instances(instances);
        this.distanceFunction = df;
        df.setInstances(data);
        numOfInstances = data.numInstances();
    }

    public void setSparseMatrix(){
        useSparseMatrix = true;
    }

    public DistanceFunction getDistanceFunction(){
        return distanceFunction;
    }

    public Instances getData() {
        return data;
    }

    private DoubleMatrix2D getW() {
        int n = numOfInstances;
        final DoubleMatrix2D w = useSparseMatrix ? DoubleFactory2D.sparse.make(
                n, n) : DoubleFactory2D.dense.make(n, n);
    /*
     * final double[][] v1 = new double[n][]; for (int i = 0; i < n; i++)
     * v1[i] = data.instance(i).toDoubleArray(); final DoubleMatrix2D v =
     * DoubleFactory2D.dense.make(v1);
     */
        final double sigma_sq = sigma * sigma;
        // Sets up similarity matrix
        for (int i = 0;  i < n; i++)
            for (int j = i; j < n; j++)            {
                final double dist = getDistanceFunction().distance(
                        getData().instance(i), getData().instance(j));
                if ((r <= 0) || (dist < r)) {
                    final double sim = Math
                            .exp(-(dist * dist) / (2 * sigma_sq));
                    w.set(i, j, sim);
                    w.set(j, i, sim);
                }
            }
        return w;
    }

    private DoubleMatrix2D getD() {
        int n = numOfInstances;
        DoubleMatrix2D W = getW();
        DoubleMatrix2D D = DoubleFactory2D.dense.make(n, n);

        for (int i = 0; i < n; i++) {
            double sum = W.viewRow(i).zSum();
            for (int j = 0; j < n; j++) {
                if (i == j)
                    D.set(i, j, sum);
                else
                    D.set(i, j, 0.0);
            }
        }

        //EigenvalueDecomposition e = new EigenvalueDecomposition(D);
        //cern.colt.matrix.linalg.SingularValueDecomposition valueDecomposition = new SingularValueDecomposition(D);

        //Algebra a =new Algebra();
        //a.

        return D;
    }

    private DoubleMatrix2D getL(){

    }

    public abstract DoubleMatrix2D getLambda();

    public abstract DoubleMatrix1D evaluationFunction();

}
