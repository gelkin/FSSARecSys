package ru.ifmo.ctddev.FSSARecSys.alternative.internal;

import ru.ifmo.ctddev.FSSARecSys.utils.ClusterCentroid;
import weka.clusterers.SimpleKMeans;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.clusterers.Clusterer;

import java.util.ArrayList;
import java.util.Collections;
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

    private Double NW;

    //clusterer should be already be built on the whole dataset
    public Clusterisation(ArrayList<Instances> clusters, Clusterer clusterer) {
        this.clusters = clusters;
        this.clusterer = Objects.requireNonNull(clusterer);
        numOfClusters = clusters.size();

        unitedClusters = getAllInstances();

        centroids = new Instances(clusters.get(0), 0);

        NW = nW();
        //NW = 0.0;
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

    //** all the formulars are in An extensive comparative study of cluster validity indices
    //Olatz Arbelaitz, Ibai Gurrutxagan, Javier Muguerza,Jesus M.Perez, Inigo Perona

    // ** Dunn's index **

    public Double DunnIndex(){

        //get max inter-cluster distance

        EuclideanDistance allInstancedDist = new EuclideanDistance(unitedClusters);

        Double maxTotal = Double.NEGATIVE_INFINITY;
        Double maxForCluster = Double.MIN_VALUE;
        for (int i = 0; i < numOfClusters; i++) {
            Instances currCluster = clusters.get(i);
            //maxTotal = Double.NEGATIVE_INFINITY;
            maxForCluster = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < currCluster.numInstances(); j++) {
                Instance currInstance = currCluster.instance(j);
                maxForCluster = Double.NEGATIVE_INFINITY;
                for (int k = 0; k < currCluster.numInstances(); k++) {
                    if (j != k)
                        maxForCluster = Double.max(maxForCluster,
                                allInstancedDist.distance(currInstance, currCluster.instance(k)));
                }
            }
            maxTotal = Double.max(maxTotal, maxForCluster);
        }

        //get min intra-cluster distance

        Double minIntraclusterDistance = Double.POSITIVE_INFINITY;
        Double minLocalDistance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < numOfClusters - 1; i++) {
            for (int j = i + 1; j < numOfClusters; j++) {
                Instances clusterI = clusters.get(i);
                Instances clusterJ = clusters.get(j);

                minLocalDistance = Double.POSITIVE_INFINITY;
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
            sTemp.add(Double.NEGATIVE_INFINITY);
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
            dTemp.add(Double.NEGATIVE_INFINITY);
        }

        EuclideanDistance centroidDist = new EuclideanDistance(centroids);

        Double maxVal = Double.NEGATIVE_INFINITY;
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
            for (int j = i + 1; j < numOfClusters; j++) {
                dens += den2(i, j, stdDevVal) / Math.max(den1(i, stdDevVal), den1(j, stdDevVal));
            }
        }
        dens /= numOfClusters * (numOfClusters - 1);

        return scat + dens;
    }

    //** CS-index **

    public Double CS() throws Exception {

        Double numerator = 0.0;

        for (int i = 0; i < numOfClusters; i++) {
            Instances currCluster = clusters.get(i);
            int currSize = currCluster.numInstances();
            EuclideanDistance e = new EuclideanDistance(currCluster);
            Double maxDist = Double.NEGATIVE_INFINITY;
            Double sum = 0.0;
            for (int j = 0; j < currSize - 1; j++) {
                for (int k = j; k < currSize; k++) {
                    Instance first = currCluster.instance(j);
                    Instance second = currCluster.instance(k);
                    maxDist = Double.max(maxDist, e.distance(first, second));
                }
                sum += maxDist;
            }
            sum /= currSize;
            numerator += sum;
        }

        Double denominator = 0.0;

        EuclideanDistance eCent = new EuclideanDistance(centroids);
        for (int i = 0; i < numOfClusters - 1; i++) {
            Double minVal = Double.POSITIVE_INFINITY;
            for (int j = i + 1; j < numOfClusters; j++) {
                minVal = Double.min(minVal, eCent.distance(centroids.instance(i), centroids.instance(j)));
            }
            denominator += minVal;
        }

        return numerator / denominator;
    }


    // ** COP-index (used mostly for Hierarchical algo) **

    public Double COP(){
        Double result = 0.0;
        EuclideanDistance eclAll = new EuclideanDistance(unitedClusters);

        for (int i = 0; i < numOfClusters; i++){

            Double numerator = 0.0;

            Instances currCluster = clusters.get(i);
            currCluster.add(centroids.instance(i));
            Double sum = 0.0;
            EuclideanDistance e = new EuclideanDistance(currCluster);
            for (int j = 0; j < currCluster.numInstances() - 1; j++) {
                sum += e.distance(currCluster.instance(j), currCluster.lastInstance());
            }
            sum /= (currCluster.numInstances() - 1);
            currCluster.delete(currCluster.numInstances() - 1);
            numerator = sum;

            Double denominator = 0.0;

            Double minDist = Double.POSITIVE_INFINITY;
            for (int j = 0; j < numOfClusters; j++) {
                if (i != j) {
                    Instances comparedCluster = clusters.get(j);
                    for (int k = 0; k < comparedCluster.numInstances(); k++) {
                        Double maxDist = Double.NEGATIVE_INFINITY;
                        for (int p = 0; p < currCluster.numInstances(); p++)
                            maxDist = Double.max(maxDist, eclAll.distance(comparedCluster. instance(k), currCluster.instance(p)));
                        minDist = Double.min(minDist, maxDist);
                    }
                }
            }
            denominator = minDist;
            result += (numerator / denominator) * currCluster.numInstances();
        }
        return result / unitedClusters.numInstances();
    }

    // ** SV-index **

    public Double SV(){
        Double numerator = 0.0;

        EuclideanDistance e = new EuclideanDistance(centroids);

        Double minCentrDist = Double.POSITIVE_INFINITY;
        for (int i = 0; i < numOfClusters; i++) {
            for (int j = i + 1; j < numOfClusters; j++) {
                minCentrDist = Double.min(minCentrDist, e.distance(centroids.instance(i), centroids.instance(j)));
            }
        }
        numerator = minCentrDist;

        Double denominator = 0.0;

        for (int i = 0; i < numOfClusters; i++){
            Instances currCluster = clusters.get(i);
            Double sum = 0.0;
            //Double maxToCentrDist = Double.NEGATIVE_INFINITY;

            currCluster.add(centroids.instance(i));
            EuclideanDistance ecl = new EuclideanDistance(currCluster);

            ArrayList<Double> dist = new ArrayList<>();
            for (int j = 0; j < currCluster.numInstances() - 1; j++) {
                dist.add(ecl.distance(currCluster.instance(j), currCluster.lastInstance()));
                //maxToCentrDist = Double.max(maxToCentrDist, ecl.distance(currCluster.instance(j), currCluster.lastInstance()));
            }
            currCluster.delete(currCluster.numInstances() - 1);
            Collections.sort(dist);
            Collections.reverse(dist);

            for (int j = 0; j < 0.1 * (currCluster.numInstances()); j++){
                sum += dist.get(j);
            }
            sum *= 10;
            sum /= currCluster.numInstances();
            denominator += sum;
        }

        return numerator / denominator;
    }

    // ** OS-index **

    private Double aOS(int xi, int ck){
        Instances cluster = clusters.get(ck);
        EuclideanDistance e = new EuclideanDistance(cluster);
        Double sum = 0.0;
        for (int i = 0; i < cluster.numInstances(); i++) {
            sum += e.distance(cluster.instance(xi), cluster.instance(i));
        }
        return sum / cluster.numInstances();
    }

    private Double bOS(int xi, int ck){
        Instances cluster = clusters.get(ck);
        EuclideanDistance e = new EuclideanDistance(unitedClusters);

        Double sum = 0.0;
        ArrayList<Double> dist = new ArrayList<>();
        for (int i = 0; i < numOfClusters; i++) {
            if (i != ck) {
                Instances currCluster = clusters.get(i);
                for (int j = 0; j < currCluster.numInstances(); j++) {
                    dist.add(e.distance(currCluster.instance(j), cluster.instance(xi)));
                }
                Collections.sort(dist);
                for (int j = 0; j < currCluster.numInstances(); j++){
                    sum += dist.get(j);
                }
            }

        }
        return sum /= cluster.numInstances();
    }

    private Double ovOS(int xi, int ck){
        Double a = aOS(xi, ck);
        Double b = bOS(xi, ck);
        if (((b - a) / (b + a)) < 0.4) {
            return a / b;
        } else {
            return 0.0;
        }
    }

    public Double OS(){
        Double numerator = 0.0;

        for (int i = 0; i < numOfClusters; i++) {
            Instances currCluster = clusters.get(i);
            for (int j = 0; j < currCluster.numInstances(); j++) {
                numerator += ovOS(j, i);
            }
        }

        Double denominator = 0.0;

        for (int i = 0; i < numOfClusters; i++){
            Instances currCluster = clusters.get(i);
            Double sum = 0.0;
            //Double maxToCentrDist = Double.NEGATIVE_INFINITY;

            currCluster.add(centroids.instance(i));
            EuclideanDistance ecl = new EuclideanDistance(currCluster);

            ArrayList<Double> dist = new ArrayList<>();
            for (int j = 0; j < currCluster.numInstances() - 1; j++) {
                dist.add(ecl.distance(currCluster.instance(j), currCluster.lastInstance()));
                //maxToCentrDist = Double.max(maxToCentrDist, ecl.distance(currCluster.instance(j), currCluster.lastInstance()));
            }
            currCluster.delete(currCluster.numInstances() - 1);
            Collections.sort(dist);
            Collections.reverse(dist);

            for (int j = 0; j < 0.1 * (currCluster.numInstances()); j++){
                sum += dist.get(j);
            }
            sum *= 10;
            sum /= currCluster.numInstances();
            denominator += sum;
        }
        return numerator / denominator;
    }

    // ** Sym-index **

    private Instance getSpecialInstance(int xi, int ck){
        Instance curr = clusters.get(ck).instance(xi);
        Instance centroid = centroids.instance(ck);
        int numOfAttributes = curr.numAttributes();

        double [] first = curr.toDoubleArray();
        double [] second = centroid.toDoubleArray();
        double [] result = new double[numOfAttributes];

        for (int i = 0; i < numOfAttributes; i++) {
            result[i] = 2 * second[i] - first[i];
        }
        return new Instance(1.0, result);
    }

    private Double dps(int xi, int ck) {
        Instances cluster = clusters.get(ck);
        ArrayList<Double> dist = new ArrayList<>();

        for (int i = 0; i < cluster.numInstances(); i++) {
            Instance spec = getSpecialInstance(xi, ck);
            cluster.add(spec);
            EuclideanDistance e = new EuclideanDistance(cluster);
            Double distance = e.distance(cluster.instance(i), cluster.lastInstance());
            cluster.delete(cluster.numInstances() - 1);
            dist.add(distance);
        }
        Collections.sort(dist);
        return 0.5 * (dist.get(0) + dist.get(1));
    }

    public Double SymIndex(){
        Double numerator = 0.0;
        EuclideanDistance e = new EuclideanDistance(centroids);
        Double maxCentrDist = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < numOfClusters; i++)
            for (int j = 0; j < numOfClusters; j++) {
                maxCentrDist = Double.max(maxCentrDist, e.distance(centroids.instance(i), centroids.instance(j)));
            }
        numerator = maxCentrDist;

        Double denominator = 0.0;
        for (int i = 0; i < numOfClusters; i++) {
            Instances currCluster = clusters.get(i);
            for (int j = 0; j < currCluster.numInstances(); j++) {
                denominator += dps(j, i);
            }
        }
        denominator *= numOfClusters;

        return numerator / denominator;
    }

    // ** Gamma index **

    private Double dl(int t1, int t2, int ck) {
        int result = 0;
        EuclideanDistance e = new EuclideanDistance(unitedClusters);
        Instances comparedCluster = clusters.get(ck);
        Double distance = e.distance(comparedCluster.instance(t1), comparedCluster.instance(t2));
        for (int i = 0; i < numOfClusters - 1; i++) {
            for (int j = i + 1; j < numOfClusters; j++) {
                if (i != j){
                    Instances clusterFirst = clusters.get(i);
                    Instances clusterSecond = clusters.get(j);

                    for (int k = 0; k < clusterFirst.numInstances(); k++)
                        for (int p = 0; p < clusterSecond.numInstances(); p++)
                            if (e.distance(clusterFirst.instance(k), clusterSecond.instance(p)) < distance)
                                result++;
                }
            }
        }
        Integer answer = result;
        return answer.doubleValue();
    }

    private Double fact(Double num) {
        return (num == 0.0) ? 1.0 : num * fact(num - 1.0);
    }

    private Double nW(){
        Double result = 0.0;
        for (int i = 0; i < numOfClusters; i++) {
            int n = clusters.get(i).numInstances();
            Double num = Double.valueOf(n);
            if (n > 2) {
//                Double nf = fact(num);
//                Double kf = fact(2.0) * fact(num - 2.0);
                result += num * (num - 1.0) / 2.0;
            }
        }
        return result;
    }

    public Double Gamma(){
        Double numerator = 0.0;

        for (int i = 0; i < numOfClusters; i++) {
            Instances cluster = clusters.get(i);
            for (int j = 0; j < cluster.numInstances() - 1; j++) {
                for (int k = j + 1; k < cluster.numInstances(); k++){
                    numerator += dl(j, k, i);
                }
            }
        }

        Double denominator = 0.0;

        int N = unitedClusters.numInstances();
        //Double tmpN2 = fact(Double.valueOf(N)) / (fact(2.0) * fact(Double.valueOf(N - 2)));
        Double N2 = (N * (N - 1)) / 2.0 ;//tmpN2.doubleValue();

        //NW = nW();

        denominator = NW * (N2 - NW);

        return numerator / denominator;
    }

    // ** C-index **

    private Double SCI() {
        Double result = 0.0;
        for (int i = 0; i < numOfClusters; i++) {
            Instances cluster = clusters.get(i);
            EuclideanDistance e = new EuclideanDistance(cluster);
            int size = cluster.numInstances();
            for (int j = 0; j < size - 1; j++) {
                for (int k = j + 1; k < size; k++) {
                    result += e.distance(cluster.instance(j), cluster.instance(k));
                }
            }
        }
        return result;
    }

    private ArrayList<Double> getAllDistances() {
        ArrayList<Double> dist = new ArrayList<>();
        EuclideanDistance e = new EuclideanDistance(unitedClusters);
        for (int i = 0; i < numOfClusters; i++) {
            for (int j = i; j < numOfClusters; j++) {
                Instances clusterFirst = clusters.get(i);
                Instances clusterSecond = clusters.get(j);

                int sizeFirst = clusterFirst.numInstances();
                int sizeSecond = clusterSecond.numInstances();

                for (int k = 0; k < sizeFirst; k++) {
                    for (int p = 0; p < sizeSecond; p++) {
                        dist.add(e.distance(clusterFirst.instance(k), clusterSecond.instance(p)));
                    }
                }

            }
        }
        return dist;
    }

    private Double SCIMin(ArrayList<Double> dist){
        Double result = 0.0;
        ArrayList<Double> localDist = dist;
        Collections.sort(localDist);
        for (int i = 0; i < NW; i++) {
            result += localDist.get(i);
        }
        return result;
    }

    private Double SCIMax(ArrayList<Double> dist){
        Double result = 0.0;
        ArrayList<Double> localDist = dist;
        Collections.sort(localDist);
        Collections.reverse(localDist);
        for (int i = 0; i < NW; i++) {
            result += localDist.get(i);
        }
        return result;
    }

    public Double CI(){
        Double S = SCI();
        ArrayList<Double> dist = getAllDistances();

        Double Smin = SCIMin(dist);
        Double Smax = SCIMax(dist);

        return (S - Smin) / (Smax - Smin);
    }

    // ** DB-star **

    public Double DaviesBouldinStarIndex() throws Exception {
        //count S_i = 1 / |C_i| sum_all_xi(dist(x_i, centr_i))

        ArrayList<Double> sTemp = new ArrayList<>(numOfClusters);
        ArrayList<EuclideanDistance> euclideanDistances = new ArrayList<>(numOfClusters);

        for (int i = 0; i < numOfClusters; i++){
            sTemp.add(Double.NEGATIVE_INFINITY);
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

        //count D_i = max_j (i!=j) {(S_i + S_j)} / min(i!=j) {dist(centr_i, centr_j)}

        ArrayList<Double> dTemp = new ArrayList<>(numOfClusters);
        for (int i = 0; i < numOfClusters; i++){
            dTemp.add(Double.NEGATIVE_INFINITY);
        }

        EuclideanDistance centroidDist = new EuclideanDistance(centroids);

        Double maxVal = Double.NEGATIVE_INFINITY;
        Double minVal = Double.POSITIVE_INFINITY;
        for (int i = 0; i < clusters.size(); i++) {
            maxVal = Double.NEGATIVE_INFINITY;
            minVal = Double.POSITIVE_INFINITY;
            for (int j = 0; j < clusters.size(); j++)
                if (i != j) {
                    maxVal = Double.max(maxVal, (sTemp.get(i) + sTemp.get(j)));
                    minVal = Double.min(minVal, centroidDist.distance(centroids.instance(i), centroids.instance(j)));
                }
            dTemp.set(i, maxVal / minVal);
        }
        Double result = 0.0;

        for (Double i: dTemp) {
            result += i;
        }

        return result / numOfClusters;
    }

    // ** Sym-DB **

    public Double SymDB() throws Exception {
        ArrayList<Double> sTemp = new ArrayList<>(numOfClusters);
        ArrayList<EuclideanDistance> euclideanDistances = new ArrayList<>(numOfClusters);

        //count C_i = (1 / |c_k|) * sum (x in c_k) {dps(x, centr_k)}

        Double tmpSum = 0.0;
        for (int i = 0; i < numOfClusters; i++) {
            Instances currCluster = clusters.get(i);
            for (int j = 0; j < currCluster.numInstances(); j++) {
                tmpSum += dps(j, i);
            }
            sTemp.set(i, tmpSum / currCluster.numInstances());
        }

        //count D_i = max_j (i!=j) {(S_i + S_j) / dist(centr_i, centr_j)}

        ArrayList<Double> dTemp = new ArrayList<>(numOfClusters);
        for (int i = 0; i < numOfClusters; i++){
            dTemp.add(Double.NEGATIVE_INFINITY);
        }

        EuclideanDistance centroidDist = new EuclideanDistance(centroids);

        Double maxVal = Double.NEGATIVE_INFINITY;
        Double minVal = Double.POSITIVE_INFINITY;
        for (int i = 0; i < clusters.size(); i++) {
            maxVal = Double.NEGATIVE_INFINITY;
            minVal = Double.POSITIVE_INFINITY;
            for (int j = 0; j < clusters.size(); j++)
                if (i != j) {
                    maxVal = Double.max(maxVal, (sTemp.get(i) + sTemp.get(j)));
                    minVal = Double.min(minVal, centroidDist.distance(centroids.instance(i), centroids.instance(j)));
                }
            dTemp.set(i, maxVal / minVal);
        }
        Double result = 0.0;

        for (Double i: dTemp) {
            result += i;
        }

        return result / numOfClusters;
    }


    // ** gD31 **

    private Double delta3(int c1, int c2) {
        Instances cluster1 = clusters.get(c1);
        Instances cluster2 = clusters.get(c2);

        int size1 = cluster1.numInstances();
        int size2 = cluster2.numInstances();

        EuclideanDistance allInstancedDist = new EuclideanDistance(unitedClusters);
        Double result = 0.0;
        for (int i = 0; i < size1; i++) {
            for (int j = 0; j < size2; j++) {
                result += allInstancedDist.distance(cluster1.instance(i), cluster2.instance(j));
            }
        }
        result /= (size1 * size2);
        return result;
    }


    public Double gD31(){

        //get max inter-cluster distance

        EuclideanDistance allInstancedDist = new EuclideanDistance(unitedClusters);

        Double maxTotal = Double.NEGATIVE_INFINITY;
        Double maxForCluster = Double.MIN_VALUE;
        for (int i = 0; i < numOfClusters; i++) {
            Instances currCluster = clusters.get(i);
            //maxTotal = Double.NEGATIVE_INFINITY;
            maxForCluster = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < currCluster.numInstances(); j++) {
                Instance currInstance = currCluster.instance(j);
                maxForCluster = Double.NEGATIVE_INFINITY;
                for (int k = 0; k < currCluster.numInstances(); k++) {
                    if (j != k)
                        maxForCluster = Double.max(maxForCluster,
                                allInstancedDist.distance(currInstance, currCluster.instance(k)));
                }
            }
            maxTotal = Double.max(maxTotal, maxForCluster);
        }

        //get min intra-cluster distance

        Double minIntraclusterDistance = Double.POSITIVE_INFINITY;
        Double minLocalDistance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < numOfClusters - 1; i++) {
            minLocalDistance = Double.POSITIVE_INFINITY;
            for (int j = i + 1; j < numOfClusters; j++) {
                minLocalDistance = Double.min(minLocalDistance, delta3(i, j));
            }
            minIntraclusterDistance = Double.min(minIntraclusterDistance, minLocalDistance);
        }

        return minIntraclusterDistance / maxTotal;
    }

    // ** gD41 **

    public Double gD41(){

        //get max inter-cluster distance

        EuclideanDistance allInstancedDist = new EuclideanDistance(unitedClusters);

        Double maxTotal = Double.NEGATIVE_INFINITY;
        Double maxForCluster = Double.MIN_VALUE;
        for (int i = 0; i < numOfClusters; i++) {
            Instances currCluster = clusters.get(i);
            //maxTotal = Double.NEGATIVE_INFINITY;
            maxForCluster = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < currCluster.numInstances(); j++) {
                Instance currInstance = currCluster.instance(j);
                maxForCluster = Double.NEGATIVE_INFINITY;
                for (int k = 0; k < currCluster.numInstances(); k++) {
                    if (j != k)
                        maxForCluster = Double.max(maxForCluster,
                                allInstancedDist.distance(currInstance, currCluster.instance(k)));
                }
            }
            maxTotal = Double.max(maxTotal, maxForCluster);
        }

        //get min intra-cluster distance

        Double minIntraclusterDistance = Double.POSITIVE_INFINITY;
        Double minLocalDistance = Double.POSITIVE_INFINITY;
        EuclideanDistance e = new EuclideanDistance(centroids);

        for (int i = 0; i < numOfClusters - 1; i++) {
            minLocalDistance = Double.POSITIVE_INFINITY;
            for (int j = i + 1; j < numOfClusters; j++) {
                minLocalDistance = Double.min(minLocalDistance, e.distance(centroids.instance(i), centroids.instance(j)));
            }
            minIntraclusterDistance = Double.min(minIntraclusterDistance, minLocalDistance);
        }

        return minIntraclusterDistance / maxTotal;
    }

    // ** gD51 **

    private Double delta5(int c1, int c2) {
        Double result;
        Instances cluster1 = clusters.get(c1);
        Instances cluster2 = clusters.get(c2);

        int size1 = cluster1.numInstances();
        int size2 = cluster2.numInstances();

        cluster1.add(centroids.instance(c1));
        cluster2.add(centroids.instance(c2));

        EuclideanDistance d1 = new EuclideanDistance(cluster1);
        EuclideanDistance d2 = new EuclideanDistance(cluster2);

        Double sum1 = 0.0;
        for (int i = 0; i < size1; i++) {
            sum1 += d1.distance(cluster1.instance(i), cluster1.lastInstance());
        }

        Double sum2 = 0.0;
        for (int i = 0; i < size2; i++) {
            sum2 += d2.distance(cluster2.instance(i), cluster2.lastInstance());
        }

        cluster1.delete(cluster1.numInstances() - 1);
        cluster2.delete(cluster2.numInstances() - 1);

        result = (sum1 + sum2) / (size1 + size2);

        return result;
    }

    public Double gD51(){

        //get max inter-cluster distance

        EuclideanDistance allInstancedDist = new EuclideanDistance(unitedClusters);

        Double maxTotal = Double.NEGATIVE_INFINITY;
        Double maxForCluster = Double.MIN_VALUE;
        for (int i = 0; i < numOfClusters; i++) {
            Instances currCluster = clusters.get(i);
            //maxTotal = Double.NEGATIVE_INFINITY;
            maxForCluster = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < currCluster.numInstances(); j++) {
                Instance currInstance = currCluster.instance(j);
                maxForCluster = Double.NEGATIVE_INFINITY;
                for (int k = 0; k < currCluster.numInstances(); k++) {
                    if (j != k)
                        maxForCluster = Double.max(maxForCluster,
                                allInstancedDist.distance(currInstance, currCluster.instance(k)));
                }
            }
            maxTotal = Double.max(maxTotal, maxForCluster);
        }

        //get min intra-cluster distance

        Double minIntraclusterDistance = Double.POSITIVE_INFINITY;
        Double minLocalDistance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < numOfClusters - 1; i++) {
            minLocalDistance = Double.POSITIVE_INFINITY;
            for (int j = i + 1; j < numOfClusters; j++) {
                minLocalDistance = Double.min(minLocalDistance, delta5(i, j));
            }
            minIntraclusterDistance = Double.min(minIntraclusterDistance, minLocalDistance);
        }

        return minIntraclusterDistance / maxTotal;
    }

    // ** gD33 **

    public Double gD33(){

        //get max inter-cluster distance

        Double maxTotal = Double.NEGATIVE_INFINITY;
        Double maxForCluster = Double.MIN_VALUE;
        for (int i = 0; i < numOfClusters; i++) {
            Instances currCluster = clusters.get(i);
            Double result;

            int size1 = currCluster.numInstances();
            currCluster.add(centroids.instance(i));
            EuclideanDistance d1 = new EuclideanDistance(currCluster);
            Double sumTmp = 0.0;
            for (int j = 0; j < size1; j++) {
                sumTmp += d1.distance(currCluster.instance(i), currCluster.lastInstance());
            }
            currCluster.delete(currCluster.numInstances() - 1);

            sumTmp *= 2;
            sumTmp /=  size1;
            maxTotal = Double.max(maxTotal, sumTmp);
        }

        //get min intra-cluster distance

        Double minIntraclusterDistance = Double.POSITIVE_INFINITY;
        Double minLocalDistance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < numOfClusters - 1; i++) {
            minLocalDistance = Double.POSITIVE_INFINITY;
            for (int j = i + 1; j < numOfClusters; j++) {
                minLocalDistance = Double.min(minLocalDistance, delta3(i, j));
            }
            minIntraclusterDistance = Double.min(minIntraclusterDistance, minLocalDistance);
        }

        return minIntraclusterDistance / maxTotal;
    }

    // ** gD43 **

    public Double gD43(){

        //get max inter-cluster distance

        Double maxTotal = Double.NEGATIVE_INFINITY;
        Double maxForCluster = Double.MIN_VALUE;
        for (int i = 0; i < numOfClusters; i++) {
            Instances currCluster = clusters.get(i);
            Double result;

            int size1 = currCluster.numInstances();
            currCluster.add(centroids.instance(i));
            EuclideanDistance d1 = new EuclideanDistance(currCluster);
            Double sumTmp = 0.0;
            for (int j = 0; j < size1; j++) {
                sumTmp += d1.distance(currCluster.instance(i), currCluster.lastInstance());
            }
            currCluster.delete(currCluster.numInstances() - 1);

            sumTmp *= 2;
            sumTmp /=  size1;
            maxTotal = Double.max(maxTotal, sumTmp);
        }

        //get min intra-cluster distance

        Double minIntraclusterDistance = Double.POSITIVE_INFINITY;
        Double minLocalDistance = Double.POSITIVE_INFINITY;
        EuclideanDistance e = new EuclideanDistance(centroids);

        for (int i = 0; i < numOfClusters - 1; i++) {
            minLocalDistance = Double.POSITIVE_INFINITY;
            for (int j = i + 1; j < numOfClusters; j++) {
                minLocalDistance = Double.min(minLocalDistance, e.distance(centroids.instance(i), centroids.instance(j)));
            }
            minIntraclusterDistance = Double.min(minIntraclusterDistance, minLocalDistance);
        }

        return minIntraclusterDistance / maxTotal;
    }

    // ** gD53 **

    public Double gD53(){

        //get max inter-cluster distance

        Double maxTotal = Double.NEGATIVE_INFINITY;
        Double maxForCluster = Double.MIN_VALUE;
        for (int i = 0; i < numOfClusters; i++) {
            Instances currCluster = clusters.get(i);
            Double result;

            int size1 = currCluster.numInstances();
            currCluster.add(centroids.instance(i));
            EuclideanDistance d1 = new EuclideanDistance(currCluster);
            Double sumTmp = 0.0;
            for (int j = 0; j < size1; j++) {
                sumTmp += d1.distance(currCluster.instance(i), currCluster.lastInstance());
            }
            currCluster.delete(currCluster.numInstances() - 1);
            sumTmp *= 2;
            sumTmp /=  size1;
            maxTotal = Double.max(maxTotal, sumTmp);
        }

        //get min intra-cluster distance

        Double minIntraclusterDistance = Double.POSITIVE_INFINITY;
        Double minLocalDistance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < numOfClusters - 1; i++) {
            minLocalDistance = Double.POSITIVE_INFINITY;
            for (int j = i + 1; j < numOfClusters; j++) {
                minLocalDistance = Double.min(minLocalDistance, delta5(i, j));
            }
            minIntraclusterDistance = Double.min(minIntraclusterDistance, minLocalDistance);
        }

        return minIntraclusterDistance / maxTotal;
    }



}
