package ru.ifmo.ctddev.FSSARecSys.utils;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

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

    public void setMyWorkBook(FileInputStream myWorkBook) throws IOException {
        this.myWorkBook = new HSSFWorkbook(myWorkBook);
    }

    public void sheetProcessing(int sheetNum) {
        HSSFSheet currentSheet = myWorkBook.getSheetAt(sheetNum);
        metrics = new ArrayList<>();
        asessors = new ArrayList<>();

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
        System.out.print("LOOL");

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

        ArrayList<Integer> avgSorted = new ArrayList<>(avgAs);
        Collections.sort(avgSorted);

        ArrayList<Integer> avgSortedRev = new ArrayList<>(avgSorted);
        Collections.reverse(avgSortedRev);

        ArrayList<Pair<Double, Integer>> permutation = new ArrayList<>();
        for (int i = 0; i < metrics.size(); i++) {
            for (int j = 0; j < metrics.get(i).size(); j++) {
                permutation.add(new Pair<Double, Integer>(metrics.get(i).get(j), avgAs.get(j)));
            }
            Collections.sort(permutation, new Comparator<Pair<Double, Integer>>() {
                @Override
                public int compare(Pair<Double, Integer> o1, Pair<Double, Integer> o2) {
                    if ((Double) o1.first < (Double) o2.first)
                        return 1;
                    if ((Double) o1.first == (Double) o2.first) {
                        if ((Integer) o1.second < (Integer) o2.second)
                            return 1;
                        if ((Integer) o1.second == (Integer) o2.second)
                            return 0;
                        else return -1;
                    } else {
                        return -1;
                    }
                }
            });

            if (!metricsWithBestMin.contains(i)) {
                Collections.reverse(permutation);
            }

            for (int j = 0; j < permutation.size(); j++) {
                avgAs.set(j, permutation.get(j).second);
            }

            for (int j = 0; j < permutation.size(); j++) {

            }

        }


    }

    public static void main(String [] args) throws IOException {
        File myFile = new File("results.xls");
        FileInputStream fis = new FileInputStream(myFile);

        MetricsAgregation ma = new MetricsAgregation();
        ma.myWorkBook = new HSSFWorkbook(fis);
        Collections.addAll(metricsWithBestMin, 1, 5, 9, 10, 12, 13);

        ma.sheetProcessing(0);
    }


}
