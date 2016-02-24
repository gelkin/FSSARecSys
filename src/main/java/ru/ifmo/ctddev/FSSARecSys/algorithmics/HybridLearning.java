package ru.ifmo.ctddev.FSSARecSys.algorithmics;

/**
 * Created by Kirill on 26.12.2015.
 */
public class HybridLearning extends AbstractAlgorithm implements Runnable {




    public static double function(double Qi,
            double Qij, int t, int n)
    {

        return  Qi-Qij+Math.sqrt(2 * Math.log(t) / n);
    }

    public static double sumFunction(double algLearning, double dataLearning)
    {
        return algLearning+dataLearning;
    }


    @Override
    public void run() {

    }
}
