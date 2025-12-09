package com.weather.frontend;

import org.apache.xmlrpc.client.TimingOutCallback;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcSunHttpTransportFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.net.URL;

@RestController
@RequestMapping("/api/xmlrpc-proxy")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class XmlRpcProxyController {

    private XmlRpcClient xmlRpcClient;

    @PostConstruct
    public void init() throws Exception {
        System.out.println("🔧 Initializing XML-RPC Client...");

        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL("http://host.docker.internal:8099/xmlrpc"));
        config.setEnabledForExtensions(true);
        config.setConnectionTimeout(60 * 1000);
        config.setReplyTimeout(60 * 1000);

        xmlRpcClient = new XmlRpcClient();
        xmlRpcClient.setTransportFactory(new XmlRpcSunHttpTransportFactory(xmlRpcClient));
        xmlRpcClient.setConfig(config);

        System.out.println("✅ XML-RPC Client ready!");
    }

    @GetMapping("/history/{city}")
    public ResponseEntity<?> getHistory(
            @PathVariable String city,
            @RequestParam(defaultValue = "5") int days) {

        System.out.println("📞 Frontend calling XML-RPC: getHistory(" + city + ", " + days + ")");

        try {
            Object result = xmlRpcClient.execute("WeatherService.getHistory",
                    new Object[]{city, days});
            System.out.println("✅ XML-RPC response received (sync)");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ XML-RPC error: " + e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/history-async/{city}")
    public ResponseEntity<?> getHistoryAsync(
            @PathVariable String city,
            @RequestParam(defaultValue = "3") int days) {

        System.out.println("📞 Frontend calling XML-RPC: getHistoryWithDelay(" + city + ", " + days + ") - ASYNC");

        try {
            TimingOutCallback callback = new TimingOutCallback(10000);
            xmlRpcClient.executeAsync("WeatherService.getHistoryWithDelay",
                    new Object[]{city, days},
                    callback);

            System.out.println("⏳ Waiting for async XML-RPC response...");
            Object result = callback.waitForResponse();
            System.out.println("✅ XML-RPC async response received");

            return ResponseEntity.ok(result);

        } catch (Throwable e) {
            System.err.println("❌ XML-RPC error: " + e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/statistics/{city}")
    public ResponseEntity<?> getStatistics(@PathVariable String city) {

        System.out.println("📞 Frontend calling XML-RPC: getStatistics(" + city + ")");

        try {
            Object result = xmlRpcClient.execute("WeatherService.getStatistics",
                    new Object[]{city});
            System.out.println("✅ XML-RPC statistics received");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ XML-RPC error: " + e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}