package com.weather.frontend;

import com.weather.frontend.grpc.WeatherResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "*")
public class WeatherController {

    @Autowired
    private WeatherGrpcClient grpcClient;

    @GetMapping("/{city}")
    public ResponseEntity<EntityModel<WeatherData>> getWeather(@PathVariable String city) {

        WeatherResponse grpcResponse = grpcClient.getWeather(city);

        WeatherData data = new WeatherData(
                grpcResponse.getCity(),
                grpcResponse.getTemperature(),
                grpcResponse.getDescription(),
                grpcResponse.getStatus()
        );

        // HATEOAS
        EntityModel<WeatherData> model = EntityModel.of(data);
        model.add(linkTo(methodOn(WeatherController.class).getWeather(city)).withSelfRel());

        return ResponseEntity.ok(model);
    }

    // ========== ASYNC ENDPOINT ==========

    @GetMapping("/async/{city}")
    public ResponseEntity<Map<String, String>> getWeatherAsync(@PathVariable String city) {

        System.out.println("Controller: Starting async call for " + city);
        long startTime = System.currentTimeMillis();

        grpcClient.getWeatherAsync(city, new WeatherGrpcClient.WeatherResponseCallback() {
            @Override
            public void onSuccess(WeatherResponse response) {
                System.out.println("Controller: Async success - " + response.getCity() +
                        ": " + response.getTemperature() + "°C");
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Controller: Async error - " + t.getMessage());
            }
        });

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("Controller: Returned immediately in " + elapsed + "ms (non-blocking!)");

        Map<String, String> response = new HashMap<>();
        response.put("message", "Async call initiated for " + city);
        response.put("info", "Check server logs for result from StreamObserver");
        response.put("responseTime", elapsed + "ms");

        return ResponseEntity.ok(response);
    }

    // ========== SSE STREAMING ENDPOINT (REAL-TIME!) ==========

    @GetMapping(value = "/stream-sse/{city}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamWeatherSSE(
            @PathVariable String city,
            @RequestParam(defaultValue = "10") int updates,
            @RequestParam(defaultValue = "5") int intervalSeconds) {

        System.out.println("🌊 SSE: Starting real-time weather stream for: " + city);

        SseEmitter emitter = new SseEmitter(120000L); // 2 minutes timeout

        // Start streaming in a separate thread
        new Thread(() -> {
            try {
                grpcClient.getWeatherStream(city, updates, intervalSeconds,
                        new WeatherGrpcClient.WeatherStreamCallback() {
                            @Override
                            public void onUpdate(WeatherResponse response) {
                                try {
                                    Map<String, Object> update = new HashMap<>();
                                    update.put("city", response.getCity());
                                    update.put("temperature", response.getTemperature());
                                    update.put("description", response.getDescription());
                                    update.put("timestamp", new Date(response.getTimestamp()).toString());
                                    update.put("updateNumber", response.getUpdateNumber());

                                    // Send update immediately!
                                    emitter.send(SseEmitter.event()
                                            .name("weather-update")
                                            .data(update));

                                    System.out.println("📤 SSE: Sent update #" + response.getUpdateNumber());

                                } catch (Exception e) {
                                    System.err.println("❌ SSE: Error sending update: " + e.getMessage());
                                    emitter.completeWithError(e);
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                System.err.println("❌ SSE: Stream error: " + t.getMessage());
                                emitter.completeWithError(t);
                            }

                            @Override
                            public void onCompleted() {
                                System.out.println("✅ SSE: Stream completed!");
                                emitter.complete();
                            }
                        });

            } catch (Exception e) {
                System.err.println("❌ SSE: Error: " + e.getMessage());
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    public static class WeatherData {
        private String city;
        private double temperature;
        private String description;
        private String status;

        public WeatherData(String city, double temperature, String description, String status) {
            this.city = city;
            this.temperature = temperature;
            this.description = description;
            this.status = status;
        }

        // Gettery
        public String getCity() { return city; }
        public double getTemperature() { return temperature; }
        public String getDescription() { return description; }
        public String getStatus() { return status; }
    }

    @GetMapping("/stream/{city}")
    public ResponseEntity<?> streamWeather(
            @PathVariable String city,
            @RequestParam(defaultValue = "10") int updates,
            @RequestParam(defaultValue = "5") int intervalSeconds) {

        System.out.println("🌊 Starting weather stream: " + city);

        List<Map<String, Object>> streamResults = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        try {
            grpcClient.getWeatherStream(city, updates, intervalSeconds,
                new WeatherGrpcClient.WeatherStreamCallback() {
                    @Override
                    public void onUpdate(WeatherResponse response) {
                        System.out.println("   📥 Received update #" + response.getUpdateNumber() +
                                ": " + response.getTemperature() + "°C");

                        // Store each update
                        Map<String, Object> update = new HashMap<>();
                        update.put("city", response.getCity());
                        update.put("temperature", response.getTemperature());
                        update.put("description", response.getDescription());
                        update.put("timestamp", new Date(response.getTimestamp()).toString());
                        update.put("updateNumber", response.getUpdateNumber());

                        streamResults.add(update);
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.err.println("   ❌ Stream error: " + t.getMessage());
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("   ✅ Stream completed!");
                        latch.countDown();
                    }
                });

            // Wait for stream to complete (max 2 minutes)
            latch.await(2, TimeUnit.MINUTES);

            Map<String, Object> result = new HashMap<>();
            result.put("city", city);
            result.put("totalUpdates", streamResults.size());
            result.put("updates", streamResults);
            result.put("message", "Stream completed successfully");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}