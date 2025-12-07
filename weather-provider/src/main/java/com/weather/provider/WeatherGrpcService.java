package com.weather.provider;

import com.weather.provider.grpc.WeatherRequest;
import com.weather.provider.grpc.WeatherResponse;
import com.weather.provider.grpc.WeatherServiceGrpc;
import com.weather.provider.model.WeatherAlert;
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

    @Override
    public void getWeather(WeatherRequest request, StreamObserver<WeatherResponse> responseObserver) {
        System.out.println("Received weather request for city: " + request.getCity());

        // Generuj losową pogodę (15-45°C)
        double temperature = 15.0 + (Math.random() * 30);
        String description;
        String status = "OK";

        if (temperature > 35) {
            description = "Very Hot";
        } else if (temperature > 25) {
            description = "Warm";
        } else if (temperature > 15) {
            description = "Mild";
        } else {
            description = "Cool";
        }

        // SPRAWDŹ ALERTY Z BAZY DANYCH
        try {
            List<WeatherAlert> activeAlerts = alertService.getActive();

            for (WeatherAlert alert : activeAlerts) {
                // Sprawdź tylko alerty typu TEMPERATURE
                if ("TEMPERATURE".equals(alert.getAlertType())) {
                    if (temperature > alert.getThreshold()) {
                        status = "ALERT";

                        // Wyślij do RabbitMQ
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

                        break; // Wyślij tylko pierwszy pasujący alert
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking alerts: " + e.getMessage());
        }

        // Zbuduj odpowiedź gRPC
        WeatherResponse response = WeatherResponse.newBuilder()
                .setCity(request.getCity())
                .setTemperature(temperature)
                .setDescription(description)
                .setStatus(status)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        System.out.println(String.format("Sent weather response: %s, %.1f°C, %s",
                request.getCity(), temperature, status));
    }
}