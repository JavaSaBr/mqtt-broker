package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.model.reason.code.PublishReceivedReasonCode
import com.ss.mqtt.broker.network.packet.in.PublishReceivedInPacket
import com.ss.mqtt.broker.network.packet.out.PublishReceived5OutPacket
import com.ss.rlib.common.util.BufferUtils

class PublishReceived5OutPacketTest extends BaseOutPacketTest {
    
    def "should write packet correctly"() {
        
        given:
            
            def packet = new PublishReceived5OutPacket(
                mqtt5Client,
                packetId,
                PublishReceivedReasonCode.UNSPECIFIED_ERROR,
                userProperties,
                reasonString
            )
        
        when:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                packet.write(it)
            }
    
            def reader = new PublishReceivedInPacket(0b0101_0000 as byte)
            def result = reader.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        
        then:
            result
            reader.reasonCode == PublishReceivedReasonCode.UNSPECIFIED_ERROR
            reader.packetId == packetId
            reader.userProperties == userProperties
            reader.reason == reasonString
    }
}
