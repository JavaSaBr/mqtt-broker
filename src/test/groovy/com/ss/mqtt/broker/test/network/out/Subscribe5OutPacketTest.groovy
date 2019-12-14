package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.model.MqttPropertyConstants
import com.ss.mqtt.broker.network.packet.in.SubscribeInPacket
import com.ss.mqtt.broker.network.packet.out.Subscribe5OutPacket
import com.ss.rlib.common.util.BufferUtils

class Subscribe5OutPacketTest extends BaseOutPacketTest {
    
    def "should write packet correctly"() {
        
        given:
            
            def packet = new Subscribe5OutPacket(
                topicFiltersObj5,
                1,
                userProperties,
                MqttPropertyConstants.SUBSCRIPTION_ID_UNDEFINED
            )
        
        when:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                packet.write(it)
            }
            
            def reader = new SubscribeInPacket(0b1000_0000 as byte)
            def result = reader.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        
        then:
            result
            reader.packetId == 1
            reader.topicFilters == topicFiltersObj5
            reader.userProperties == userProperties
            reader.subscriptionId == MqttPropertyConstants.SUBSCRIPTION_ID_UNDEFINED
    }
}
