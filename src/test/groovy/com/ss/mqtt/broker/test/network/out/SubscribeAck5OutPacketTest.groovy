package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.network.packet.in.SubscribeAckInPacket
import com.ss.mqtt.broker.network.packet.out.SubscribeAck5OutPacket
import com.ss.rlib.common.util.BufferUtils

class SubscribeAck5OutPacketTest extends BaseOutPacketTest {
    
    def "should write packet correctly"() {
        
        given:
            
            def packet = new SubscribeAck5OutPacket(
                packetId,
                subscribeAckReasonCodes,
                userProperties,
                reasonString
            )
        
        when:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                packet.write(it)
            }
    
            def reader = new SubscribeAckInPacket(0b1001_0000 as byte)
            def result = reader.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        
        then:
            result
            reader.reasonCodes == subscribeAckReasonCodes
            reader.packetId == packetId
            reader.userProperties == userProperties
            reader.reason == reasonString
    }
}
