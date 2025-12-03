package com.weather.alert;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

@Configuration
public class RabbitMQConfig {

    @Value("${weather.alert.queue}")
    private String queueName;

    @Bean
    public Queue weatherAlertQueue() {
        return new Queue(queueName, true);
    }
}