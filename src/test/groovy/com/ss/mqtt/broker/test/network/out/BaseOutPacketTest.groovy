package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.model.MqttPropertyConstants
import com.ss.mqtt.broker.network.client.MqttClient
import com.ss.mqtt.broker.test.network.BasePacketTest
import spock.lang.Shared

class BaseOutPacketTest extends BasePacketTest {
    
    @Shared
    MqttClient mqtt5Client = Stub(MqttClient) {
        getConnectionConfig() >> mqttConnectionConfig
        getSessionExpiryInterval() >> MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_UNDEFINED
        getReceiveMax() >> BasePacketTest.receiveMaximum
        getMaximumPacketSize() >> BasePacketTest.maximumPacketSize
        getClientId() >> "any"
        getKeepAlive() >> -1
        getTopicAliasMaximum() >> BasePacketTest.topicAliasMaximum
    }
    
    @Shared
    MqttClient mqtt311Client = Stub(MqttClient) {
        getConnectionConfig() >> mqttConnectionConfig
        getSessionExpiryInterval() >> MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_UNDEFINED
        getReceiveMax() >> BasePacketTest.receiveMaximum
        getMaximumPacketSize() >> BasePacketTest.maximumPacketSize
        getClientId() >> "any"
        getKeepAlive() >> -1
        getTopicAliasMaximum() >> BasePacketTest.topicAliasMaximum
    }
}
