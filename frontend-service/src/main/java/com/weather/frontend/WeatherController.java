package com.weather.frontend;

import com.weather.frontend.grpc.WeatherResponse;
import com.weather.frontend.model.WeatherHistory;
import com.weather.frontend.service.WeatherHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "*")
public class WeatherController {
    @Autowired
    private WeatherHistoryService weatherHistoryService;

    @Autowired
    private WeatherGrpcClient grpcClient;

    @GetMapping("/{city}")
    public ResponseEntity<EntityModel<WeatherData>> getWeather(@PathVariable String city) {

        // Wywołanie gRPC
        WeatherResponse grpcResponse = grpcClient.getWeather(city);

        // Konwersja na model
        WeatherData data = new WeatherData(
                grpcResponse.getCity(),
                grpcResponse.getTemperature(),
                grpcResponse.getDescription(),
                grpcResponse.getStatus()
        );

        // 🎁 BONUS: Auto-save do historii
        try {
            WeatherHistory history = new WeatherHistory(
                    data.getCity(),
                    data.getTemperature(),
                    data.getDescription(),
                    data.getStatus()
            );
            weatherHistoryService.create(history);
        } catch (Exception e) {
            // Ignoruj błędy zapisu (nie psuj głównego flow)
        }

        // HATEOAS - linki
        EntityModel<WeatherData> model = EntityModel.of(data);
        model.add(linkTo(methodOn(WeatherController.class).getWeather(city)).withSelfRel());
        model.add(linkTo(methodOn(WeatherHistoryController.class)
                .getWeatherByCity(city)).withRel("history")); // Link do historii!

        return ResponseEntity.ok(model);
    }

    // Klasa pomocnicza dla danych
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
}