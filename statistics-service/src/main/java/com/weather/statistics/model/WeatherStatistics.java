package com.weather.statistics.model;

import java.util.List;

public class WeatherStatistics {
    private String city;
    private int recordCount;
    private double average;
    private double min;
    private double max;
    private double median;
    private double standardDeviation;
    private String trend; // "RISING", "FALLING", "STABLE"
    private List<Double> outliers; // Anomalie (wartości odstające)

    // Constructor
    public WeatherStatistics() {}

    public WeatherStatistics(String city, int recordCount, double average, double min, double max,
                            double median, double standardDeviation, String trend, List<Double> outliers) {
        this.city = city;
        this.recordCount = recordCount;
        this.average = average;
        this.min = min;
        this.max = max;
        this.median = median;
        this.standardDeviation = standardDeviation;
        this.trend = trend;
        this.outliers = outliers;
    }

    // Getters and Setters
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public int getRecordCount() { return recordCount; }
    public void setRecordCount(int recordCount) { this.recordCount = recordCount; }

    public double getAverage() { return average; }
    public void setAverage(double average) { this.average = average; }

    public double getMin() { return min; }
    public void setMin(double min) { this.min = min; }

    public double getMax() { return max; }
    public void setMax(double max) { this.max = max; }

    public double getMedian() { return median; }
    public void setMedian(double median) { this.median = median; }

    public double getStandardDeviation() { return standardDeviation; }
    public void setStandardDeviation(double standardDeviation) { this.standardDeviation = standardDeviation; }

    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }

    public List<Double> getOutliers() { return outliers; }
    public void setOutliers(List<Double> outliers) { this.outliers = outliers; }
}

