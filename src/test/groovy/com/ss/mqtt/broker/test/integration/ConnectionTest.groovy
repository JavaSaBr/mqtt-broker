package com.ss.mqtt.broker.test.integration

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode
import com.ss.mqtt.broker.model.MqttPropertyConstants
import org.springframework.beans.factory.annotation.Autowired

import java.util.concurrent.CompletionException

class ConnectionTest extends MqttBrokerTest {
    
    @Autowired
    InetSocketAddress deviceNetworkAddress
    
    def "subscriber should connect to broker without user and pass"() {
        given:
            def client = buildClient()
        when:
            def result = client.connect().join()
        then:
            result.reasonCode == Mqtt5ConnAckReasonCode.SUCCESS
            result.sessionExpiryInterval.present
            result.sessionExpiryInterval.getAsLong() == MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_DEFAULT
            result.serverKeepAlive.present
            result.serverKeepAlive.getAsInt() == MqttPropertyConstants.SERVER_KEEP_ALIVE_DISABLED
            !result.serverReference.present
            !result.responseInformation.present
            !result.assignedClientIdentifier.present
            !result.sessionPresent
        cleanup:
            client.disconnect()
    }
    
    def "subscriber should connect to broker with user and pass"() {
        given:
            def client = buildClient('1')
        when:
            def result = connectWith(client, 'user1', 'password')
        then:
            result.reasonCode == Mqtt5ConnAckReasonCode.SUCCESS
            result.sessionExpiryInterval.present
            result.sessionExpiryInterval.getAsLong() == MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_DEFAULT
            result.serverKeepAlive.present
            result.serverKeepAlive.getAsInt() == MqttPropertyConstants.SERVER_KEEP_ALIVE_DISABLED
            !result.serverReference.present
            !result.responseInformation.present
            !result.assignedClientIdentifier.present
            !result.sessionPresent
        cleanup:
            client.disconnect()
    }
    
    def "subscriber should connect to broker without providing a client id"() {
        given:
            def client = buildClient("")
        when:
            def result = client.connect().join()
        then:
            result.reasonCode == Mqtt5ConnAckReasonCode.SUCCESS
            result.assignedClientIdentifier.present
            result.assignedClientIdentifier.get().toString() != ""
        cleanup:
            client.disconnect()
    }
    
    def "subscriber should not connect to broker with invalid client id"(String clientId) {
        given:
            def client = MqttClient.builder()
                .identifier(clientId)
                .serverHost(deviceNetworkAddress.getHostName())
                .serverPort(deviceNetworkAddress.getPort())
                .useMqttVersion5()
                .build()
                .toAsync()
        when:
            client.connect().join()
        then:
            def ex = thrown CompletionException
            def cause = ex.cause as Mqtt5ConnAckException
            cause.mqttMessage.reasonCode == Mqtt5ConnAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID
        cleanup:
            client.disconnect()
        where:
            clientId << ["!@#!@*()^&"]
    }
    
    def "subscriber should not connect to broker with wrong pass"() {
        given:
            def client = buildClient()
        when:
            connectWith(client, 'user', 'wrongPassword')
        then:
            def ex = thrown CompletionException
            def cause = ex.cause as Mqtt5ConnAckException
            cause.mqttMessage.reasonCode == Mqtt5ConnAckReasonCode.BAD_USER_NAME_OR_PASSWORD
        cleanup:
            client.disconnect()
    }
}
