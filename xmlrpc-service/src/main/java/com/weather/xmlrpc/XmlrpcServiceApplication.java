package com.weather.xmlrpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class XmlrpcServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(XmlrpcServiceApplication.class, args);
    }

}
