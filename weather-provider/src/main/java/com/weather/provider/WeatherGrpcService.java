package com.weather.provider;

import com.weather.provider.grpc.WeatherRequest;
import com.weather.provider.grpc.WeatherResponse;
import com.weather.provider.grpc.WeatherServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class WeatherGrpcService extends WeatherServiceGrpc.WeatherServiceImplBase {

    @Override
    public void getWeather(WeatherRequest request, StreamObserver<WeatherResponse> responseObserver) {
        System.out.println("Received weather request for city: " + request.getCity());

        // Fake dane pogodowe
        WeatherResponse response = WeatherResponse.newBuilder()
                .setCity(request.getCity())
                .setTemperature(22.5)
                .setDescription("Sunny")
                .setStatus("OK")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        System.out.println("Sent weather response for: " + request.getCity());
    }
}