package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.network.packet.in.UnsubscribeAckInPacket
import com.ss.mqtt.broker.network.packet.out.UnsubscribeAck5OutPacket
import com.ss.rlib.common.util.BufferUtils

class UnsubscribeAck5OutPacketTest extends BaseOutPacketTest {
    
    def "should write packet correctly"() {
        
        given:
            
            def packet = new UnsubscribeAck5OutPacket(
                packetId,
                unsubscribeAckReasonCodes,
                userProperties,
                reasonString
            )
        
        when:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                packet.write(it)
            }
    
            def reader = new UnsubscribeAckInPacket(0b1011_0000 as byte)
            def result = reader.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        
        then:
            result
            reader.reasonCodes == unsubscribeAckReasonCodes
            reader.packetId == packetId
            reader.userProperties == userProperties
            reader.reason == reasonString
    }
}
