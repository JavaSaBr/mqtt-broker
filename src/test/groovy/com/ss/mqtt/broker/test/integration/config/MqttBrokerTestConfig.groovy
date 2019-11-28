package com.ss.mqtt.broker.test.integration.config

import com.ss.mqtt.broker.config.MqttBrokerConfig
import com.ss.mqtt.broker.config.MqttConnectionConfig
import com.ss.mqtt.broker.model.MqttPropertyConstants
import com.ss.mqtt.broker.model.MqttVersion
import com.ss.mqtt.broker.network.MqttConnection
import com.ss.mqtt.broker.network.client.MqttClient
import com.ss.mqtt.broker.test.integration.IntegrationSpecification
import com.ss.rlib.network.server.ServerNetwork
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.PropertySource
import spock.lang.Shared
import spock.lang.Specification

import java.util.function.Consumer

@Configuration
@Import(MqttBrokerConfig)
@PropertySource("application-test.properties")
class MqttBrokerTestConfig extends Specification {

    @Bean
    InetSocketAddress deviceNetworkAddress(
        ServerNetwork<MqttConnection> deviceNetwork,
        Consumer<MqttConnection> mqttConnectionConsumer
    ) {
        def address = deviceNetwork.start()
        deviceNetwork.onAccept(mqttConnectionConsumer)
        return address
    }
    
    @Bean
    MqttConnection mqtt5MockedConnection(MqttConnectionConfig deviceConnectionConfig) {
        return Stub(MqttConnection) {
            isSupported(MqttVersion.MQTT_5) >> true
            getConfig() >> deviceConnectionConfig
            getClient() >> Stub(MqttClient.UnsafeMqttClient) {
                getConnectionConfig() >> deviceConnectionConfig
                getSessionExpiryInterval() >> MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_DISABLED
                getReceiveMax() >> deviceConnectionConfig.getReceiveMaximum()
                getMaximumPacketSize() >> deviceConnectionConfig.getMaximumPacketSize()
                getClientId() >> IntegrationSpecification.clientId
                getKeepAlive() >> MqttPropertyConstants.SERVER_KEEP_ALIVE_DEFAULT
                getTopicAliasMaximum() >> deviceConnectionConfig.getTopicAliasMaximum()
            }
        }
    }
    
    @Bean
    MqttConnection mqtt311MockedConnection(MqttConnectionConfig deviceConnectionConfig) {
        return Stub(MqttConnection) {
            isSupported(MqttVersion.MQTT_3_1_1) >> true
            isSupported(MqttVersion.MQTT_5) >> false
            getConfig() >> deviceConnectionConfig
            getClient() >> Stub(MqttClient.UnsafeMqttClient) {
                getConnectionConfig() >> deviceConnectionConfig
                getSessionExpiryInterval() >> MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_DISABLED
                getReceiveMax() >> deviceConnectionConfig.getReceiveMaximum()
                getMaximumPacketSize() >> deviceConnectionConfig.getMaximumPacketSize()
                getClientId() >> IntegrationSpecification.clientId
                getKeepAlive() >> MqttPropertyConstants.SERVER_KEEP_ALIVE_DEFAULT
                getTopicAliasMaximum() >> deviceConnectionConfig.getTopicAliasMaximum()
            }
        }
    }
}
