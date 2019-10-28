package com.ss.mqtt.broker.test.network

import com.ss.mqtt.broker.model.PacketProperty
import com.ss.mqtt.broker.model.PublishReceivedReasonCode
import com.ss.mqtt.broker.network.packet.in.PublishReceivedInPacket
import com.ss.rlib.common.util.BufferUtils
import com.ss.rlib.common.util.array.Array

class PublishReceivedInPacketTest extends InPacketTest {
    
    def "should read packet correctly as mqtt 3.1.1"() {
        
        given:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putShort(packetId)
            }
        
        when:
            def packet = new PublishReceivedInPacket(0b0101_0000 as byte)
            def result = packet.read(mqtt311Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.getReason() == ""
            packet.getPacketId() == packetId
            packet.getReasonCode() == PublishReceivedReasonCode.SUCCESS
            packet.getUserProperties() == Array.empty()
    }
    
    def "should read packet correctly as mqtt 5.0"() {
        
        given:
            
            def propertiesBuffer = BufferUtils.prepareBuffer(512) {
                it.putProperty(PacketProperty.REASON_STRING, reasonString)
                it.putProperty(PacketProperty.USER_PROPERTY, userProperties)
            }
    
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putShort(packetId)
                it.put(PublishReceivedReasonCode.QUOTA_EXCEEDED.value)
                it.putMbi(propertiesBuffer.limit())
                it.put(propertiesBuffer)
            }
    
        when:
            def packet = new PublishReceivedInPacket(0b0101_0000 as byte)
            def result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.getReason() == reasonString
            packet.getPacketId() == packetId
            packet.getReasonCode() == PublishReceivedReasonCode.QUOTA_EXCEEDED
            packet.getUserProperties() == userProperties
        when:
    
            dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putShort(packetId)
                it.put(PublishReceivedReasonCode.IMPLEMENTATION_SPECIFIC_ERROR.value)
                it.putMbi(0)
            }
        
            packet = new PublishReceivedInPacket(0b0101_0000 as byte)
            result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.getReason() == ""
            packet.getPacketId() == packetId
            packet.getReasonCode() == PublishReceivedReasonCode.IMPLEMENTATION_SPECIFIC_ERROR
            packet.getUserProperties() == Array.empty()
    }
}
