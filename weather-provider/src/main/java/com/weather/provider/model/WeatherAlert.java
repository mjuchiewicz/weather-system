package com.weather.provider.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_alerts")
public class WeatherAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Alert type cannot be blank")
    @Column(nullable = false)
    private String alertType; // "TEMPERATURE", "HUMIDITY", "WIND"

    @NotNull(message = "Threshold cannot be null")
    @Column(nullable = false)
    private Double threshold; // np. 35.0

    @NotBlank(message = "Severity cannot be blank")
    @Column(nullable = false)
    private String severity; // "LOW", "MEDIUM", "HIGH", "EXTREME"

    @NotBlank(message = "Color cannot be blank")
    @Column(nullable = false)
    private String color; // "#FF5722"

    @NotBlank(message = "Message cannot be blank")
    @Column(nullable = false)
    private String message; // "Extreme heat warning!"

    @Column
    private String imageUrl; // "fire.png" (opcjonalne)

    @Column(nullable = false)
    private Boolean active; // true/false

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Constructor pusty (JPA)
    public WeatherAlert() {
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor z parametrami
    public WeatherAlert(String alertType, Double threshold, String severity,
                        String color, String message, String imageUrl) {
        this.alertType = alertType;
        this.threshold = threshold;
        this.severity = severity;
        this.color = color;
        this.message = message;
        this.imageUrl = imageUrl;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    // GETTERS
    public Long getId() { return id; }
    public String getAlertType() { return alertType; }
    public Double getThreshold() { return threshold; }
    public String getSeverity() { return severity; }
    public String getColor() { return color; }
    public String getMessage() { return message; }
    public String getImageUrl() { return imageUrl; }
    public Boolean getActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // SETTERS
    public void setId(Long id) { this.id = id; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    public void setThreshold(Double threshold) { this.threshold = threshold; }
    public void setSeverity(String severity) { this.severity = severity; }
    public void setColor(String color) { this.color = color; }
    public void setMessage(String message) { this.message = message; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setActive(Boolean active) { this.active = active; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}