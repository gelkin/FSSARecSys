package ru.ifmo.ctddev.FSSARecSys.alternative.internal;

import ru.ifmo.ctddev.FSSARecSys.utils.ClusterCentroid;
import weka.clusterers.SimpleKMeans;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.clusterers.Clusterer;
import weka.core.matrix.Matrix;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Администратор on 19.08.2015.
 */
public class Clusterisation {

    private ArrayList<Instances> clusters;
    private Instances unitedClusters;
    private Instances centroids;
    private Clusterer clusterer;

    private Integer numOfClusters;

    //clusterer should be already be built on the whole dataset
    public Clusterisation(ArrayList<Instances> clusters, Clusterer clusterer) {
        this.clusters = clusters;
        this.clusterer = Objects.requireNonNull(clusterer);
        numOfClusters = clusters.size();

        unitedClusters = getAllInstances();

        centroids = new Instances(clusters.get(0), 0);
    }

    public Integer getNumClusters(){
        return numOfClusters;
    }

    public Instance getClusterCentroid(int clusterNum) throws Exception {
        if (clusterer instanceof SimpleKMeans) {
            return ((SimpleKMeans) clusterer).getClusterCentroids().instance(clusterNum);
        } else {
            ClusterCentroid ct = new ClusterCentroid();
            return ct.findCentroid(clusterNum, clusters.get(clusterNum));
        }
    }

    public Instance getDatasetCentroid(){
        ClusterCentroid ct = new ClusterCentroid();
        return ct.findCentroid(0, unitedClusters);
    }

    private Instances getAllInstances(){
        Instances all = new Instances(clusters.get(0));
        for (Instances cluster: clusters) {
            for (int i = 1; i < cluster.numInstances(); i++){
                all.add(cluster.instance(i));
            }
        }
        return all;
    }

    // ** Dunn's index **

    public Double DunnIndex(){

        //get max inter-cluster distance

        EuclideanDistance allInstancedDist = new EuclideanDistance(unitedClusters);

        Double maxTotal = Double.MIN_VALUE;
        Double maxForCluster = Double.MIN_VALUE;
        for (int i = 0; i < numOfClusters; i++) {
            Instances currCluster = clusters.get(i);
            maxTotal = Double.MIN_VALUE;
            maxForCluster = Double.MIN_VALUE;
            for (int j = 0; j < currCluster.numInstances(); j++) {
                Instance currInstance = currCluster.instance(j);
                maxForCluster = Double.MIN_VALUE;
                for (int k = 0; k < currCluster.numInstances(); k++) {
                    if (j != k)
                        maxForCluster = Double.max(maxForCluster,
                                allInstancedDist.distance(currInstance, currCluster.instance(k)));
                }
            }
            maxTotal = Double.max(maxTotal, maxForCluster);
        }

        //get min intra-cluster distance

        Double minIntraclusterDistance = Double.MAX_VALUE;
        Double minLocalDistance = Double.MAX_VALUE;
        for (int i = 0; i < numOfClusters - 1; i++) {
            for (int j = i; j < numOfClusters; j++) {
                Instances clusterI = clusters.get(i);
                Instances clusterJ = clusters.get(j);

                minLocalDistance = Double.MAX_VALUE;
                for (int k = 0; k < clusterI.numInstances(); k++) {
                    for (int p = 0; p < clusterJ.numInstances(); p++) {
                        Instance first = clusterI.instance(k);
                        Instance second = clusterJ.instance(p);
                        minLocalDistance = Double.min(minLocalDistance, allInstancedDist.distance(first, second));
                    }
                }
            }
            minIntraclusterDistance = Double.min(minIntraclusterDistance, minLocalDistance);
        }
        return minIntraclusterDistance / maxTotal;
    }

    // ** Davies-Bouldin index **

