package ru.ifmo.ctddev.FSSARecSys.algorithmics;

/**
 * Created by Kirill on 5.01.2016.
 */

    public interface BanditAlgorithm {

        public int selectArm();

        public void update(int arm, double reward);

        public void reset();
    }
