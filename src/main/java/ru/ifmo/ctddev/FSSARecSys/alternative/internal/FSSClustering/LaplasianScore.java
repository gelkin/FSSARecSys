package ru.ifmo.ctddev.FSSARecSys.alternative.internal.FSSClustering;

import cern.colt.function.DoubleDoubleFunction;
import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
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
public class LaplasianScore {

    /**
     * Laplasian score
     *
     * fi^ = fi_2
     * Lambda(L) = L;
     *
     * Spectral Feature Selection for
     * Supervised and Unsupervised Learning. Zheng Zhao, Huan Liu
     */

    private Instances data;

    private DistanceFunction distanceFunction;

    private int numOfInstances;

    private boolean useSparseMatrix = false;

    private DoubleMatrix1D eOnes;

    /**
     * The sigma scaling factor
     */
    protected double sigma = 1.0;

    /**
     * The distance cut factor
     */
    protected double r = -1;

    private DoubleMatrix1D d_1_2;

    public LaplasianScore(Instances instances, DistanceFunction df) {
        this.data = new Instances(instances);
        this.distanceFunction = df;
        df.setInstances(data);
        numOfInstances = data.numInstances();
        eOnes = DoubleFactory1D.dense.make(numOfInstances);
        eOnes.assign(1.0);
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

    private DoubleMatrix2D getL() {
        int n = numOfInstances;

        DoubleMatrix2D W = DoubleFactory2D.dense.make(numOfInstances, numOfInstances);
        W.assign(getW());
        final DoubleMatrix1D d = DoubleFactory1D.dense.make(n);
        final DoubleMatrix1D d_minus_1_2 = DoubleFactory1D.dense.make(n);
        d_1_2 = DoubleFactory1D.dense.make(n);
        for (int i = 0; i < n; i++) {
            double d_i = W.viewRow(i).zSum();
            d.set(i, d_i);
            d_minus_1_2.set(i, 1 / Math.sqrt(d_i));
            d_1_2.set(i, Math.sqrt(d_i));
        }

        final DoubleMatrix2D D = DoubleFactory2D.sparse.diagonal(d);
        final DoubleMatrix2D X = D.copy();
        // X = D^(-1/2) * (D - W) * D^(-1/2)
        X.assign(W, Functions.minus);
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                X.set(i, j, X.get(i, j) * d_minus_1_2.get(i)
                        * d_minus_1_2.get(j));
        return X;
    }

    private DoubleMatrix1D getMatrixDecomposition(){
        int n = numOfInstances;
        DoubleMatrix2D X = getL().copy();
        final EigenvalueDecomposition e = new EigenvalueDecomposition(X);
        final DoubleMatrix1D lambda = e.getRealEigenvalues();
        return lambda;
    }

    /*
     * feature matrix [instances][features]
     * f_i is the column of the feature matrix
     */

    private DoubleMatrix2D getFeatureMatrix(){
        DoubleMatrix2D res = DoubleFactory2D.dense.make(numOfInstances, data.numAttributes());
        for (int i = 0; i < data.numAttributes(); i++) {
            double [] f = data.attributeToDoubleArray(i);
            for (int j = 0; j < numOfInstances; j++) {
                res.set(j, i, f[j]);
            }
        }
        return res;
    }

    public Double getFeatureWeight(int featureNum) {
        DoubleMatrix2D F = DoubleFactory2D.dense.make(numOfInstances, data.numAttributes());
        F.assign(getFeatureMatrix());

        DoubleMatrix2D L = DoubleFactory2D.dense.make(numOfInstances, numOfInstances);
        L.assign(getL());
        Algebra alg = new Algebra();

        DoubleMatrix2D D12 = DoubleFactory2D.sparse.diagonal(d_1_2);

        DoubleMatrix1D D12feature = alg.mult(D12, F.viewColumn(featureNum));
        DoubleMatrix1D D12ones = alg.mult(D12, eOnes);

        //(((D^1/2) * f)^T) * L * ((D^1/2) * f)

        DoubleMatrix2D D12featureTransposed = DoubleFactory2D.dense.make(1, numOfInstances);
        for (int i = 0; i < numOfInstances; i++) {
            D12featureTransposed.set(0, i, D12feature.get(i));
        }

        Double numerator = alg.mult(alg.mult(D12featureTransposed, L).viewRow(0), D12feature);
        //(((D^1/2) * f)^T) * ((D^1/2) * f) - (((((D^1/2) * f)^T) * ((D^1/2) * e)) ^ 2) / (((D^1/2) * e)^T) * ((D^1/2) * e))
        Double denominator = alg.mult(D12feature, D12feature) -
                Math.pow(alg.mult(D12feature, D12ones), 2.0) / alg.mult(D12ones, D12ones);

        return numerator / denominator;
    }


}
