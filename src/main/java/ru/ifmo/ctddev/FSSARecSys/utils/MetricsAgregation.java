package ru.ifmo.ctddev.FSSARecSys.utils;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.ifmo.ctddev.FSSARecSys.db.internal.Metrics;

import java.awt.image.AreaAveragingScaleFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by sergey on 06.12.15.
 */
public class MetricsAgregation {
    private String result = "result.xlsx";
    public HSSFWorkbook myWorkBook;
    public static ArrayList<Integer> metricsWithBestMin = new ArrayList<>();

    public List<ArrayList<Double>> metrics;
    public List<ArrayList<Double>> asessors;
    public List<Integer> avgAs;
    private Double distWorstBest;

    public ArrayList<ArrayList<Double>> adeqAVG = new ArrayList<>();
    public ArrayList<ArrayList<String>> adeqBest = new ArrayList<>();

    public ArrayList<ArrayList<Double>> rang = new ArrayList<>();
    public ArrayList<ArrayList<String>> rangBest = new ArrayList<>();

    private int best = Integer.MAX_VALUE;

    public void setMyWorkBook(FileInputStream myWorkBook) throws IOException {
        this.myWorkBook = new HSSFWorkbook(myWorkBook);
    }

    private int distanceToBest(List<Integer> permutation, int curdistance) {
        for (int i = 1; i < permutation.size(); i++) {
            if (permutation.get(i - 1) < permutation.get(i)) {
                Collections.swap(permutation, i - 1, i);
                curdistance = distanceToBest(permutation, curdistance + 1);
                break;
            }
        }
        return curdistance;
    }

    public void sheetProcessing(int sheetNum) {
        HSSFSheet currentSheet = myWorkBook.getSheetAt(sheetNum);
        metrics = new ArrayList<>();
        asessors = new ArrayList<>();

        Double min = Double.POSITIVE_INFINITY;

        for (int i = 1; i < 20; i++) {
            HSSFRow row = currentSheet.getRow(i);
            ArrayList<Double> currentMetrics = new ArrayList<>();
            for (int j = 1; j < 15; j++) {
                Cell cell = row.getCell(j);
                switch (cell.getCellType()) {
                    case Cell.CELL_TYPE_STRING: {
                        String infinity = "Infinity";
                        String nan = "NaN";
                        if (cell.getStringCellValue().equals(infinity)){
                            if (i == 9)
                                currentMetrics.add(Double.NEGATIVE_INFINITY);
                            else
                                currentMetrics.add(Double.POSITIVE_INFINITY);
                        }
                        if (cell.getStringCellValue().equals(nan)){
                            currentMetrics.add(Double.NEGATIVE_INFINITY);
                        }
                        break;
                    }
                    case Cell.CELL_TYPE_NUMERIC:
                        currentMetrics.add(cell.getNumericCellValue());
                        //System.out.print(cell.getNumericCellValue() + "\t");
                        break;
                    default :;
                }
            }
            metrics.add(currentMetrics);
        }

        for (int i = 20; i < 25; i++) {
            HSSFRow row = currentSheet.getRow(i);
            ArrayList<Double> currentAsess = new ArrayList<>();
            for (int j = 1; j < 15; j++) {
                Cell cell = row.getCell(j);
                switch (cell.getCellType()) {
                    case Cell.CELL_TYPE_STRING: {
                        String s = cell.getStringCellValue();
                        currentAsess.add(Double.parseDouble(s));
                        break;
                    }
                    case Cell.CELL_TYPE_NUMERIC:
                        currentAsess.add(cell.getNumericCellValue());
                        break;
                    default :;
                }
            }
            asessors.add(currentAsess);
        }
        // adequate / inadequate

        avgAs = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            int count = 0;
            for (int j = 0; j < 5; j++) {
                if (asessors.get(j).get(i) > 0)
                    count++;
            }
            if (count < 2)
                avgAs.add(-1);
            else {
                if (count == 2)
                    avgAs.add(0);
                else
                    avgAs.add(1);
            }
        }

        boolean allNegative = true;

        for (int k = 0; k < avgAs.size(); k++) {
            if (avgAs.get(k) == 1) {
                allNegative = false;
                System.out.print('+');
            }
            if (avgAs.get(k) == 0) {
                allNegative = false;
                System.out.print('*');
            }
            if (avgAs.get(k) == -1) System.out.print('-');
        }
        System.out.println();

