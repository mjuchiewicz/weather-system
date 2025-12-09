package com.weather;

import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Weather History Service - XML-RPC Service Implementation
 *
 * Zgodne z listą Cw0:
 * - Dwie metody: synchronous + asynchronous
 * - Async z Thread.sleep(3000)
 *
 * BONUS: Integracja z Weather Provider (prawdziwe dane z bazy H2)
 */
public class WeatherHistoryService {

    private static final int DELAY_SECONDS = 3; // Jak na liście!
    private static final String WEATHER_PROVIDER_URL =
            System.getenv().getOrDefault("WEATHER_PROVIDER_URL", "http://localhost:8084");

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * SYNCHRONOUS METHOD - jak echo() na liście
     * Zwraca od razu bez opóźnienia
     */
    public Map<String, Object>[] getHistory(String city, int days) {
        System.out.println("📥 XML-RPC SYNC request: getHistory(city=" + city + ", days=" + days + ")");

        try {
            // Dzwoń do Weather Provider - PRAWDZIWE DANE!
            String url = WEATHER_PROVIDER_URL + "/api/history/" + city + "?limit=" + days;
            System.out.println("   Calling Weather Provider: " + url);

            List<Map> historyData = restTemplate.getForObject(url, List.class);

            if (historyData != null && !historyData.isEmpty()) {
                System.out.println("✅ Retrieved " + historyData.size() + " real records from database");
                return historyData.toArray(new Map[0]);
            } else {
                System.out.println("⚠️  No data found for " + city);
                return createNoDataResponse(city, "No historical data. Please check weather first.");
            }

        } catch (Exception e) {
            System.err.println("❌ Error calling Weather Provider: " + e.getMessage());
            return createNoDataResponse(city, "Error: " + e.getMessage());
        }
    }

    /**
     * ASYNCHRONOUS METHOD - jak echoWithDelay() na liście
     * Z 3-sekundowym opóźnieniem (Thread.sleep)
     */
    public Map<String, Object>[] getHistoryWithDelay(String city, int days)
            throws InterruptedException {

        System.out.println("📥 XML-RPC ASYNC request: getHistoryWithDelay(city=" + city + ", days=" + days + ")");
        System.out.println("⏳ Processing... (waiting " + DELAY_SECONDS + " seconds)");

        // DELAY - ZGODNE Z LISTĄ!
        Thread.sleep(DELAY_SECONDS * 1000);

        System.out.println("✅ Delay completed, fetching data...");

        // Zwróć te same dane co sync (ale po delay)
        return getHistory(city, days);
    }

    /**
     * BONUS METHOD: Get statistics (AVG/MIN/MAX)
     * Pokazuje że można więcej!
     */
    public Map<String, Object> getStatistics(String city) {
        System.out.println("📥 XML-RPC request: getStatistics(city=" + city + ")");

        try {
            String url = WEATHER_PROVIDER_URL + "/api/history/" + city + "/stats";
            System.out.println("   Calling Weather Provider: " + url);

            Map<String, Object> stats = restTemplate.getForObject(url, Map.class);

            if (stats != null) {
                System.out.println("✅ Statistics: " + stats);
                return stats;
            } else {
                return createErrorMap("No statistics available for " + city);
            }

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            return createErrorMap("Error: " + e.getMessage());
        }
    }

    // Helper methods

    private Map<String, Object>[] createNoDataResponse(String city, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("city", city);
        response.put("error", message);
        response.put("timestamp", new Date().toString());
        return new Map[]{response};
    }

    private Map<String, Object> createErrorMap(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", new Date().toString());
        return error;
    }
}