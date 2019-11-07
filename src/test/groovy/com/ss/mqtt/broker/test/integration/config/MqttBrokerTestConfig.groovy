package com.ss.mqtt.broker.test.integration.config

import com.ss.mqtt.broker.config.MqttBrokerConfig
import com.ss.mqtt.broker.network.MqttConnection
import com.ss.rlib.network.server.ServerNetwork
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.PropertySource

import java.util.function.Consumer

@Configuration
@Import(MqttBrokerConfig)
@PropertySource("application-test.properties")
class MqttBrokerTestConfig {

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