        ArrayList<Integer> avgSorted = new ArrayList<>(avgAs);
        Collections.sort(avgSorted);

        ArrayList<Integer> avgSortedRev = new ArrayList<>(avgSorted);
        Collections.reverse(avgSortedRev);

        distWorstBest = (double)distanceToBest(avgSorted, 0);

        double [][] asessArrays = new double [5][14];
        double [][] metricsArrays = new double [19][14];

        for (int i = 0; i < asessArrays.length; i++) {
            for (int j = 0; j < asessArrays[i].length; j++) {
                asessArrays[i][j] = asessors.get(i).get(j);
            }
        }

        for (int i = 0; i < metricsArrays.length; i++) {
            for (int j = 0; j < metricsArrays[i].length; j++) {
                metricsArrays[i][j] = metrics.get(i).get(j);
            }
        }

        double[] wA = SoftRanking.aggregate(asessArrays);
        double[] negWA = Arrays.copyOf(wA, wA.length);

        double currMin = Double.POSITIVE_INFINITY;
        for (int j = 0; j < negWA.length; j++) {
            if(negWA[j] < currMin)
                currMin = negWA[j];
        }

        double epsilon = currMin / 1000;
        for (int j = 0; j < negWA.length -1; j++) {
            ArrayList<Integer> flag = new ArrayList<>();
            flag.add(j);
            for (int k = j + 1; k < negWA.length; k++) {
                if (negWA[j] == negWA[k])
                    flag.add(k);
            }
            if (flag.size() > 1) {
                for (int k = 0; k < flag.size(); k++) {
                    negWA[flag.get(k)] += epsilon * k;
                }
            }
        }

        for (int j = 0; j < negWA.length; j++) {
            negWA[j] *= -1.0;
        }

        double dWorstBest = SoftRanking.distance(wA, negWA);

