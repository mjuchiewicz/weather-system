package com.weather.report;

import com.weather.report.soap.GetWeatherReportRequest;
import com.weather.report.soap.GetWeatherReportResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Endpoint
public class WeatherReportEndpoint {

    private static final String NAMESPACE_URI = "http://weather.com/report";

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getWeatherReportRequest")
    @ResponsePayload
    public GetWeatherReportResponse getWeatherReport(@RequestPayload GetWeatherReportRequest request) {
        String city = request.getCity();
        int days = request.getDays();

        System.out.println("SOAP Report request: city=" + city + ", days=" + days);

        GetWeatherReportResponse response = new GetWeatherReportResponse();
        response.setCity(city);
        response.setReportDate(LocalDate.now().format(DateTimeFormatter.ISO_DATE));

        try {
            String weatherProviderUrl = System.getenv().getOrDefault("WEATHER_PROVIDER_URL", "http://localhost:8084");
            String url = weatherProviderUrl + "/api/history/" + city + "/stats";
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            java.util.Map<String, Object> stats = restTemplate.getForObject(url, java.util.Map.class);

            if (stats != null && stats.get("recordCount") != null) {
                long count = ((Number) stats.get("recordCount")).longValue();

                if (count > 0) {
                    double avgTemp = ((Number) stats.get("avgTemp")).doubleValue();
                    double minTemp = ((Number) stats.get("minTemp")).doubleValue();
                    double maxTemp = ((Number) stats.get("maxTemp")).doubleValue();

                    response.setAverageTemperature(avgTemp);
                    response.setMinTemperature(minTemp);
                    response.setMaxTemperature(maxTemp);
                    response.setSummary(String.format(
                            "Weather report for %s based on %d real measurements from OpenWeatherMap API. " +
                                    "Temperature ranges from %.1f°C to %.1f°C with average of %.1f°C.",
                            city, count, minTemp, maxTemp, avgTemp
                    ));

                    System.out.println("SOAP Report generated from " + count + " real records");

                    return response;
                }
            }

            response.setAverageTemperature(0.0);
            response.setMinTemperature(0.0);
            response.setMaxTemperature(0.0);
            response.setSummary("No historical data available for " + city + ". Please check weather first to generate history.");

            System.out.println("SOAP Report: No data available for " + city);

        } catch (Exception e) {
            System.err.println("Failed to fetch stats: " + e.getMessage());
            e.printStackTrace();

            response.setAverageTemperature(0.0);
            response.setMinTemperature(0.0);
            response.setMaxTemperature(0.0);
            response.setSummary("Error generating report: " + e.getMessage());
        }

        return response;
    }
}