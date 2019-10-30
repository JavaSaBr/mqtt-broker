package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.model.PublishAckReasonCode
import com.ss.mqtt.broker.network.packet.in.PublishAckInPacket
import com.ss.mqtt.broker.network.packet.out.PublishAck311OutPacket
import com.ss.rlib.common.util.BufferUtils
import com.ss.rlib.common.util.array.Array

class PublishAck311OutPacketTest extends BaseOutPacketTest {
    
    def "should write packet correctly"() {
        
        given:
            def packet = new PublishAck311OutPacket(mqtt311Client, packetId)
        when:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                packet.write(it)
            }
    
            def reader = new PublishAckInPacket(0b0100_0000 as byte)
            def result = reader.read(mqtt311Connection, dataBuffer, dataBuffer.limit())
        
        then:
            result
            reader.reasonCode == PublishAckReasonCode.SUCCESS
            reader.packetId == packetId
            reader.userProperties == Array.empty()
            reader.reason == ""
    }
}
