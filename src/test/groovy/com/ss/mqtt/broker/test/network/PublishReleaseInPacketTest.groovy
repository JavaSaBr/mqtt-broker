package com.ss.mqtt.broker.test.network

import com.ss.mqtt.broker.model.PacketProperty
import com.ss.mqtt.broker.model.PublishReleaseReasonCode
import com.ss.mqtt.broker.network.packet.in.PublishReleaseInPacket
import com.ss.rlib.common.util.BufferUtils
import com.ss.rlib.common.util.array.Array

class PublishReleaseInPacketTest extends InPacketTest {
    
    def "should read packet correctly as mqtt 3.1.1"() {
        
        given:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putShort(packetId)
            }
        
        when:
            def packet = new PublishReleaseInPacket(0b0110_0000 as byte)
            def result = packet.read(mqtt311Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.reason == ""
            packet.packetId == packetId
            packet.reasonCode == PublishReleaseReasonCode.SUCCESS
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
                it.put(PublishReleaseReasonCode.PACKET_IDENTIFIER_NOT_FOUND.value)
                it.putMbi(propertiesBuffer.limit())
                it.put(propertiesBuffer)
            }
    
        when:
            def packet = new PublishReleaseInPacket(0b0110_0000 as byte)
            def result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.reason == reasonString
            packet.packetId == packetId
            packet.reasonCode == PublishReleaseReasonCode.PACKET_IDENTIFIER_NOT_FOUND
            packet.userProperties == userProperties
        when:
    
            dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putShort(packetId)
                it.put(PublishReleaseReasonCode.SUCCESS.value)
                it.putMbi(0)
            }
        
            packet = new PublishReleaseInPacket(0b0110_0000 as byte)
            result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.reason == ""
            packet.packetId == packetId
            packet.reasonCode == PublishReleaseReasonCode.SUCCESS
            packet.userProperties == Array.empty()
    }
}
