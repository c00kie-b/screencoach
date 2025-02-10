package com.android.achievix;

public class KalmanFilter {
    private double Q; // process noise covariance
    private double R; // measurement noise covariance
    private double X; // estimated value
    private double P; // estimation error covariance
    private double K; // Kalman gain


    public KalmanFilter(double initValue, double processNoise, double measurementNoise, double estimationError) {
        this.Q = processNoise;
        this.R = measurementNoise;
        this.X = initValue;
        this.P = estimationError;
    }


    public double update(double measurement) {
        // Prediction update
        P = P + Q;


        // Measurement update
        K = P / (P + R);
        X = X + K * (measurement - X);
        P = (1 - K) * P;


        return X;
    }
}
