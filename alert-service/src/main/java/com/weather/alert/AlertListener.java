package com.weather.alert;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AlertListener {

    @RabbitListener(queues = "${weather.alert.queue}")
    public void receiveAlert(String alertMessage) {
        System.out.println("\n========================================");
        System.out.println("⚠️  WEATHER ALERT RECEIVED!");
        System.out.println("Message: " + alertMessage);
        System.out.println("========================================\n");

    }
}