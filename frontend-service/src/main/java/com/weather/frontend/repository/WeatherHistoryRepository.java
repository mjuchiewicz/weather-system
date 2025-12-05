package com.weather.frontend.repository;

import com.weather.frontend.model.WeatherHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeatherHistoryRepository extends JpaRepository<WeatherHistory, Long> {

    // Znajdź wszystkie wpisy dla danego miasta
    List<WeatherHistory> findByCity(String city);

    // Znajdź z paginacją
    Page<WeatherHistory> findByCity(String city, Pageable pageable);

    // Znajdź gorące dni (temperatura > podana wartość)
    @Query("SELECT w FROM WeatherHistory w WHERE w.temperature > :temp")
    List<WeatherHistory> findHotDays(Double temp);

    // Znajdź alerty
    List<WeatherHistory> findByStatus(String status);
}