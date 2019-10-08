package com.ss.mqtt.broker.test.integration.config

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.ss.mqtt.broker.config.MqttBrokerConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(MqttBrokerConfig)
class MqttBrokerTestConfig {
    
    @Bean
    Mqtt5AsyncClient mqttSubscriber() {
       return MqttClient.builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost("localhost")
            .useMqttVersion5()
            .build()
            .toAsync()
    }
    
    @Bean
    Mqtt5AsyncClient mqttPublisher() {
        return MqttClient.builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost("localhost")
            .useMqttVersion5()
            .build()
            .toAsync()
    }
}
