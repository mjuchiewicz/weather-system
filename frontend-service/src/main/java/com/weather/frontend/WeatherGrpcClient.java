package com.weather.frontend;

import com.weather.frontend.grpc.WeatherRequest;
import com.weather.frontend.grpc.WeatherResponse;
import com.weather.frontend.grpc.WeatherServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class WeatherGrpcClient {

    @Value("${grpc.weather.host}")
    private String host;

    @Value("${grpc.weather.port}")
    private int port;

    private ManagedChannel channel;
    private WeatherServiceGrpc.WeatherServiceBlockingStub stub;

    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        stub = WeatherServiceGrpc.newBlockingStub(channel);
        System.out.println("gRPC client connected to " + host + ":" + port);
    }

    public WeatherResponse getWeather(String city) {
        WeatherRequest request = WeatherRequest.newBuilder()
                .setCity(city)
                .build();
        return stub.getWeather(request);
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null) {
            channel.shutdown();
        }
    }
}