    public Double DaviesBouldinIndex() throws Exception {
        //count S_i = 1 / |C_i| sum_all_xi(dist(x_i, centr_i))

        ArrayList<Double> sTemp = new ArrayList<>(numOfClusters);
        ArrayList<EuclideanDistance> euclideanDistances = new ArrayList<>(numOfClusters);

        for (int i = 0; i < numOfClusters; i++){
            sTemp.add(Double.MIN_VALUE);
            euclideanDistances.add(new EuclideanDistance());
        }

        for (int i = 0; i < numOfClusters; i++) {
            Double sumTmp = 0.0;
            Instances currentCluster = clusters.get(i);
            euclideanDistances.set(i, new EuclideanDistance(currentCluster));

            Instance centroid = getClusterCentroid(i);
            centroids.add(centroid);

            for (int j = 0; j < currentCluster.numInstances(); j++){
                Instance instance = currentCluster.instance(j);
                sumTmp += euclideanDistances.get(i).distance(instance, centroid);
            }
            sTemp.set(i, sumTmp / currentCluster.numInstances());
        }

        //count D_i = max_j (i!=j) {(S_i + S_j) / dist(centr_i, centr_j)}

        ArrayList<Double> dTemp = new ArrayList<>(numOfClusters);
        for (int i = 0; i < numOfClusters; i++){
            dTemp.add(Double.MIN_VALUE);
        }

        EuclideanDistance centroidDist = new EuclideanDistance(centroids);

        Double maxVal = Double.MIN_VALUE;
        for (int i = 0; i < clusters.size(); i++) {
            maxVal = Double.MIN_VALUE;
            for (int j = 0; j < clusters.size(); j++)
                if (i != j) {
                    maxVal = Double.max(maxVal, (sTemp.get(i) + sTemp.get(j)) /
                            centroidDist.distance(centroids.instance(i), centroids.instance(j)));

                }
            dTemp.set(i, maxVal);
        }
        Double result = 0.0;

        for (Double i: dTemp) {
            result += i;
        }

        return result / numOfClusters;
    }

    // ** silhouette index **

    private Double avgInterClusterDistance(int instanceIndex, Instances cluster) {
        Instance current = cluster.instance(instanceIndex);
        EuclideanDistance ed = new EuclideanDistance(cluster);
        Double result = 0.0;
        for (int i = 0; i < cluster.numInstances(); i++) {
            if (instanceIndex != i)
                result += ed.distance(current, cluster.instance(i));
        }
        return result / cluster.numInstances();
    }

    private Double avgIntraClusterDistace(int clusterIndex, Instance x) {
        EuclideanDistance ed = new EuclideanDistance(unitedClusters);

        Double avgDistance = 0.0;
        Double minVal = Double.MAX_VALUE;
        for (int i = 0; i < clusters.size(); i++) {
            avgDistance = 0.0;
            if (i != clusterIndex) {
                Instances currentCluster = clusters.get(i);
                for (int j = 0; j < currentCluster.numInstances(); j++) {
                    Instance currentInstance = currentCluster.instance(j);
                    avgDistance += ed.distance(x, currentInstance);
                }
                minVal = Double.min(minVal, avgDistance);
            }
        }
        return minVal;
    }


    public Double silhouetteIndex(){

        Double result = 0.0;
        Double silhoetteOfInstanse = 0.0;
        for (int i = 0; i < numOfClusters; i++) {
            Instances currentCluster = clusters.get(i);
            silhoetteOfInstanse = 0.0;
            for (int j = 0; j < currentCluster.numInstances(); j++) {
                Double a = avgInterClusterDistance(j, currentCluster);
                Double b = avgIntraClusterDistace(i, currentCluster.instance(j));
                silhoetteOfInstanse += (b - a) / Double.max(a, b);
            }
            result += silhoetteOfInstanse / currentCluster.numInstances();
        }

        return result / numOfClusters;
    }

    // ** Calinski-Harabasz index **

    public Double CalinskiHarabaszIndex(){
        Double numerator = 0.0;

        Instance datasetCentroid = getDatasetCentroid();
        Instances centroidsCpy = new Instances(centroids);
        centroidsCpy.add(datasetCentroid);

        EuclideanDistance e = new EuclideanDistance(centroidsCpy);

        Double sum = 0.0;
        for (int i = 0; i < numOfClusters; i++) {
            sum += clusters.get(i).numInstances() * Math.pow(e.distance(centroidsCpy.instance(i), centroidsCpy.lastInstance()), 2.0);
        }
        numerator = (unitedClusters.numInstances() - numOfClusters) * sum;

        Double denominator = 0.0;

        sum = 0.0;
        for (int i = 0; i < numOfClusters; i++) {
            Instances currCluster = clusters.get(i);
            currCluster.add(centroids.instance(i));
            EuclideanDistance ecl = new EuclideanDistance(currCluster);
            for (int j = 0; j < currCluster.numInstances() - 1; j++) {
                sum += Math.pow(ecl.distance(currCluster.instance(j), currCluster.lastInstance()), 2.0);
            }
        }
        denominator = sum * (numOfClusters - 1);
        return numerator / denominator;
    }

    // ** Score function **

    private Double bcd(){
        Instance datasetCentroid = getDatasetCentroid();
        Instances centroidsCpy = new Instances(centroids);
        centroidsCpy.add(datasetCentroid);

        EuclideanDistance e = new EuclideanDistance(centroidsCpy);

        Double sum = 0.0;
        for (int i = 0; i < numOfClusters; i++) {
            sum += clusters.get(i).numInstances() * e.distance(centroidsCpy.instance(i), centroidsCpy.lastInstance());
        }

        return sum / (numOfClusters * unitedClusters.numInstances());

    }

