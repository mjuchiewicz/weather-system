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

            WebServer webServer = new WebServer(PORT);

            org.apache.xmlrpc.server.XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();

            PropertyHandlerMapping handler = new PropertyHandlerMapping();

            handler.addHandler("MessagesService", MessageService.class);

            xmlRpcServer.setHandlerMapping(handler);

            webServer.start();

            System.out.println("Server started...");
            System.out.println("Listening on port: " + PORT);
            System.out.println("Press <ENTER> to stop the server.");

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            br.readLine();

            webServer.shutdown();
            System.out.println("Server stopped.");

        } catch (Exception e) {
            System.err.println("Something went wrong!");
            e.printStackTrace();
        }
    }
}