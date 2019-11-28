package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.model.MqttPropertyConstants
import com.ss.mqtt.broker.network.packet.in.SubscribeInPacket
import com.ss.mqtt.broker.network.packet.out.Subscribe311OutPacket
import com.ss.rlib.common.util.BufferUtils
import com.ss.rlib.common.util.array.Array

class Subscribe311OutPacketTest extends BaseOutPacketTest {
    
    def "should write packet correctly"() {
        
        given:
            
            def packet = new Subscribe311OutPacket(
                topicFiltersObj311,
                1
            )
        
        when:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                packet.write(it)
            }
            
            def reader = new SubscribeInPacket(0b0001_0000 as byte)
            def result = reader.read(mqtt311Connection, dataBuffer, dataBuffer.limit())
        
        then:
            result
            reader.packetId == 1
            reader.topicFilters == topicFiltersObj311
            reader.userProperties == Array.empty()
            reader.subscriptionId == MqttPropertyConstants.SUBSCRIPTION_ID_UNDEFINED
    }
}
