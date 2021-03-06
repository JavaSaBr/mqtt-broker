package com.ss.mqtt.broker.test.integration

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.ss.mqtt.broker.config.MqttConnectionConfig
import com.ss.mqtt.broker.model.MqttPropertyConstants
import com.ss.mqtt.broker.model.MqttVersion
import com.ss.mqtt.broker.network.MqttConnection
import com.ss.mqtt.broker.test.integration.config.MqttBrokerTestConfig
import com.ss.mqtt.broker.test.mock.MqttMockClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger

@ContextConfiguration(classes = MqttBrokerTestConfig)
class IntegrationSpecification extends Specification {
    
    public static final encoding = StandardCharsets.UTF_8
    public static final topicFilter = "topic/Filter"
    public static final publishPayload = "publishPayload".getBytes(encoding)
    public static final clientId = "testClientId"
    public static final keepAlive = 120
    
    private static final idGenerator = new AtomicInteger(1)
    
    @Autowired
    InetSocketAddress externalNetworkAddress
    
    @Autowired
    InetSocketAddress internalNetworkAddress
    
    @Autowired
    MqttConnectionConfig externalConnectionConfig
    
    def buildExternalMqtt311Client() {
        return buildMqtt311Client(generateClientId(), externalNetworkAddress)
    }
    
    def buildInternalMqtt311Client() {
        return buildMqtt311Client(generateClientId(), internalNetworkAddress)
    }
    
    def buildExternalMqtt5Client() {
        return buildMqtt5Client(generateClientId(), externalNetworkAddress)
    }
    
    def buildInternalMqtt5Client() {
        return buildMqtt5Client(generateClientId(), internalNetworkAddress)
    }
    
    def buildExternalMqtt311Client(String clientId) {
        return MqttClient.builder()
            .identifier(clientId)
            .serverHost(externalNetworkAddress.getHostName())
            .serverPort(externalNetworkAddress.getPort())
            .useMqttVersion3()
            .build()
            .toAsync()
    }
    
    def buildMqtt311Client(String clientId, InetSocketAddress address) {
        return MqttClient.builder()
            .identifier(clientId)
            .serverHost(address.getHostName())
            .serverPort(address.getPort())
            .useMqttVersion3()
            .build()
            .toAsync()
    }
    
    def buildExternalMqtt5Client(String clientId) {
        return MqttClient.builder()
            .identifier(clientId)
            .serverHost(externalNetworkAddress.getHostName())
            .serverPort(externalNetworkAddress.getPort())
            .useMqttVersion5()
            .build()
            .toAsync()
    }
    
    def buildMqtt5Client(String clientId, InetSocketAddress address) {
        return MqttClient.builder()
            .identifier(clientId)
            .serverHost(address.getHostName())
            .serverPort(address.getPort())
            .useMqttVersion5()
            .build()
            .toAsync()
    }
    
    def generateClientId() {
        return generateClientId("Default")
    }
    
    def generateClientId(String prefix) {
        return prefix + "_" + idGenerator.incrementAndGet()
    }
    
    def connectWith(Mqtt3AsyncClient client, String user, String pass) {
        return client.connectWith()
            .simpleAuth()
            .username(user)
            .password(pass.getBytes(encoding))
            .applySimpleAuth()
            .send()
            .join()
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
            externalNetworkAddress.getHostName(),
            externalNetworkAddress.getPort(),
            mqtt5MockedConnection(externalConnectionConfig)
        )
    }
    
    def buildMqtt311MockClient() {
        return new MqttMockClient(
            externalNetworkAddress.getHostName(),
            externalNetworkAddress.getPort(),
            mqtt311MockedConnection(externalConnectionConfig)
        )
    }
    
    def mqtt5MockedConnection(MqttConnectionConfig deviceConnectionConfig) {

        return Stub(MqttConnection) {
            isSupported(MqttVersion.MQTT_5) >> true
            isSupported(MqttVersion.MQTT_3_1_1) >> true
            getConfig() >> deviceConnectionConfig
            getClient() >> Stub(com.ss.mqtt.broker.network.client.MqttClient.UnsafeMqttClient) {
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
    
    def mqtt311MockedConnection(MqttConnectionConfig deviceConnectionConfig) {
        return Stub(MqttConnection) {
            isSupported(MqttVersion.MQTT_5) >> false
            isSupported(MqttVersion.MQTT_3_1_1) >> true
            getConfig() >> deviceConnectionConfig
            getClient() >> Stub(com.ss.mqtt.broker.network.client.MqttClient.UnsafeMqttClient) {
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