        for (int i = 0; i < metrics.size(); i++) {

            if (allNegative){
                adeqAVG.get(i).add(1.0);
                adeqBest.get(i).add("-");
            } else {
                ArrayList<Pair<Double, Integer>> permutation = new ArrayList<>();
                List<Integer> copy = new ArrayList<>(avgAs);

                for (int j = 0; j < metrics.get(i).size(); j++) {
                    permutation.add(new Pair<Double, Integer>(metrics.get(i).get(j), copy.get(j)));
                }

//            for (int k = 0; k < permutation.size(); k++) {
//                if (avgAs.get(k) == 1) System.out.print('+');
//                if (avgAs.get(k) == 0) System.out.print('*');
//                if (avgAs.get(k) == -1) System.out.print('-');
//
//            }
//            System.out.println("  ");

                if (metricsWithBestMin.contains(i + 1)) {
                    Collections.sort(permutation, new Comparator<Pair<Double, Integer>>() {
                        @Override
                        public int compare(Pair<Double, Integer> o1, Pair<Double, Integer> o2) {
                            if ((Double) o1.first < (Double) o2.first)
                                return -1;
                            if ((Double) o1.first == (Double) o2.first) {
                                if ((Integer) o1.second < (Integer) o2.second)
                                    return -1;
                                if ((Integer) o1.second == (Integer) o2.second)
                                    return 0;
                                else return 1;
                            } else {
                                return 1;
                            }
                        }
                    });
                } else {
                    Collections.sort(permutation, new Comparator<Pair<Double, Integer>>() {
                        @Override
                        public int compare(Pair<Double, Integer> o1, Pair<Double, Integer> o2) {
                            if ((Double) o1.first < (Double) o2.first)
                                return 1;
                            if ((Double) o1.first == (Double) o2.first) {
                                if ((Integer) o1.second < (Integer) o2.second)
                                    return -1;
                                if ((Integer) o1.second == (Integer) o2.second)
                                    return 0;
                                else return 1;
                            } else {
                                return -1;
                            }
                        }
                    });
                }

//            if (!metricsWithBestMin.contains(i + 1)) {
//                Collections.reverse(permutation);
//            }

                for (int k = 0; k < permutation.size(); k++) {
                    copy.set(k, permutation.get(k).second);
                }

                Integer adeq = permutation.get(0).second;

                if (adeq == 1) adeqBest.get(i).add("+");
                if (adeq == 0) adeqBest.get(i).add("*");
                if (adeq == -1) adeqBest.get(i).add("-");



//            for (int k = 0; k < permutation.size(); k++) {
//                if (copy.get(k) == 1) System.out.print('+');
//                if (copy.get(k) == 0) System.out.print('*');
//                if (copy.get(k) == -1) System.out.print('-');
//
//            }
//            System.out.println("  ");
//
//            for (int k = 0; k < permutation.size(); k++) {
//                System.out.print(permutation.get(k).first + " ");
//            }
//            System.out.println();
//


                Double distCurrBest = (double)distanceToBest(copy, 0);
                Double result = distCurrBest / distWorstBest;

                //System.out.println(result + " " + distCurrBest + " " + distWorstBest);
//            if (result < min) {
//                best = i;
//                min = result;
//            }
                //System.out.println("================");

                adeqAVG.get(i).add(result);
            }

            // ranging

            double dBestCurr = SoftRanking.distance(wA, metricsArrays[i]);

            Double resRange = dBestCurr / dWorstBest;

            rang.get(i).add(resRange);

        }
        //System.out.println(best + 1);


    }

    public ArrayList<ArrayList<Double>> deepAggregation(ArrayList<ArrayList<Double>> evaluations) {
        ArrayList<Set<Double>> unique = new ArrayList<>();
        ArrayList<ArrayList<Double>> sorted = new ArrayList<>();
        for (int i = 0; i < evaluations.get(0).size(); i++) {
            unique.add(new HashSet<>());
            for (int j = 0; j < evaluations.size(); j++) {
                unique.get(i).add(evaluations.get(j).get(i));
            }
            sorted.add(new ArrayList<>());
            sorted.get(i).addAll(unique.get(i));
            Collections.sort(sorted.get(i));
        }
        return sorted;
    }

    public static void main(String [] args) throws IOException {
        File myFile = new File("results.xls");
        FileInputStream fis = new FileInputStream(myFile);

        MetricsAgregation ma = new MetricsAgregation();
        ma.myWorkBook = new HSSFWorkbook(fis);
        Collections.addAll(metricsWithBestMin, 1, 5, 9, 10, 12, 13);

        for (int i = 0; i < 19; i++) {
            ma.adeqAVG.add(new ArrayList<>());
            ma.adeqBest.add(new ArrayList<>());
            ma.rang.add(new ArrayList<>());
        }

        for(int i = 0; i < ma.myWorkBook.getNumberOfSheets(); i++) {
            ma.sheetProcessing(i);
            //System.out.println("========================================");
        }

        for (int i = 0; i < ma.adeqAVG.size(); i++) {
            for (int j = 0; j < ma.adeqAVG.get(i).size(); j++) {
                System.out.printf("%-20s", ma.adeqAVG.get(i).get(j));
            }
            System.out.println();
        }

        System.out.println();
        System.out.println();

        for (int i = 0; i < ma.adeqBest.size(); i++) {
            for (int j = 0; j < ma.adeqBest.get(i).size(); j++) {
                System.out.printf("%-20s", ma.adeqBest.get(i).get(j));
            }
            System.out.println();
        }

        System.out.println();
        System.out.println();

        for (int i = 0; i < ma.rang.size(); i++) {
            for (int j = 0; j < ma.rang.get(i).size(); j++) {
                System.out.printf("%-20s", ma.rang.get(i).get(j));
            }
            System.out.println();
        }

        ArrayList<ArrayList<Double>> uniqueAdeq = ma.deepAggregation(ma.adeqAVG);
        ArrayList<ArrayList<Double>> uniqueRang = ma.deepAggregation(ma.rang);

        ArrayList<ArrayList<Integer>> adeqEvaluation = new ArrayList<>();
        ArrayList<ArrayList<Integer>> rangEvaluation = new ArrayList<>();

        ArrayList<Integer> tmpNull = new ArrayList<>();
        for (int i = 0; i < ma.adeqAVG.size(); i++)
            tmpNull.add(0);

        Double counter = 0.0;
        int n = ma.adeqAVG.size();
        int m = ma.adeqAVG.get(0).size();
        for (int i = 0; i < m; i++) {
            adeqEvaluation.add(new ArrayList<>(tmpNull));
            if (uniqueAdeq.get(i).size() > 1) {
                counter += 1.0;
                for (int j = 0; j < n; j++) {
                    if (uniqueAdeq.get(i).size() == 2) {
                        if (Double.compare(ma.adeqAVG.get(j).get(i), uniqueAdeq.get(i).get(0)) == 0
                                || Double.compare(ma.adeqAVG.get(j).get(i), uniqueAdeq.get(i).get(1)) == 0) {
                            adeqEvaluation.get(i).set(j, adeqEvaluation.get(i).get(j) + 1);
                        }
                    } else {
                        if (Double.compare(ma.adeqAVG.get(j).get(i), uniqueAdeq.get(i).get(0)) == 0
                                || Double.compare(ma.adeqAVG.get(j).get(i), uniqueAdeq.get(i).get(1)) == 0
                                || Double.compare(ma.adeqAVG.get(j).get(i), uniqueAdeq.get(i).get(2)) == 0) {
                            adeqEvaluation.get(i).set(j, adeqEvaluation.get(i).get(j) + 1);
                        }
                    }

                }
            }
        }

        for (int i = 0; i < m; i++) {
            rangEvaluation.add(new ArrayList<>(tmpNull));
            if (uniqueRang.get(i).size() > 1) {
                for (int j = 0; j < n; j++) {
                    if (uniqueRang.get(i).size() == 2) {
                        if (Double.compare(ma.rang.get(j).get(i), uniqueRang.get(i).get(0)) == 0
                                || Double.compare(ma.rang.get(j).get(i), uniqueRang.get(i).get(1)) == 0) {
                            rangEvaluation.get(i).set(j, rangEvaluation.get(i).get(j) + 1);
                        }
                    } else {
                        if (Double.compare(ma.rang.get(j).get(i), uniqueRang.get(i).get(0)) == 0
                                || Double.compare(ma.rang.get(j).get(i), uniqueRang.get(i).get(1)) == 0
                                || Double.compare(ma.rang.get(j).get(i), uniqueRang.get(i).get(2)) == 0) {
                            rangEvaluation.get(i).set(j, rangEvaluation.get(i).get(j) + 1);
                        }
                    }

                }
            }
        }

        ArrayList<Double> avgAdeq = new ArrayList<>();
        ArrayList<Double> avgRang = new ArrayList<>();

        System.out.println();
        System.out.println();

        for (int i = 0; i < adeqEvaluation.get(0).size(); i++) {
            Double sumTmp = 0.0;
            for (int j = 0; j < adeqEvaluation.size(); j++) {
                System.out.printf("%-20s", adeqEvaluation.get(j).get(i));
                sumTmp += adeqEvaluation.get(j).get(i);
            }
            avgAdeq.add(sumTmp / counter);
            System.out.println();
        }

        System.out.println();
        System.out.println();

        for (int i = 0; i < rangEvaluation.get(0).size(); i++) {
            Double sumTmp = 0.0;
            for (int j = 0; j < rangEvaluation.size(); j++) {
                System.out.printf("%-20s", rangEvaluation.get(j).get(i));
                sumTmp += rangEvaluation.get(j).get(i);
            }
            System.out.println();
            avgRang.add(sumTmp / 41.0);
        }

        System.out.println();
        System.out.println();

        for (int i = 0; i < avgAdeq.size(); i++){
            System.out.println(avgAdeq.get(i));
        }

        System.out.println();
        System.out.println();

        for (int i = 0; i < avgRang.size(); i++){
            System.out.println(avgRang.get(i));
        }

        ArrayList<Integer> stars = new ArrayList<>();
        ArrayList<Integer> plus = new ArrayList<>();
        for (int i = 0; i < ma.adeqBest.size(); i++) {
            int countPlus = 0;
            int countStar = 0;
            for (int j = 0; j < ma.adeqBest.get(i).size(); j++) {
                if (ma.adeqBest.get(i).get(j).equals("+"))
                    countPlus++;
                if (ma.adeqBest.get(i).get(j).equals("*"))
                    countStar++;
            }
            stars.add(countStar);
            plus.add(countPlus);

            System.out.println("\"+\": " + countPlus + "; \"*\": " + countStar);
        }

    }


}
