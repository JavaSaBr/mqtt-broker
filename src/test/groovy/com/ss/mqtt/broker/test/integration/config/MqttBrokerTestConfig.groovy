package com.ss.mqtt.broker.test.integration.config

import com.ss.mqtt.broker.config.MqttBrokerConfig
import com.ss.mqtt.broker.config.MqttNetworkConfig
import com.ss.mqtt.broker.network.MqttConnection
import com.ss.rlib.network.server.ServerNetwork
import org.jetbrains.annotations.NotNull
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.PropertySource

import java.util.function.Consumer

@Configuration
@Import([
    MqttBrokerConfig,
    MqttNetworkConfig
])
@PropertySource("classpath:application-test.properties")
class MqttBrokerTestConfig {

    @Bean
    InetSocketAddress externalNetworkAddress(
        ServerNetwork<MqttConnection> externalNetwork,
        Consumer<MqttConnection> externalConnectionConsumer
    ) {
        def address = externalNetwork.start()
        externalNetwork.onAccept(externalConnectionConsumer)
        return address
    }
    
    @Bean
    @NotNull InetSocketAddress internalNetworkAddress(
        ServerNetwork<MqttConnection> internalNetwork,
        Consumer<MqttConnection> internalConnectionConsumer
    ) {
        def address = internalNetwork.start()
        internalNetwork.onAccept(internalConnectionConsumer)
        return address
    }
}
