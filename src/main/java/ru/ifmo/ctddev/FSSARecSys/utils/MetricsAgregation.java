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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Created by sergey on 06.12.15.
 */
public class MetricsAgregation {
    private String result = "result.xlsx";
    public HSSFWorkbook myWorkBook;

    public List<ArrayList<Double>> metrics;
    public List<ArrayList<Double>> asessors;
    public List<Double> avgAs;

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
                if (asessors.get(i).get(j) > 0)
                    count++;
            }
            if (count < 2)
                avgAs.add(-1.0);
            else {
                if (count == 2)
                    avgAs.add(0.0);
                else
                    avgAs.add(1.0);
            }
        }

        ArrayList<Double> avgSorted = new ArrayList<>(avgAs);
        Collections.sort(avgSorted);

        ArrayList<Double> avgSortedRev = new ArrayList<>(avgSorted);
        Collections.reverse(avgSortedRev);
    }

    public static void main(String [] args) throws IOException {
        File myFile = new File("results.xls");
        FileInputStream fis = new FileInputStream(myFile);

        MetricsAgregation ma = new MetricsAgregation();
        ma.myWorkBook = new HSSFWorkbook(fis);

        ma.sheetProcessing(0);
    }


}
