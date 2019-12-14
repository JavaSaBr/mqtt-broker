package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.model.QoS
import com.ss.mqtt.broker.network.packet.in.PublishInPacket
import com.ss.mqtt.broker.network.packet.out.Publish5OutPacket
import com.ss.rlib.common.util.BufferUtils

class Publish5OutPacketTest extends BaseOutPacketTest {
    
    def "should write packet correctly"() {
        
        given:
            def packet = new Publish5OutPacket(
                packetId,
                QoS.EXACTLY_ONCE,
                true,
                true,
                publishTopic.toString(),
                publishPayload,
                topicAlias,
                false,
                responseTopic,
                correlationData,
                userProperties
            )
        when:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                packet.write(it)
            }
    
            def reader = new PublishInPacket(0b0011_1101 as byte)
            def result = reader.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        
        then:
            result
            reader.packetId == packetId
            reader.qos == QoS.EXACTLY_ONCE
            reader.retained
            reader.duplicate
            reader.payload == publishPayload
            reader.topicName == publishTopic
            reader.userProperties == userProperties
            reader.topicAlias == topicAlias
            !reader.payloadFormatIndicator
            reader.responseTopic == responseTopic
            reader.correlationData == correlationData
        when:
    
            packet = new Publish5OutPacket(
                packetId,
                QoS.AT_MOST_ONCE,
                false,
                false,
                publishTopic.toString(),
                publishPayload,
                topicAlias,
                true,
                responseTopic,
                correlationData,
                userProperties
            )
        
            dataBuffer = BufferUtils.prepareBuffer(512) {
                packet.write(it)
            }
        
            reader = new PublishInPacket(0b0011_0000 as byte)
            result = reader.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
    
        then:
            result
            reader.packetId == 0
            reader.qos == QoS.AT_MOST_ONCE
            !reader.retained
            !reader.duplicate
            reader.payload == publishPayload
            reader.topicName == publishTopic
            reader.userProperties == userProperties
            reader.topicAlias == topicAlias
            reader.payloadFormatIndicator
            reader.responseTopic == responseTopic
            reader.correlationData == correlationData
    }
}
