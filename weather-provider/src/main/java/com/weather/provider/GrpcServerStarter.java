package com.weather.provider;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class GrpcServerStarter implements CommandLineRunner {

    @Autowired
    private WeatherGrpcService weatherGrpcService;

    @Override
    public void run(String... args) throws Exception {
        Server server = ServerBuilder.forPort(9090)
                .addService(weatherGrpcService)
                .build();

        server.start();
        System.out.println("gRPC Server started on port 9090");

        server.awaitTermination();
    }
}