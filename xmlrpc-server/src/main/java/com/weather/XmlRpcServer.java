package com.weather;

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.webserver.WebServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class XmlRpcServer {
    private static final int PORT = 8090;

    public static void main(String[] args) {
        try {
            System.out.println("Starting XML-RPC Server...");

            // 1. Create WebServer on port 8090
            WebServer webServer = new WebServer(PORT);

            // 2. Get XmlRpcServer from WebServer
            org.apache.xmlrpc.server.XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();

            // 3. Create handler mapping
            PropertyHandlerMapping handler = new PropertyHandlerMapping();

            // 4. Add handler for MessageService
            handler.addHandler("MessagesService", MessageService.class);

            // 5. Set handler mapping
            xmlRpcServer.setHandlerMapping(handler);

            // 6. Start server
            webServer.start();

            System.out.println("Server started...");
            System.out.println("Listening on port: " + PORT);
            System.out.println("Press <ENTER> to stop the server.");

            // Wait for ENTER key
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            br.readLine();

            // Shutdown
            webServer.shutdown();
            System.out.println("Server stopped.");

        } catch (Exception e) {
            System.err.println("Something went wrong!");
            e.printStackTrace();
        }
    }
}