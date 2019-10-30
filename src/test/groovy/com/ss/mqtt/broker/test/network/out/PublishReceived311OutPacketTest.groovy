package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.model.PublishReceivedReasonCode
import com.ss.mqtt.broker.network.packet.in.PublishReceivedInPacket
import com.ss.mqtt.broker.network.packet.out.PublishReceived311OutPacket
import com.ss.rlib.common.util.BufferUtils
import com.ss.rlib.common.util.array.Array

class PublishReceived311OutPacketTest extends BaseOutPacketTest {
    
    def "should write packet correctly"() {
        
        given:
            def packet = new PublishReceived311OutPacket(mqtt311Client, packetId)
        when:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                packet.write(it)
            }
    
            def reader = new PublishReceivedInPacket(0b0101_0000 as byte)
            def result = reader.read(mqtt311Connection, dataBuffer, dataBuffer.limit())
        
        then:
            result
            reader.reasonCode == PublishReceivedReasonCode.SUCCESS
            reader.packetId == packetId
            reader.userProperties == Array.empty()
            reader.reason == ""
    }
}
