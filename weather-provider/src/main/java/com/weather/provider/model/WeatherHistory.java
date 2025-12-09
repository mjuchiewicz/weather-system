package com.weather.provider.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_history")
public class WeatherHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private Double temperature;

    @Column
    private String description;

    @Column
    private Integer humidity;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Constructor pusty (JPA)
    public WeatherHistory() {
        this.timestamp = LocalDateTime.now();
    }

    // Constructor z parametrami
    public WeatherHistory(String city, Double temperature, String description, Integer humidity) {
        this.city = city;
        this.temperature = temperature;
        this.description = description;
        this.humidity = humidity;
        this.timestamp = LocalDateTime.now();
    }

    // GETTERS
    public Long getId() { return id; }
    public String getCity() { return city; }
    public Double getTemperature() { return temperature; }
    public String getDescription() { return description; }
    public Integer getHumidity() { return humidity; }
    public LocalDateTime getTimestamp() { return timestamp; }

    // SETTERS
    public void setId(Long id) { this.id = id; }
    public void setCity(String city) { this.city = city; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public void setDescription(String description) { this.description = description; }
    public void setHumidity(Integer humidity) { this.humidity = humidity; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}