package ru.ifmo.ctddev.FSSARecSys.algorithmics;

/**
 * Created by Kirill on 5.01.2016.
 */


//import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractBanditAlgorithm implements BanditAlgorithm {

    protected final int numArms;
    protected final int[] counts;
    protected final double[] values;

    public AbstractBanditAlgorithm(int numArms) {
        this.numArms = numArms;
        this.counts = new int[numArms];
        this.values = new double[numArms];
    }

    @Override
    public void update(int arm, double reward) {
        counts[arm]++;
      }

    @Override
    public void reset() {
        for (int i = 0; i < counts.length; i++) {
            counts[i] = 0;
            values[i] = 0;
        }
    }


    public abstract String toString();
}
