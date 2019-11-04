package com.ss.mqtt.broker.test.integration.config

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.ss.mqtt.broker.config.MqttBrokerConfig
import com.ss.mqtt.broker.network.MqttConnection
import com.ss.rlib.network.server.ServerNetwork
import org.springframework.context.annotation.*

import java.util.function.Consumer

@Configuration
@Import(MqttBrokerConfig)
@PropertySources([
    @PropertySource("broker.properties"),
    @PropertySource("broker-test.properties")
])
class MqttBrokerTestConfig {
    
    @Bean
    static Mqtt5AsyncClient mqttSubscriber(InetSocketAddress deviceNetworkAddress) {
        return MqttClient.builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost(deviceNetworkAddress.getHostName())
            .serverPort(deviceNetworkAddress.getPort())
            .useMqttVersion5()
            .build()
            .toAsync()
    }
    
    @Bean
    static Mqtt5AsyncClient mqttPublisher(InetSocketAddress deviceNetworkAddress) {
        return MqttClient.builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost(deviceNetworkAddress.getHostName())
            .serverPort(deviceNetworkAddress.getPort())
            .useMqttVersion5()
            .build()
            .toAsync()
    }
    
    @Bean
    static InetSocketAddress deviceNetworkAddress(
        ServerNetwork<MqttConnection> deviceNetwork,
        Consumer<MqttConnection> mqttConnectionConsumer
    ) {
        def address = deviceNetwork.start()
        deviceNetwork.onAccept(mqttConnectionConsumer)
        return address
    }
}
