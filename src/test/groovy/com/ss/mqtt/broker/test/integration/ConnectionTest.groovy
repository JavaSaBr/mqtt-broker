package com.ss.mqtt.broker.test.integration

import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode

import java.util.concurrent.CompletionException

class ConnectionTest extends MqttBrokerTest {
    
    def "subscriber should not connect to broker with wrong pass"() {
        when:
            connectWith(mqttSubscriber, 'user', 'wrongPassword')
        then:
            def ex = thrown CompletionException
            def cause = ex.cause as Mqtt5ConnAckException
            cause.mqttMessage.reasonCode == Mqtt5ConnAckReasonCode.BAD_USER_NAME_OR_PASSWORD
    }
    
    def "subscriber should connect to broker without user and pass"() {
        when:
            def result = mqttSubscriber.connect().join()
        then:
            result.reasonCode == Mqtt5ConnAckReasonCode.SUCCESS
            !result.serverReference.present
            !result.responseInformation.present
            !result.serverKeepAlive.present
            !result.sessionExpiryInterval.present
            !result.assignedClientIdentifier.present
            !result.sessionPresent
    }
    
    def "subscriber should connect to broker with user and pass"() {
        given:
            def client = buildClient('1')
        when:
            def result = connectWith(client, 'user1', 'password')
        then:
            result.reasonCode == Mqtt5ConnAckReasonCode.SUCCESS
            !result.serverReference.present
            !result.responseInformation.present
            !result.serverKeepAlive.present
            !result.sessionExpiryInterval.present
            !result.assignedClientIdentifier.present
            !result.sessionPresent
    }
    
    def "subscriber should connect to broker without providing a client id"() {
        given:
            def client = buildClient('')
        when:
            def result = connectWith(client, 'user', 'password')
        then:
            result.reasonCode == Mqtt5ConnAckReasonCode.SUCCESS
            result.assignedClientIdentifier.present
            result.assignedClientIdentifier.get().toString() != ""
    }
    
    def "subscriber should not connect to broker with invalid client id"(String clientId) {
        given:
            def client = buildClient(clientId)
        when:
            connectWith(client, 'user', 'password')
        then:
            def ex = thrown CompletionException
            def cause = ex.cause as Mqtt5ConnAckException
            cause.mqttMessage.reasonCode == Mqtt5ConnAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID
        where:
            clientId << ["!@#!@*()^&"]
    }
}
