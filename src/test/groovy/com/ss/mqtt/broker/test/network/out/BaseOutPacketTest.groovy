package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.network.client.MqttClient
import com.ss.mqtt.broker.test.network.NetworkUnitSpecification
import spock.lang.Shared

class BaseOutPacketTest extends NetworkUnitSpecification {
    
    @Shared
    MqttClient mqtt5Client = Stub(MqttClient.UnsafeMqttClient) {
        getConnectionConfig() >> mqttConnectionConfig
        getSessionExpiryInterval() >> NetworkUnitSpecification.sessionExpiryInterval
        getReceiveMax() >> NetworkUnitSpecification.receiveMaximum
        getMaximumPacketSize() >> NetworkUnitSpecification.maximumPacketSize
        getClientId() >> clientId
        getKeepAlive() >> serverKeepAlive
        getTopicAliasMaximum() >> NetworkUnitSpecification.topicAliasMaximum
    }
    
    @Shared
    MqttClient mqtt311Client = Stub(MqttClient.UnsafeMqttClient) {
        getConnectionConfig() >> mqttConnectionConfig
        getSessionExpiryInterval() >> NetworkUnitSpecification.sessionExpiryInterval
        getReceiveMax() >> NetworkUnitSpecification.receiveMaximum
        getMaximumPacketSize() >> NetworkUnitSpecification.maximumPacketSize
        getClientId() >> clientId
        getKeepAlive() >> serverKeepAlive
        getTopicAliasMaximum() >> NetworkUnitSpecification.topicAliasMaximum
    }
}
