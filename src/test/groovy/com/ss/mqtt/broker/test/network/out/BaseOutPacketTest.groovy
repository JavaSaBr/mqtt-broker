package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.network.MqttClient
import com.ss.mqtt.broker.test.network.BasePacketTest
import spock.lang.Shared

class BaseOutPacketTest extends BasePacketTest {
    
    @Shared
    MqttClient mqttClient5 = Stub(MqttClient) {
        
        getSessionExpiryInterval() >> sessionExpiryInterval
        getReceiveMax() >> receiveMaximum
        getMaximumPacketSize() >> maximumPacketSize
        getServerClientId() >> clientId
        getClientId() >> ""
    }
    
    @Shared
    MqttClient mqttClient311 = Stub(MqttClient) {
    }

}
