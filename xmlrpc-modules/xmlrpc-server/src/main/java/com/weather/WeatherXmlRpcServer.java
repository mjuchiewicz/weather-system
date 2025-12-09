package com.weather;

import com.weather.WeatherHistoryService;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.webserver.WebServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Weather XML-RPC Server
 *
 * ZGODNE Z LISTĄ Cw0:
 * - WebServer (Apache XML-RPC)
 * - PropertyHandlerMapping
 * - addHandler("ServiceName", ServiceClass.class)
 * - webServer.start()
 */
public class WeatherXmlRpcServer {

    private static final int PORT = 8099;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Weather XML-RPC Server");
        System.out.println("========================================");

        try {
            // 1. Create WebServer - ZGODNE Z LISTĄ!
            System.out.println("Creating XML-RPC WebServer on port " + PORT + "...");
            WebServer webServer = new WebServer(PORT);

            // 2. Get XmlRpcServer - ZGODNE Z LISTĄ!
            XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();

            // 3. Create PropertyHandlerMapping - ZGODNE Z LISTĄ!
            System.out.println("Registering service handlers...");
            PropertyHandlerMapping handler = new PropertyHandlerMapping();

            // 4. Add handler - ZGODNE Z LISTĄ!
            handler.addHandler("WeatherService", WeatherHistoryService.class);
            System.out.println("✅ Registered: WeatherService -> WeatherHistoryService.class");

            // 5. Set handler mapping - ZGODNE Z LISTĄ!
            xmlRpcServer.setHandlerMapping(handler);

            // 6. Start server - ZGODNE Z LISTĄ!
            webServer.start();

            System.out.println("========================================");
            System.out.println("✅ XML-RPC Server started successfully!");
            System.out.println("========================================");
            System.out.println("Port: " + PORT);
            System.out.println("URL:  http://localhost:" + PORT + "/xmlrpc");
            System.out.println();
            System.out.println("Available methods:");
            System.out.println("  - WeatherService.getHistory(city, days)");
            System.out.println("  - WeatherService.getHistoryWithDelay(city, days)");
            System.out.println("  - WeatherService.getStatistics(city)");
            System.out.println();
            System.out.println("Press <ENTER> to stop the server...");
            System.out.println("========================================");

            // Wait for Enter key - ZGODNE Z LISTĄ!
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            br.readLine();

            // Shutdown
            System.out.println("\nShutting down server...");
            webServer.shutdown();
            System.out.println("✅ Server stopped successfully.");

        } catch (Exception e) {
            System.err.println("❌ Error starting server:");
            e.printStackTrace();
        }
    }
}