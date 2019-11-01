package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.model.MqttPropertyConstants
import com.ss.mqtt.broker.network.client.impl.DeviceMqttClient
import com.ss.mqtt.broker.test.network.BasePacketTest
import spock.lang.Shared

class BaseOutPacketTest extends BasePacketTest {
    
    @Shared
    DeviceMqttClient mqtt5Client = Stub(DeviceMqttClient) {
        getConnection() >> mqtt5Connection
        getSessionExpiryInterval() >> MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_UNDEFINED
        getReceiveMax() >> BasePacketTest.receiveMaximum
        getMaximumPacketSize() >> BasePacketTest.maximumPacketSize
        getClientId() >> "any"
        getKeepAlive() >> -1
        getTopicAliasMaximum() >> BasePacketTest.topicAliasMaximum
    }
    
    @Shared
    DeviceMqttClient mqtt311Client = Stub(DeviceMqttClient) {
        getConnection() >> mqtt311Connection
        getSessionExpiryInterval() >> MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_UNDEFINED
        getReceiveMax() >> BasePacketTest.receiveMaximum
        getMaximumPacketSize() >> BasePacketTest.maximumPacketSize
        getClientId() >> "any"
        getKeepAlive() >> -1
        getTopicAliasMaximum() >> BasePacketTest.topicAliasMaximum
    }
}
