package ru.ifmo.ctddev.FSSARecSys.algorithmics;



/**
 * Created by Kirill on 26.12.2015.
 */
public class LearningBasedOnData implements Runnable {



    public static double function(double Qij, int t, int n)
    {
        return  -Qij+Math.sqrt(2 * Math.log(t) / n);
    }

    @Override
    public void run() {

    }
}
