package com.ss.mqtt.broker.test.integration.config

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.ss.mqtt.broker.config.MqttBrokerConfig
import com.ss.mqtt.broker.network.MqttConnection
import com.ss.rlib.network.server.ServerNetwork
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

import java.util.function.Consumer

@Configuration
@Import(MqttBrokerConfig)
class MqttBrokerTestConfig {
    
    @Bean
    Mqtt5AsyncClient mqttSubscriber(InetSocketAddress deviceNetworkAddress) {
        return MqttClient.builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost(deviceNetworkAddress.getHostName())
            .serverPort(deviceNetworkAddress.getPort())
            .useMqttVersion5()
            .build()
            .toAsync()
    }
    
    @Bean
    Mqtt5AsyncClient mqttPublisher(InetSocketAddress deviceNetworkAddress) {
        return MqttClient.builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost(deviceNetworkAddress.getHostName())
            .serverPort(deviceNetworkAddress.getPort())
            .useMqttVersion5()
            .build()
            .toAsync()
    }
    
    @Bean
    InetSocketAddress deviceNetworkAddress(
        ServerNetwork<MqttConnection> deviceNetwork,
        Consumer<MqttConnection> mqttConnectionConsumer
    ) {
        
        def address = deviceNetwork.start()
        
        deviceNetwork.onAccept(mqttConnectionConsumer)
        
        return address
    }
}
