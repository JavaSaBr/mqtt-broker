package com.ss.mqtt.broker.test.network.in

import com.ss.mqtt.broker.model.PacketProperty
import com.ss.mqtt.broker.model.PublishCompletedReasonCode
import com.ss.mqtt.broker.network.packet.in.PublishCompleteInPacket
import com.ss.rlib.common.util.BufferUtils
import com.ss.rlib.common.util.array.Array

class PublishCompleteInPacketTest extends BaseInPacketTest {
    
    def "should read packet correctly as mqtt 3.1.1"() {
        
        given:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putShort(packetId)
            }
        
        when:
            def packet = new PublishCompleteInPacket(0b0111_0000 as byte)
            def result = packet.read(mqtt311Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.reason == ""
            packet.packetId == packetId
            packet.reasonCode == PublishCompletedReasonCode.SUCCESS
            packet.userProperties == Array.empty()
    }
    
    def "should read packet correctly as mqtt 5.0"() {
        
        given:
            
            def propertiesBuffer = BufferUtils.prepareBuffer(512) {
                it.putProperty(PacketProperty.REASON_STRING, reasonString)
                it.putProperty(PacketProperty.USER_PROPERTY, userProperties)
            }
    
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putShort(packetId)
                it.put(PublishCompletedReasonCode.PACKET_IDENTIFIER_NOT_FOUND.value)
                it.putMbi(propertiesBuffer.limit())
                it.put(propertiesBuffer)
            }
    
        when:
            def packet = new PublishCompleteInPacket(0b0111_0000 as byte)
            def result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.reason == reasonString
            packet.packetId == packetId
            packet.reasonCode == PublishCompletedReasonCode.PACKET_IDENTIFIER_NOT_FOUND
            packet.userProperties == userProperties
        when:
    
            dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putShort(packetId)
                it.put(PublishCompletedReasonCode.PACKET_IDENTIFIER_NOT_FOUND.value)
                it.putMbi(0)
            }
        
            packet = new PublishCompleteInPacket(0b0111_0000 as byte)
            result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.reason == ""
            packet.packetId == packetId
            packet.reasonCode == PublishCompletedReasonCode.PACKET_IDENTIFIER_NOT_FOUND
            packet.userProperties == Array.empty()
    }
}
