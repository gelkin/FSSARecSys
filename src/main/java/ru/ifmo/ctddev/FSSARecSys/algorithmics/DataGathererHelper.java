package ru.ifmo.ctddev.FSSARecSys.algorithmics;
import ru.ifmo.ctddev.FSSARecSys.db.internal.Dataset;
import ru.ifmo.ctddev.FSSARecSys.db.internal.FSSAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.db.manager.QueryManager;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Kirill on 13.01.2016.
 */
public  class DataGathererHelper {

    private static QueryManager manager = new QueryManager("jdbc:mysql://localhost/fss", "newuser", "Sudarikov94");

    private static final String[] dataSetNames = {"simple",
            "center",
            "stripes",
            "perfect_overlap",
            "perfect_overlap2",
            "overlap",
            "linked",
            "folded",
            "end_overlap",
            "double_spiral",
            "curved_stripes",
            "curved_stripes_adv",
            "specified",
            "half-kernel",
            "corners",
            "t_corners",
            "angled_stripes",
            "x",
            "diag",
            "eye",
            "rounded_stripes",
            "i-dot",
            "i-dot_noisy",
            "folded_noisy",
            "curved_stripes_noisy",
            "triple_folded",
            "x_noisy",
            "eye_noisy",
            "ovals",
            "circle_in_star2",
            "angled_stripes_light_noisy",
            "half-kernel_noisy",
            "specified_noisy",
            "stripes_noisy",
            "t_corners_noisy",
            "double_spiral_noisy",
            "corners_noisy",
            "center_noisy",
            "simple_noisy",
            "overlap_noisy",
    };


    public static ArrayList<FSSAlgorithm> GetRandomlyTossedAlgorithms() // throws Exception
    {

        try {
            ArrayList<FSSAlgorithm> algorithms = manager.getAvailableFssAlgorithms();

            Collections.shuffle(algorithms);
        return algorithms;
        }
        catch (Exception ex)
        {
            System.out.println("Something went wrong while gatherings algorithms..."+ex.toString());
            return null;
        }
    }

    public static class Filter {

        public File[] finder( String dirName){
            File dir = new File(dirName);

            return dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String filename)
                { return filename.endsWith(".arff"); }
            } );

        }

    }


    public static ArrayList<Dataset> GetDataSets()
    {

        ArrayList<Dataset> dataSets = new ArrayList<>();

//        for (String st : dataSetNames
//                ) {
//            try
//
//            {
//                Dataset newDataset = new Dataset(st,new File( System.getProperty("user.dir")+st+".arff"),"classification");
//                manager.addDataset(newDataset);
//
//                dataSets.add(manager.getDataset(st));
//                Thread.sleep(3000);
//            }
//            catch (Exception ex)
//            {
//
//                System.out.println("Something went wrong while gatherings datasets..."+ex.getMessage());
//            }
//            }

        File folder = new File(System.getProperty("user.dir"));
        File[] listOfFiles = folder.listFiles();
        Filter filter = new Filter();
        listOfFiles =
        filter.finder(folder.getAbsolutePath());
        System.out.println("Working directory "+folder.getAbsolutePath());
        System.out.println("arff files found: "+listOfFiles.length);
        for (File file : listOfFiles) {
            if (file.isFile()) {
                if (file.getName().contains(".arff"))
                {
                    Dataset newDataset = new Dataset(file.getName().substring(0, file.getName().lastIndexOf('.')),file,"classification");
                    try {
                        manager.addDataset(newDataset);
                        dataSets.add(newDataset);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }
        }

return dataSets;
    }


    public static void ClearSchema() throws Exception {
        manager.ClearSchema();
    }

}
