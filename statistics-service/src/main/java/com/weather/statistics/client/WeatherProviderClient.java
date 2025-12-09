package com.weather.statistics.client;

import com.weather.statistics.model.WeatherHistory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign Client do komunikacji z Weather Provider
 * Spring Cloud automatycznie znajdzie serwis przez Eureka!
 */
@FeignClient(name = "weather-provider")
public interface WeatherProviderClient {

    @GetMapping("/api/history/{city}")
    List<WeatherHistory> getHistory(
            @PathVariable("city") String city,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    );

    @GetMapping("/api/history/{city}/stats")
    Object getBasicStats(@PathVariable("city") String city);
}

