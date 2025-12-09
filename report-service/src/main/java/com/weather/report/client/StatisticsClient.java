package com.weather.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Feign Client do Statistics Service
 * Spring Cloud + Eureka automatycznie znajdzie serwis!
 */
@FeignClient(name = "statistics-service")
public interface StatisticsClient {

    @GetMapping("/api/statistics/{city}")
    Map<String, Object> getStatistics(@PathVariable("city") String city);
}

