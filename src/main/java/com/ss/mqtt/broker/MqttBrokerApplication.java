package com.ss.mqtt.broker;

import com.ss.mqtt.broker.config.MqttBrokerConfig;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@RequiredArgsConstructor
@Import(MqttBrokerConfig.class)
public class MqttBrokerApplication {

    public static void main(@NotNull String[] args) {
        SpringApplication.run(MqttBrokerApplication.class, args);
    }
}
