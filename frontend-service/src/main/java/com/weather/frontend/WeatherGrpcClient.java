package com.weather.frontend;

import com.weather.frontend.grpc.WeatherRequest;
import com.weather.frontend.grpc.WeatherResponse;
import com.weather.frontend.grpc.WeatherServiceGrpc;
import com.weather.frontend.grpc.WeatherStreamRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Component
public class WeatherGrpcClient {

    @Value("${grpc.weather.host}")
    private String host;

    @Value("${grpc.weather.port}")
    private int port;

    private ManagedChannel channel;
    private WeatherServiceGrpc.WeatherServiceBlockingStub blockingStub;
    private WeatherServiceGrpc.WeatherServiceStub asyncStub;

    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        blockingStub = WeatherServiceGrpc.newBlockingStub(channel);
        asyncStub = WeatherServiceGrpc.newStub(channel); // NON-BLOCKING!

        System.out.println("gRPC client connected to " + host + ":" + port);
    }

    // BLOCKING (synchronous)
    public WeatherResponse getWeather(String city) {
        WeatherRequest request = WeatherRequest.newBuilder()
                .setCity(city)
                .build();
        return blockingStub.getWeather(request);
    }

    // NON-BLOCKING (asynchronous)
    public void getWeatherAsync(String city, WeatherResponseCallback callback) {
        WeatherRequest request = WeatherRequest.newBuilder()
                .setCity(city)
                .build();

        System.out.println("→ Calling getWeather ASYNC for: " + city);

        asyncStub.getWeather(request, new StreamObserver<WeatherResponse>() {
            @Override
            public void onNext(WeatherResponse response) {
                System.out.println("→ ASYNC response received: " + response.getCity() +
                        ", " + response.getTemperature() + "°C");
                callback.onSuccess(response);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("→ ASYNC call failed: " + t.getMessage());
                callback.onError(t);
            }

            @Override
            public void onCompleted() {
                System.out.println("→ ASYNC call completed");
            }
        });

        System.out.println("→ ASYNC call initiated (returned immediately, non-blocking!)");
    }

    // SERVER STREAMING - receive multiple updates from server
    public void getWeatherStream(String city, int updates, int intervalSeconds, WeatherStreamCallback callback) {
        WeatherStreamRequest request = WeatherStreamRequest.newBuilder()
                .setCity(city)
                .setUpdates(updates)
                .setIntervalSeconds(intervalSeconds)
                .build();

        System.out.println("🌊 Starting weather stream for: " + city);

        asyncStub.getWeatherStream(request, new StreamObserver<WeatherResponse>() {
            @Override
            public void onNext(WeatherResponse response) {
                System.out.println("📥 Stream update #" + response.getUpdateNumber() +
                        ": " + response.getTemperature() + "°C");
                callback.onUpdate(response);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("❌ Stream error: " + t.getMessage());
                callback.onError(t);
            }

            @Override
            public void onCompleted() {
                System.out.println("✅ Stream completed!");
                callback.onCompleted();
            }
        });
    }

    // Callback interface
    public interface WeatherResponseCallback {
        void onSuccess(WeatherResponse response);
        void onError(Throwable t);
    }

    // Streaming callback interface
    public interface WeatherStreamCallback {
        void onUpdate(WeatherResponse response);
        void onError(Throwable t);
        void onCompleted();
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.err.println("Channel shutdown interrupted");
                channel.shutdownNow();
            }
        }
    }
}