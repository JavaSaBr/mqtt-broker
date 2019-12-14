package com.ss.mqtt.broker.test.integration

import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3ConnAckException
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode
import com.ss.mqtt.broker.model.MqttPropertyConstants
import com.ss.mqtt.broker.model.QoS
import com.ss.mqtt.broker.model.reason.code.ConnectAckReasonCode
import com.ss.mqtt.broker.network.packet.in.ConnectAckInPacket
import com.ss.mqtt.broker.network.packet.out.Connect311OutPacket
import com.ss.rlib.common.util.ArrayUtils

import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletionException

class ExternalConnectionTest extends IntegrationSpecification {
    
    def "client should connect to broker without user and pass using mqtt 3.1.1"() {
        given:
            def client = buildExternalMqtt311Client()
        when:
            def result = client.connect().join()
        then:
            result.returnCode == Mqtt3ConnAckReturnCode.SUCCESS
            !result.sessionPresent
        cleanup:
            client.disconnect().join()
    }
    
    def "client should connect to broker without user and pass using mqtt 5"() {
        given:
            def client = buildExternalMqtt5Client()
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
            client.disconnect().join()
    }
    
    def "client should connect to broker with user and pass using mqtt 3.1.1"() {
        given:
            def client = buildExternalMqtt311Client()
        when:
            def result = connectWith(client, 'user1', 'password')
        then:
            result.returnCode == Mqtt3ConnAckReturnCode.SUCCESS
            !result.sessionPresent
        cleanup:
            client.disconnect().join()
    }
    
    def "client should connect to broker with user and pass using mqtt 5"() {
        given:
            def client = buildExternalMqtt5Client()
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
            client.disconnect().join()
    }
    
    def "client should not connect to broker without providing a client id using mqtt 3.1.1"() {
        given:
            def client = buildExternalMqtt311Client("")
        when:
            client.connect().join()
        then:
            def ex = thrown CompletionException
            def cause = ex.cause as Mqtt3ConnAckException
            cause.mqttMessage.returnCode == Mqtt3ConnAckReturnCode.IDENTIFIER_REJECTED
    }
    
    def "client should connect to broker without providing a client id using mqtt 5"() {
        given:
            def client = buildExternalMqtt5Client("")
        when:
            def result = client.connect().join()
        then:
            result.reasonCode == Mqtt5ConnAckReasonCode.SUCCESS
            result.assignedClientIdentifier.present
            result.assignedClientIdentifier.get().toString() != ""
        cleanup:
            client.disconnect().join()
    }
    
    def "client should not connect to broker with invalid client id using mqtt 3.1.1"(String clientId) {
        given:
            def client = buildExternalMqtt311Client(clientId)
        when:
            client.connect().join()
        then:
            def ex = thrown CompletionException
            def cause = ex.cause as Mqtt3ConnAckException
            cause.mqttMessage.returnCode == Mqtt3ConnAckReturnCode.IDENTIFIER_REJECTED
        where:
            clientId << ["!@#!@*()^&"]
    }
    
    def "client should not connect to broker with invalid client id using mqtt 5"(String clientId) {
        given:
            def client = buildExternalMqtt5Client(clientId)
        when:
            client.connect().join()
        then:
            def ex = thrown CompletionException
            def cause = ex.cause as Mqtt5ConnAckException
            cause.mqttMessage.reasonCode == Mqtt5ConnAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID
        where:
            clientId << ["!@#!@*()^&"]
    }
    
    def "client should not connect to broker with wrong pass using mqtt 3.1.1"() {
        given:
            def client = buildExternalMqtt311Client()
        when:
            connectWith(client, "user", "wrongPassword")
        then:
            def ex = thrown CompletionException
            def cause = ex.cause as Mqtt3ConnAckException
            cause.mqttMessage.returnCode == Mqtt3ConnAckReturnCode.BAD_USER_NAME_OR_PASSWORD
    }
    
    def "client should not connect to broker without username and with pass using mqtt 3.1.1"() {
        given:
            def client = buildMqtt311MockClient()
            def clientId = generateClientId()
        when:
    
            client.connect()
            client.send(new Connect311OutPacket(
                "",
                "",
                clientId,
                "wrongPassword".getBytes(StandardCharsets.UTF_8),
                ArrayUtils.EMPTY_BYTE_ARRAY,
                QoS.AT_MOST_ONCE,
                keepAlive,
                false,
                false
            ))
    
            def connectAck = client.readNext() as ConnectAckInPacket
        
        then:
            connectAck.reasonCode == ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD
    }
    
    def "client should not connect to broker with wrong pass using mqtt 5"() {
        given:
            def client = buildExternalMqtt5Client()
        when:
            connectWith(client, "user", "wrongPassword")
        then:
            def ex = thrown CompletionException
            def cause = ex.cause as Mqtt5ConnAckException
            cause.mqttMessage.reasonCode == Mqtt5ConnAckReasonCode.BAD_USER_NAME_OR_PASSWORD
    }
}
