package com.weather.provider;

import com.weather.provider.api.OpenWeatherMapService;
import com.weather.provider.grpc.WeatherRequest;
import com.weather.provider.grpc.WeatherResponse;
import com.weather.provider.grpc.WeatherServiceGrpc;
import com.weather.provider.model.WeatherAlert;
import com.weather.provider.model.WeatherHistory;
import com.weather.provider.repository.WeatherHistoryRepository;
import com.weather.provider.service.WeatherAlertService;
import io.grpc.stub.StreamObserver;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WeatherGrpcService extends WeatherServiceGrpc.WeatherServiceImplBase {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${weather.alert.queue}")
    private String queueName;

    @Autowired
    private WeatherAlertService alertService;

    @Autowired
    private WeatherHistoryRepository historyRepository;

    @Autowired
    private OpenWeatherMapService weatherApiService;

    @Override
    public void getWeather(WeatherRequest request, StreamObserver<WeatherResponse> responseObserver) {
        System.out.println("Received weather request for city: " + request.getCity());

        // CALL REAL WEATHER API
        OpenWeatherMapService.WeatherData apiData = weatherApiService.getWeather(request.getCity());

        double temperature = apiData.getTemperature();
        String description = apiData.getDescription();
        String status = "OK";

        // AUTO-SAVE do historii
        try {
            WeatherHistory history = new WeatherHistory(
                    request.getCity(),
                    temperature,
                    description,
                    apiData.getHumidity()
            );
            historyRepository.save(history);
            System.out.println("Saved to history: " + request.getCity() + ", " + temperature + "°C");
        } catch (Exception e) {
            System.err.println("Failed to save history: " + e.getMessage());
        }

        // SPRAWDŹ ALERTY Z BAZY DANYCH
        try {
            List<WeatherAlert> activeAlerts = alertService.getActive();

            for (WeatherAlert alert : activeAlerts) {
                if ("TEMPERATURE".equals(alert.getAlertType())) {
                    boolean triggered = checkThreshold(temperature, alert.getOperator(), alert.getThreshold());

                    if (triggered) {
                        status = "ALERT";

                        String alertMessage = String.format(
                                "🚨 %s | %s: %.1f°C (threshold: %.1f°C) - %s",
                                alert.getSeverity(),
                                request.getCity(),
                                temperature,
                                alert.getThreshold(),
                                alert.getMessage()
                        );

                        rabbitTemplate.convertAndSend(queueName, alertMessage);
                        System.out.println("Alert triggered and sent to RabbitMQ: " + alertMessage);

                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking alerts: " + e.getMessage());
        }

        // odpowiedź gRPC
        WeatherResponse response = WeatherResponse.newBuilder()
                .setCity(request.getCity())
                .setTemperature(temperature)
                .setDescription(description)
                .setStatus(status)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        System.out.println(String.format("Sent weather response: %s, %.1f°C, %s (REAL DATA!)",
                request.getCity(), temperature, status));
    }

    private boolean checkThreshold(double value, String operator, double threshold) {
        switch (operator) {
            case ">":
                return value > threshold;
            case "<":
                return value < threshold;
            case ">=":
                return value >= threshold;
            case "<=":
                return value <= threshold;
            default:
                return value > threshold;
        }
    }

    // ============================================
// ZAMIEŃ CAŁĄ METODĘ getWeatherStream w WeatherGrpcService.java
// ============================================

    @Override
    public void getWeatherStream(com.weather.provider.grpc.WeatherStreamRequest request,
                                 StreamObserver<WeatherResponse> responseObserver) {

        String city = request.getCity();
        int updates = request.getUpdates() > 0 ? request.getUpdates() : 10;  // Default 10
        int intervalSeconds = request.getIntervalSeconds() > 0 ? request.getIntervalSeconds() : 5;  // Default 5s

        System.out.println("📡 Starting weather stream for " + city);
        System.out.println("   Updates: " + updates + ", Interval: " + intervalSeconds + "s");

        try {
            for (int i = 1; i <= updates; i++) {
                System.out.println("   Sending update #" + i + "...");

                // Fetch REAL weather from OpenWeatherMap!
                OpenWeatherMapService.WeatherData currentWeather = weatherApiService.getWeather(city);

                // AUTO-SAVE to history database (same as getWeather method)
                try {
                    WeatherHistory history = new WeatherHistory(
                            city,
                            currentWeather.getTemperature(),
                            currentWeather.getDescription(),
                            currentWeather.getHumidity()
                    );
                    historyRepository.save(history);
                    System.out.println("   💾 Saved to history: " + city + ", " + currentWeather.getTemperature() + "°C");
                } catch (Exception e) {
                    System.err.println("   ⚠️ Failed to save history: " + e.getMessage());
                }

                // Build response
                WeatherResponse response = WeatherResponse.newBuilder()
                        .setCity(city)
                        .setTemperature(currentWeather.getTemperature())
                        .setDescription(currentWeather.getDescription())
                        .setStatus("STREAMING")
                        .setTimestamp(System.currentTimeMillis())
                        .setUpdateNumber(i)
                        .build();

                // Send to client via stream!
                responseObserver.onNext(response);

                // Wait before next update (except for last one)
                if (i < updates) {
                    Thread.sleep(intervalSeconds * 1000);
                }
            }

            // Stream completed!
            System.out.println("✅ Stream completed! Sent " + updates + " updates");
            responseObserver.onCompleted();

        } catch (InterruptedException e) {
            System.err.println("❌ Stream interrupted: " + e.getMessage());
            responseObserver.onError(e);
        } catch (Exception e) {
            System.err.println("❌ Stream error: " + e.getMessage());
            e.printStackTrace();
            responseObserver.onError(e);
        }
    }
}