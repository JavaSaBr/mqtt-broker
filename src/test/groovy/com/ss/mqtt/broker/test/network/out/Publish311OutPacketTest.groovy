package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.model.QoS
import com.ss.mqtt.broker.network.packet.in.PublishInPacket
import com.ss.mqtt.broker.network.packet.out.Publish311OutPacket
import com.ss.rlib.common.util.BufferUtils
import com.ss.rlib.common.util.array.Array

class Publish311OutPacketTest extends BaseOutPacketTest {
    
    def "should write packet correctly"() {
        
        given:
            def packet = new Publish311OutPacket(
                packetId,
                QoS.EXACTLY_ONCE_DELIVERY,
                true,
                true,
                publishTopic.toString(),
                publishPayload
            )
        when:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                packet.write(it)
            }
    
            def reader = new PublishInPacket(0b0011_1101 as byte)
            def result = reader.read(mqtt311Connection, dataBuffer, dataBuffer.limit())
        
        then:
            result
            reader.packetId == packetId
            reader.qos == QoS.EXACTLY_ONCE_DELIVERY
            reader.retained
            reader.duplicate
            reader.payload == publishPayload
            reader.topicName == publishTopic
            reader.userProperties == Array.empty()
        when:
    
            packet = new Publish311OutPacket(
                packetId,
                QoS.AT_MOST_ONCE_DELIVERY,
                false,
                false,
                publishTopic.toString(),
                publishPayload
            )
        
            dataBuffer = BufferUtils.prepareBuffer(512) {
                packet.write(it)
            }
        
            reader = new PublishInPacket(0b0011_0000 as byte)
            result = reader.read(mqtt311Connection, dataBuffer, dataBuffer.limit())
    
        then:
            result
            reader.packetId == 0
            reader.qos == QoS.AT_MOST_ONCE_DELIVERY
            !reader.retained
            !reader.duplicate
            reader.payload == publishPayload
            reader.topicName == publishTopic
            reader.userProperties == Array.empty()
    }
}
