package com.weather.statistics.service;

import com.weather.statistics.client.WeatherProviderClient;
import com.weather.statistics.model.WeatherHistory;
import com.weather.statistics.model.WeatherStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsCalculator {

    @Autowired
    private WeatherProviderClient weatherProviderClient;

    public WeatherStatistics calculateAdvancedStatistics(String city) {
        System.out.println("📊 Calculating advanced statistics for: " + city);

        // Pobierz historię przez Feign Client (Spring Cloud!)
        List<WeatherHistory> history = weatherProviderClient.getHistory(city, 100);

        if (history == null || history.isEmpty()) {
            throw new RuntimeException("No historical data available for " + city);
        }

        // Wyciągnij tylko temperatury
        List<Double> temperatures = history.stream()
                .map(WeatherHistory::getTemperature)
                .filter(temp -> temp != null)
                .collect(Collectors.toList());

        if (temperatures.isEmpty()) {
            throw new RuntimeException("No temperature data available");
        }

        // Oblicz statystyki
        int count = temperatures.size();
        double avg = calculateAverage(temperatures);
        double min = Collections.min(temperatures);
        double max = Collections.max(temperatures);
        double median = calculateMedian(temperatures);
        double stdDev = calculateStandardDeviation(temperatures, avg);
        String trend = calculateTrend(temperatures);
        List<Double> outliers = findOutliers(temperatures, avg, stdDev);

        System.out.println("✅ Statistics calculated:");
        System.out.println("   Records: " + count);
        System.out.println("   Average: " + String.format("%.2f", avg) + "°C");
        System.out.println("   Median: " + String.format("%.2f", median) + "°C");
        System.out.println("   Std Dev: " + String.format("%.2f", stdDev));
        System.out.println("   Trend: " + trend);
        System.out.println("   Outliers: " + outliers.size());

        return new WeatherStatistics(
                city, count, avg, min, max, median, stdDev, trend, outliers
        );
    }

    private double calculateAverage(List<Double> values) {
        return values.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private double calculateMedian(List<Double> values) {
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int size = sorted.size();

        if (size % 2 == 0) {
            return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
        } else {
            return sorted.get(size / 2);
        }
    }

    private double calculateStandardDeviation(List<Double> values, double mean) {
        double variance = values.stream()
                .mapToDouble(val -> Math.pow(val - mean, 2))
                .average()
                .orElse(0.0);

        return Math.sqrt(variance);
    }

    private String calculateTrend(List<Double> values) {
        if (values.size() < 2) {
            return "STABLE";
        }

        // Prosta regresja liniowa (trend)
        int n = values.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += values.get(i);
            sumXY += i * values.get(i);
            sumX2 += i * i;
        }

        // Współczynnik nachylenia (slope)
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);

        if (slope > 0.1) {
            return "RISING";
        } else if (slope < -0.1) {
            return "FALLING";
        } else {
            return "STABLE";
        }
    }

    private List<Double> findOutliers(List<Double> values, double mean, double stdDev) {
        // Outliers = wartości > 2 * odchylenie standardowe od średniej
        double threshold = 2 * stdDev;

        return values.stream()
                .filter(val -> Math.abs(val - mean) > threshold)
                .distinct()
                .collect(Collectors.toList());
    }
}

