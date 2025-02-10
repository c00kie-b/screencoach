package com.android.achievix.Fragment;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;


public class StandardScaler {
    private double[] means;
    private double[] stdDevs;

    public void fit(double[][] data) {
        int nFeatures = data[0].length;
        means = new double[nFeatures];
        stdDevs = new double[nFeatures];

        Mean mean = new Mean();
        StandardDeviation stdDev = new StandardDeviation();

        for (int i = 0; i < nFeatures; i++) {
            double[] feature = new double[data.length];
            for (int j = 0; j < data.length; j++) {
                feature[j] = data[j][i];
            }
            means[i] = mean.evaluate(feature);
            stdDevs[i] = stdDev.evaluate(feature);
        }
    }

    public double[] transform(double[] data) {
        double[] scaledData = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            scaledData[i] = (data[i] - means[i]) / stdDevs[i];
        }
        return scaledData;
    }

    public double[][] fitTransform(double[][] data) {
        fit(data);
        double[][] scaledData = new double[data.length][data[0].length];
        for (int i = 0; i < data.length; i++) {
            scaledData[i] = transform(data[i]);
        }
        return scaledData;
    }
}
