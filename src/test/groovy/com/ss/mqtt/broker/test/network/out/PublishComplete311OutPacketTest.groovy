package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.model.reason.code.PublishCompletedReasonCode
import com.ss.mqtt.broker.network.packet.in.PublishCompleteInPacket
import com.ss.mqtt.broker.network.packet.out.PublishComplete311OutPacket
import com.ss.rlib.common.util.BufferUtils
import com.ss.rlib.common.util.array.Array

class PublishComplete311OutPacketTest extends BaseOutPacketTest {
    
    def "should write packet correctly"() {
        
        given:
            def packet = new PublishComplete311OutPacket(packetId)
        when:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                packet.write(it)
            }
    
            def reader = new PublishCompleteInPacket(0b0111_0000 as byte)
            def result = reader.read(mqtt311Connection, dataBuffer, dataBuffer.limit())
        
        then:
            result
            reader.reasonCode == PublishCompletedReasonCode.SUCCESS
            reader.packetId == packetId
            reader.userProperties == Array.empty()
            reader.reason == ""
    }
}