    private Double wcd(){
        Double result = 0.0;
        for (int i = 0; i < numOfClusters; i++) {
            Double sum = 0.0;
            Instances currCluster = clusters.get(i);
            currCluster.add(centroids.instance(i));
            EuclideanDistance ecl = new EuclideanDistance(currCluster);
            for (int j = 0; j < currCluster.numInstances() - 1; j++) {
                sum += ecl.distance(currCluster.instance(j), currCluster.lastInstance());
            }
            result += (1 / clusters.get(i).numInstances()) * sum;
        }
        return result;
    }

    public Double ScoreFunction(){
        return Math.exp(Math.exp(bcd() + wcd()));
    }

    // ** S_Dbw **

    private Double normedSigma(Instances x, int clusterNum) throws Exception {
        Instance centroid = new Instance(centroids.numAttributes());
        if (clusterNum == -1) {
            centroid = getDatasetCentroid();
        } else {
            centroid = getClusterCentroid(clusterNum);
        }

        double [] centroidArr = centroid.toDoubleArray();

        Instances copyX = new Instances(x);
        copyX.add(centroid);
        EuclideanDistance e = new EuclideanDistance(copyX);

        double [] sum = new double[centroidArr.length];
        for (int i = 0; i < x.numInstances(); i++) {
            Instance current = x.instance(i);
            double [] currArr = current.toDoubleArray();

            for (int j = 0; j < sum.length; j++) {
                if (x.attribute(j).isNumeric())
                    sum[j] += Math.pow(currArr[j] - centroidArr[j], 2.0);
                else {
                    if (x.attribute(j).isNominal()) {
                        sum[j] = currArr[j] == centroidArr[j] ? 0 : 1;
                    }
                }
            }
        }
        Double norm = 0.0;
        for (int i = 0; i < sum.length; i++) {
            norm += Math.pow(sum[i], 2.0);
        }
        norm = Math.sqrt(norm);
        int clusterSize = x.numInstances();

        return (norm / clusterSize);
    }

    private Double stdev() throws Exception {
        Double result = 0.0;
        for (int i = 0; i < numOfClusters; i++) {
            result += normedSigma(clusters.get(i), i);
        }
        return (1 / numOfClusters) * Math.sqrt(result);
    }

    private Double func(Instance a, Instance b, Double stdevVal){
        Instances x = new Instances(centroids, 0);
        x.add(a);
        x.add(b);
        EuclideanDistance e = new EuclideanDistance(x);
        return e.distance(a, b) > stdevVal? 0.0 : 1.0;
    }

    private Double den1(int clusterNum, Double stdevVal) throws Exception {
        Instances cluster = clusters.get(clusterNum);
        Instance centroid = centroids.instance(clusterNum);

        Double result = 0.0;
        for (int i = 0; i < cluster.numInstances(); i++){
            Instance curr = cluster.instance(i);
            result += func(centroid, curr, stdevVal);
        }
        return result;
    }

    private Double den2(int clusterNum1, int clusterNum2, Double stdevVal) {
        Instances cluster1 = clusters.get(clusterNum1);
        Instances cluster2 = clusters.get(clusterNum2);

        Instances union = new Instances(cluster1);
        for (int i = 0; i < cluster2.numInstances(); i++) {
            union.add(cluster2.instance(i));
        }

        Instance centroid1 = centroids.instance(clusterNum1);
        Instance centroid2 = centroids.instance(clusterNum2);

        double [] meanCentrArr = new double[centroids.numAttributes()];
        double [] centr1 = centroid1.toDoubleArray();
        double [] centr2 = centroid2.toDoubleArray();

        for (int i = 0; i < meanCentrArr.length; i++) {
            meanCentrArr[i] = (centr1[i] + centr2[i]) / 2;
        }

        Instance meanCentroid = new Instance(1.0, meanCentrArr);

        Double result = 0.0;
        for (int i = 0; i < union.numInstances(); i++){
            Instance curr = union.instance(i);
            result += func(meanCentroid, curr, stdevVal);
        }
        return result;

    }

    public Double SDbw() throws Exception {
        Double scat = 0.0;
        Double nsTotal = normedSigma(unitedClusters, -1);

        for (int i = 0; i < numOfClusters; i++) {
            Double nsCurr = normedSigma(clusters.get(i), i);
            scat += nsCurr / nsTotal;
        }

        scat /= numOfClusters;

        Double stdDevVal = stdev();
        Double dens = 0.0;

        for (int i = 0; i < numOfClusters - 1; i++) {
            for (int j = i; j < numOfClusters; j++) {
                dens += den2(i, j, stdDevVal) / Math.max(den1(i, stdDevVal), den1(j, stdDevVal));
            }
        }
        dens /= numOfClusters * (numOfClusters - 1);

        return scat + dens;
    }

}
