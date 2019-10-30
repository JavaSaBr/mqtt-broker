package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.model.PublishReleaseReasonCode
import com.ss.mqtt.broker.network.packet.in.PublishReleaseInPacket
import com.ss.mqtt.broker.network.packet.out.PublishRelease311OutPacket
import com.ss.rlib.common.util.BufferUtils
import com.ss.rlib.common.util.array.Array

class PublishRelease311OutPacketTest extends BaseOutPacketTest {
    
    def "should write packet correctly"() {
        
        given:
            def packet = new PublishRelease311OutPacket(mqtt311Client, packetId)
        when:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                packet.write(it)
            }
    
            def reader = new PublishReleaseInPacket(0b0110_0000 as byte)
            def result = reader.read(mqtt311Connection, dataBuffer, dataBuffer.limit())
        
        then:
            result
            reader.reasonCode == PublishReleaseReasonCode.SUCCESS
            reader.packetId == packetId
            reader.userProperties == Array.empty()
            reader.reason == ""
    }
}
