package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.network.client.MqttClient
import com.ss.mqtt.broker.test.network.BasePacketTest
import spock.lang.Shared

class BaseOutPacketTest extends BasePacketTest {
    
    @Shared
    MqttClient mqtt5Client = Stub(MqttClient.UnsafeMqttClient) {
        getConnectionConfig() >> mqttConnectionConfig
        getSessionExpiryInterval() >> BasePacketTest.sessionExpiryInterval
        getReceiveMax() >> BasePacketTest.receiveMaximum
        getMaximumPacketSize() >> BasePacketTest.maximumPacketSize
        getClientId() >> clientId
        getKeepAlive() >> serverKeepAlive
        getTopicAliasMaximum() >> BasePacketTest.topicAliasMaximum
    }
    
    @Shared
    MqttClient mqtt311Client = Stub(MqttClient.UnsafeMqttClient) {
        getConnectionConfig() >> mqttConnectionConfig
        getSessionExpiryInterval() >> BasePacketTest.sessionExpiryInterval
        getReceiveMax() >> BasePacketTest.receiveMaximum
        getMaximumPacketSize() >> BasePacketTest.maximumPacketSize
        getClientId() >> clientId
        getKeepAlive() >> serverKeepAlive
        getTopicAliasMaximum() >> BasePacketTest.topicAliasMaximum
    }
}
