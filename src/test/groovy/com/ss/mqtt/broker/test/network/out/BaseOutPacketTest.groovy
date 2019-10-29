package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.network.MqttClient
import com.ss.mqtt.broker.test.network.BasePacketTest
import spock.lang.Shared

class BaseOutPacketTest extends BasePacketTest {

    @Shared
    MqttClient mqttClient5 = Stub(MqttClient) {
    
        getConnection() >> mqtt5Connection
        getSessionExpiryInterval() >> -1
        getReceiveMax() >> BasePacketTest.receiveMaximum
        getMaximumPacketSize() >> BasePacketTest.maximumPacketSize
        getClientId() >> "any"
        getKeepAlive() >> -1
        getTopicAliasMaximum() >> BasePacketTest.topicAliasMaximum
    }
    
    @Shared
    MqttClient mqttClient311 = Stub(MqttClient) {
        getConnection() >> mqtt5Connection
    }
}
