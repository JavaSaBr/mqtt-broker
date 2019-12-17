package com.ss.mqtt.broker.test.integration

import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode
import com.ss.mqtt.broker.model.MqttPropertyConstants

class InternalConnectionTest extends IntegrationSpecification {
    
    def "client should connect to broker without user and pass using mqtt 3.1.1"() {
        given:
            def client = buildInternalMqtt311Client()
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
            def client = buildInternalMqtt5Client()
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
}
