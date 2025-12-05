package com.weather.frontend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_history")
public class WeatherHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "City name cannot be blank")
    @Size(min = 2, max = 100, message = "City name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String city;

    @NotNull(message = "Temperature cannot be null")
    @Min(value = -100, message = "Temperature must be above -100°C")
    @Max(value = 100, message = "Temperature must be below 100°C")
    @Column(nullable = false)
    private Double temperature;

    @NotBlank(message = "Description cannot be blank")
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Constructor - pusty (wymagany przez JPA)
    public WeatherHistory() {
        this.timestamp = LocalDateTime.now();
    }

    // Constructor z parametrami
    public WeatherHistory(String city, Double temperature, String description, String status) {
        this.city = city;
        this.temperature = temperature;
        this.description = description;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    // GETTERS
    public Long getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

    public Double getTemperature() {
        return temperature;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // SETTERS
    public void setId(Long id) {
        this.id = id;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}