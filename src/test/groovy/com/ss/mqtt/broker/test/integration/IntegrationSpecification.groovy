package com.ss.mqtt.broker.test.integration

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.ss.mqtt.broker.config.MqttConnectionConfig
import com.ss.mqtt.broker.network.MqttConnection
import com.ss.mqtt.broker.test.integration.config.MqttBrokerTestConfig
import com.ss.mqtt.broker.test.mock.MqttMockClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.nio.charset.StandardCharsets

@ContextConfiguration(classes = MqttBrokerTestConfig)
class IntegrationSpecification extends Specification {
    
    public static final encoding = StandardCharsets.UTF_8
    public static final topicFilter = "topic/Filter"
    public static final publishPayload = "publishPayload".getBytes(encoding)
    public static final clientId = "testClientId"
    public static final keepAlive = 120
    
    @Autowired
    InetSocketAddress deviceNetworkAddress
    
    @Autowired
    MqttConnectionConfig deviceConnectionConfig
    
    @Autowired
    MqttConnection mqtt5MockedConnection
    
    @Autowired
    MqttConnection mqtt311MockedConnection
    
    def buildClient() {
        return buildClient(generateClientId())
    }
    
    def buildClient(String clientId) {
        return MqttClient.builder()
            .identifier(clientId)
            .serverHost(deviceNetworkAddress.getHostName())
            .serverPort(deviceNetworkAddress.getPort())
            .useMqttVersion5()
            .build()
            .toAsync()
    }
    
    def generateClientId() {
        UUID.randomUUID().toString()
    }
    
    def connectWith(Mqtt5AsyncClient client, String user, String pass) {
        return client.connectWith()
            .simpleAuth()
            .username(user)
            .password(pass.getBytes(encoding))
            .applySimpleAuth()
            .send()
            .join()
    }
    
    def buildMqtt5MockClient() {
        return new MqttMockClient(
            deviceNetworkAddress.getHostName(),
            deviceNetworkAddress.getPort(),
            mqtt5MockedConnection
        )
    }
    
    def buildMqtt311MockClient() {
        return new MqttMockClient(
            deviceNetworkAddress.getHostName(),
            deviceNetworkAddress.getPort(),
            mqtt311MockedConnection
        )
    }
}
