package com.weather.frontend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
@CrossOrigin(origins = "*")
public class ReportController {

    private static final String SOAP_URL = "http://localhost:8087/ws";

    @GetMapping("/{city}")
    public ResponseEntity<Map<String, Object>> generateReport(@PathVariable String city,
                                                              @RequestParam(defaultValue = "7") int days) {

        // SOAP Request XML
        String soapRequest = String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                        "xmlns:rep=\"http://weather.com/report\">" +
                        "<soapenv:Header/>" +
                        "<soapenv:Body>" +
                        "<rep:getWeatherReportRequest>" +
                        "<rep:city>%s</rep:city>" +
                        "<rep:days>%d</rep:days>" +
                        "</rep:getWeatherReportRequest>" +
                        "</soapenv:Body>" +
                        "</soapenv:Envelope>",
                city, days
        );

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);

            HttpEntity<String> request = new HttpEntity<>(soapRequest, headers);
            String soapResponse = restTemplate.postForObject(SOAP_URL, request, String.class);
            System.out.println("SOAP Response: " + soapResponse);

            // Parse SOAP response (simplified)
            Map<String, Object> result = parseSoapResponse(soapResponse);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to generate report: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    private Map<String, Object> parseSoapResponse(String xml) {
        Map<String, Object> result = new HashMap<>();

        result.put("city", extractValue(xml, "city>", "</"));
        result.put("reportDate", extractValue(xml, "reportDate>", "</"));
        result.put("avgTemp", extractValue(xml, "averageTemperature>", "</"));
        result.put("minTemp", extractValue(xml, "minTemperature>", "</"));
        result.put("maxTemp", extractValue(xml, "maxTemperature>", "</"));
        result.put("summary", extractValue(xml, "summary>", "</"));

        return result;
    }

    private String extractValue(String xml, String startTag, String endTag) {
        try {
            int start = xml.indexOf(startTag) + startTag.length();
            int end = xml.indexOf(endTag, start);
            return xml.substring(start, end);
        } catch (Exception e) {
            return "";
        }
    }
}