package com.weather.report;

import com.weather.report.soap.GetWeatherReportRequest;
import com.weather.report.soap.GetWeatherReportResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Endpoint
public class WeatherReportEndpoint {

    private static final String NAMESPACE_URI = "http://weather.com/report";

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getWeatherReportRequest")
    @ResponsePayload
    public GetWeatherReportResponse getWeatherReport(@RequestPayload GetWeatherReportRequest request) {
        System.out.println("SOAP Request received for city: " + request.getCity() + ", days: " + request.getDays());

        GetWeatherReportResponse response = new GetWeatherReportResponse();
        response.setCity(request.getCity());
        response.setReportDate(LocalDate.now().format(DateTimeFormatter.ISO_DATE));

        // Fake data - symulujemy raport pogodowy
        response.setAverageTemperature(22.5);
        response.setMinTemperature(18.0);
        response.setMaxTemperature(28.0);
        response.setSummary(String.format("Weather report for %s over last %d days: Mostly sunny with mild temperatures.",
                request.getCity(), request.getDays()));

        System.out.println("SOAP Response sent for: " + request.getCity());

        return response;
    }
}