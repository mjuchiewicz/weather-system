package com.weather.report;

import com.weather.report.client.StatisticsClient;
import com.weather.report.soap.GetWeatherReportRequest;
import com.weather.report.soap.GetWeatherReportResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Endpoint
public class WeatherReportEndpoint {

    private static final String NAMESPACE_URI = "http://weather.com/report";

    @Autowired
    private StatisticsClient statisticsClient;

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getWeatherReportRequest")
    @ResponsePayload
    public GetWeatherReportResponse getWeatherReport(@RequestPayload GetWeatherReportRequest request) {
        String city = request.getCity();
        int days = request.getDays();

        System.out.println("📊 SOAP Report request: city=" + city + ", days=" + days);
        System.out.println("🔗 Calling Statistics Service via Feign Client...");

        GetWeatherReportResponse response = new GetWeatherReportResponse();
        response.setCity(city);
        response.setReportDate(LocalDate.now().format(DateTimeFormatter.ISO_DATE));

        try {
            // NOWE: Użyj Statistics Service przez Feign Client (Spring Cloud!)
            Map<String, Object> advancedStats = statisticsClient.getStatistics(city);

            if (advancedStats != null && !advancedStats.containsKey("error")) {
                int count = ((Number) advancedStats.get("recordCount")).intValue();
                double avgTemp = ((Number) advancedStats.get("average")).doubleValue();
                double minTemp = ((Number) advancedStats.get("min")).doubleValue();
                double maxTemp = ((Number) advancedStats.get("max")).doubleValue();
                double median = ((Number) advancedStats.get("median")).doubleValue();
                double stdDev = ((Number) advancedStats.get("standardDeviation")).doubleValue();
                String trend = (String) advancedStats.get("trend");

                response.setAverageTemperature(avgTemp);
                response.setMinTemperature(minTemp);
                response.setMaxTemperature(maxTemp);

                // ULEPSZONE: Dodaj zaawansowane statystyki do podsumowania
                response.setSummary(String.format(
                        "Advanced Weather Report for %s based on %d measurements. " +
                                "Temperature: avg=%.1f°C (median=%.1f°C), range=[%.1f°C to %.1f°C], " +
                                "std.dev=%.2f, trend=%s. " +
                                "Statistics calculated using Feign Client + Eureka service discovery.",
                        city, count, avgTemp, median, minTemp, maxTemp, stdDev, trend
                ));

                System.out.println("✅ SOAP Report generated with ADVANCED statistics from Statistics Service!");
                System.out.println("   Records: " + count + ", Trend: " + trend + ", StdDev: " + String.format("%.2f", stdDev));

                return response;
            }

            // Fallback: Brak danych
            response.setAverageTemperature(0.0);
            response.setMinTemperature(0.0);
            response.setMaxTemperature(0.0);
            response.setSummary("No historical data available for " + city + ". Generate some weather data first!");

            System.out.println("⚠️ SOAP Report: No data available");

        } catch (Exception e) {
            System.err.println("❌ Failed to fetch statistics: " + e.getMessage());
            e.printStackTrace();

            response.setAverageTemperature(0.0);
            response.setMinTemperature(0.0);
            response.setMaxTemperature(0.0);
            response.setSummary("Error generating report: " + e.getMessage());
        }

        return response;
    }
}