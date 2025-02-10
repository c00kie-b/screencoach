package com.android.achievix.Fragment;

import java.util.List;

/**
 * Represents the scaling parameters used in machine learning models.
 * This class is designed to deserialize JSON data that contains mean and scale arrays.
 */
public class ScalerParams {
    private List<Double> mean;
    private List<Double> scale;

    /**
     * Gets the list of mean values.
     * @return A list of doubles representing the mean values.
     */
    public List<Double> getMean() {
        return mean;
    }

    /**
     * Sets the list of mean values.
     * @param mean A list of doubles representing the mean values.
     */
    public void setMean(List<Double> mean) {
        this.mean = mean;
    }

    /**
     * Gets the list of scale values.
     * @return A list of doubles representing the scale values.
     */
    public List<Double> getScale() {
        return scale;
    }

    /**
     * Sets the list of scale values.
     * @param scale A list of doubles representing the scale values.
     */
    public void setScale(List<Double> scale) {
        this.scale = scale;
    }
}
