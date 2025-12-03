package com.weather.provider;

import com.weather.provider.grpc.WeatherRequest;
import com.weather.provider.grpc.WeatherResponse;
import com.weather.provider.grpc.WeatherServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WeatherGrpcService extends WeatherServiceGrpc.WeatherServiceImplBase {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${weather.alert.queue}")
    private String queueName;

    @Override
    public void getWeather(WeatherRequest request, StreamObserver<WeatherResponse> responseObserver) {
        System.out.println("Received weather request for city: " + request.getCity());

        // Fake dane pogodowe - możesz zmienić temperaturę dla testów
        double temperature = 35.0; // Zmień na np. 35.0 żeby wywołać alert!
        String description = "Very Hot";
        String status = "OK";

        // Jeśli temperatura > 30°C -> ALERT
        if (temperature > 30.0) {
            status = "ALERT";
            String alertMessage = String.format("ALERT! High temperature in %s: %.1f°C",
                    request.getCity(), temperature);

            // Wyślij do RabbitMQ
            rabbitTemplate.convertAndSend(queueName, alertMessage);
            System.out.println("Alert sent to RabbitMQ: " + alertMessage);
        }

        WeatherResponse response = WeatherResponse.newBuilder()
                .setCity(request.getCity())
                .setTemperature(temperature)
                .setDescription(description)
                .setStatus(status)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        System.out.println("Sent weather response for: " + request.getCity());
    }
}