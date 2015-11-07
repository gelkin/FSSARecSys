package ru.ifmo.ctddev.FSSARecSys.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergey on 07.11.15.
 */
public class MakePDFs {

    public static String datasetName;

    public MakePDFs() {

    }

    public void setDataset(String datasetName) {
        this.datasetName = datasetName;
    }



    public void generateText() throws IOException {
        String first = "%% LyX 2.1.4 created this file.  For more info, see http://www.lyx.org/.\n" +
                "%% Do not edit unless you really know what you are doing.\n" +
                "\\documentclass[20pt,english]{extarticle}\n" +
                "\\usepackage[T1]{fontenc}\n" +
                "\\usepackage[latin9]{inputenc}\n" +
                "\\usepackage[landscape,paperwidth=1000mm,paperheight=700mm]{geometry}\n" +
                "\\geometry{verbose,tmargin=2cm,bmargin=2cm,lmargin=2cm,rmargin=2cm}\n" +
                "\\usepackage{graphicx}\n" +
                "\n" +
                "\\makeatletter\n" +
                "\n" +
                "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LyX specific LaTeX commands.\n" +
                "%% Because html converters don't know tabularnewline\n" +
                "\\providecommand{\\tabularnewline}{\\\\}\n" +
                "%% A simple dot to overcome graphicx limitations\n" +
                "\\newcommand{\\lyxdot}{.}\n" +
                "\n" +
                "\n" +
                "\\makeatother\n" +
                "\n" +
                "\\usepackage{babel}\n" +
                "\\begin{document}\n\n";


        CharSequence c1 = "_";
        CharSequence c2 = "\\_";

        String dcopy = datasetName;
        dcopy.replace(c1, c2);

        String section = "\\section*{" + dcopy + ".arff}\n\n";

        String table = "\\begin{tabular}{|c|c|c|c|c|}\n" +
                "\\hline \n" +
                "\\includegraphics[clip,scale=1.5]{/home/sergey/masters/FSSARecSys/final/ "+ datasetName +"\\lyxdot sample} & \\includegraphics{/home/sergey/masters/FSSARecSys/final/" + datasetName  + "\\lyxdot arff__DBSCAN} & \\includegraphics{/home/sergey/masters/FSSARecSys/final/" + datasetName + "\\lyxdot arff__EM-1} & \\includegraphics{/home/sergey/masters/FSSARecSys/final/" + datasetName + "\\lyxdot arff__EM-2} & \\includegraphics[bb=0bp 0bp 500bp 500bp]{masters/FSSARecSys/final/" + datasetName + "\\lyxdot arff__EM-3}\\tabularnewline\n" +
                "\\hline \n" +
                "\\includegraphics{/home/sergey/masters/FSSARecSys/final/" + datasetName + "\\lyxdot arff__FarthestFirst-1} & \\includegraphics{/home/sergey/masters/FSSARecSys/final/" + datasetName + "\\lyxdot arff__FarthestFirst-2} & \\includegraphics{/home/sergey/masters/FSSARecSys/final/"+ datasetName +"\\lyxdot arff__FarthestFirst-3} & \\includegraphics{/home/sergey/masters/FSSARecSys/final/" + datasetName + "\\lyxdot arff__Hieracical} & \\includegraphics{masters/FSSARecSys/final/" + datasetName + "\\lyxdot arff__K-means-1}\\tabularnewline\n" +
                "\\hline \n" +
                "\\includegraphics{/home/sergey/masters/FSSARecSys/final/" + datasetName + "\\lyxdot arff__K-means-2} & \\includegraphics{/home/sergey/masters/FSSARecSys/final/" + datasetName + "\\lyxdot arff__K-means-3} &\\includegraphics{/home/sergey/masters/FSSARecSys/final/" + datasetName + "\\lyxdot arff__X-Means-1} & \\includegraphics{/home/sergey/masters/FSSARecSys/final/" + datasetName +"\\lyxdot arff__X-Means-2} & \\includegraphics{masters/FSSARecSys/final/" + datasetName + "\\lyxdot arff__X-Means-3} \\tabularnewline\n" +
                "\\hline \n" +
                "\\end{tabular}\n" +
                "\\end{document}";

        File f = new File("/home/sergey/masters/FSSARecSys/pdfs/" + datasetName + ".tex");

        FileOutputStream fos = new FileOutputStream(f);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        bw.write(first);
        bw.write(section);
        bw.write(table);
        bw.close();
        fos.close();

    }



    public static void main(String [] args) throws Exception {
        // list_of_datasets

        List<String> listOfFiles = new ArrayList<>();

        File fl = new File("list_of_datasets.txt");
        FileInputStream fis = new FileInputStream(fl);

        //Construct BufferedReader from InputStreamReader
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        String line = null;
        while ((line = br.readLine()) != null) {
            listOfFiles.add(line);
        }

        br.close();
        fis.close();

        MakePDFs mp = new MakePDFs();

        for (String s: listOfFiles) {
            mp.setDataset(s);
            mp.generateText();
        }

    }
}
