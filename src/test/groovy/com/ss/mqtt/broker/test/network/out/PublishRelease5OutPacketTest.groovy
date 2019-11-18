package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.model.reason.code.PublishReleaseReasonCode
import com.ss.mqtt.broker.network.packet.in.PublishReleaseInPacket
import com.ss.mqtt.broker.network.packet.out.PublishRelease5OutPacket
import com.ss.rlib.common.util.BufferUtils

class PublishRelease5OutPacketTest extends BaseOutPacketTest {
    
    def "should write packet correctly"() {
        
        given:
            
            def packet = new PublishRelease5OutPacket(
                mqtt5Client,
                packetId,
                PublishReleaseReasonCode.PACKET_IDENTIFIER_NOT_FOUND,
                userProperties,
                reasonString
            )
        
        when:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                packet.write(it)
            }
    
            def reader = new PublishReleaseInPacket(0b0110_0000 as byte)
            def result = reader.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        
        then:
            result
            reader.reasonCode == PublishReleaseReasonCode.PACKET_IDENTIFIER_NOT_FOUND
            reader.packetId == packetId
            reader.userProperties == userProperties
            reader.reason == reasonString
    }
}
