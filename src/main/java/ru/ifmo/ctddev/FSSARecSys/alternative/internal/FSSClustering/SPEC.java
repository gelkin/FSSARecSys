package ru.ifmo.ctddev.FSSARecSys.alternative.internal.FSSClustering;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.Functions;
import weka.core.DistanceFunction;
import weka.core.Instances;

/**
 * Created by Администратор on 04.09.2015.
 */
public class SPEC extends FSSClusteringAlgorithm{

    /**
     * Customised SPEC algorithm
     *
     * fi^ = fi_1
     * Lambda(L) = L;
     *
     * Spectral Feature Selection for
     * Supervised and Unsupervised Learning. Zheng Zhao, Huan Liu
     */


    private Instances data;

    private DistanceFunction distanceFunction;

    private int numOfInstances;
    private int numOfAttributes;

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
    private DoubleMatrix1D d_minus_1_2;
    private DoubleMatrix1D d;


    public SPEC(Instances instances, DistanceFunction df) {
        setName("SPEC score");
        this.data = new Instances(instances);
        this.distanceFunction = df;
        df.setInstances(data);
        numOfInstances = data.numInstances();
        numOfAttributes = data.numAttributes();
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
        d = DoubleFactory1D.dense.make(n);
        d_minus_1_2 = DoubleFactory1D.dense.make(n);
        d_1_2 = DoubleFactory1D.dense.make(n);
        for (int i = 0; i < n; i++) {
            double d_i = W.viewRow(i).zSum();
            d.set(i, d_i);
            d_minus_1_2.set(i, 1 / Math.sqrt(d_i));
            d_1_2.set(i, Math.sqrt(d_i));
        }

        final DoubleMatrix2D D = DoubleFactory2D.sparse.diagonal(d);
        final DoubleMatrix2D X = D.copy();
        // (D - W)
        X.assign(W, Functions.minus);
        return X;
    }

    private DoubleMatrix2D getLambdaL() {
        int n = numOfInstances;

        DoubleMatrix2D W = DoubleFactory2D.dense.make(numOfInstances, numOfInstances);
        W.assign(getW());
        d = DoubleFactory1D.dense.make(n);
        d_minus_1_2 = DoubleFactory1D.dense.make(n);
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
        // fi = (D^(1/2)f_i / ||D^(1/2)f_i||)^T * (D - W) * (D^(1/2)f_i / ||D^(1/2)f_i||) =
        // = ((f_i)^T * L * (f_i)) / ((f_i)^T * D * (f_i))

        DoubleMatrix2D F = DoubleFactory2D.dense.make(numOfInstances, data.numAttributes());
        F.assign(getFeatureMatrix());

        DoubleMatrix2D L = DoubleFactory2D.dense.make(numOfInstances, numOfInstances);
        L.assign(getL());

        Algebra alg = new Algebra();

        DoubleMatrix2D featureTransposed = DoubleFactory2D.dense.make(1, numOfInstances);
        for (int i = 0; i < numOfInstances; i++)
            featureTransposed.set(0, i, F.viewColumn(featureNum).get(i));


        DoubleMatrix2D D = DoubleFactory2D.sparse.diagonal(d);

        Double numerator = alg.mult(alg.mult(featureTransposed, L).viewRow(0), F.viewColumn(featureNum));
        Double denominator = alg.mult(alg.mult(D, F.viewColumn(featureNum)), F.viewColumn(featureNum));


        return numerator / denominator;
    }
}
