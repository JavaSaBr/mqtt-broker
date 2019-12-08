package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.model.reason.code.PublishAckReasonCode
import com.ss.mqtt.broker.network.packet.in.PublishAckInPacket
import com.ss.mqtt.broker.network.packet.out.PublishAck5OutPacket
import com.ss.rlib.common.util.BufferUtils

class PublishAck5OutPacketTest extends BaseOutPacketTest {
    
    def "should write packet correctly"() {
        
        given:
            
            def packet = new PublishAck5OutPacket(
                packetId,
                PublishAckReasonCode.NOT_AUTHORIZED,
                userProperties,
                reasonString
            )
        
        when:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                packet.write(it)
            }
    
            def reader = new PublishAckInPacket(0b0100_0000 as byte)
            def result = reader.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        
        then:
            result
            reader.reasonCode == PublishAckReasonCode.NOT_AUTHORIZED
            reader.packetId == packetId
            reader.userProperties == userProperties
            reader.reason == reasonString
    }
}
