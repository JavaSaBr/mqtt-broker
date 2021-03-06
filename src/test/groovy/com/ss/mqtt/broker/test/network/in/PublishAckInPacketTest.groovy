package com.ss.mqtt.broker.test.network.in

import com.ss.mqtt.broker.model.PacketProperty
import com.ss.mqtt.broker.model.reason.code.PublishAckReasonCode
import com.ss.mqtt.broker.network.packet.in.PublishAckInPacket
import com.ss.rlib.common.util.BufferUtils
import com.ss.rlib.common.util.array.Array

class PublishAckInPacketTest extends BaseInPacketTest {
    
    def "should read packet correctly as mqtt 3.1.1"() {
        
        given:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putShort(packetId)
            }
        
        when:
            def packet = new PublishAckInPacket(0b0100_0000 as byte)
            def result = packet.read(mqtt311Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.reason == ""
            packet.packetId == packetId
            packet.reasonCode == PublishAckReasonCode.SUCCESS
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
                it.put(PublishAckReasonCode.PAYLOAD_FORMAT_INVALID.value)
                it.putMbi(propertiesBuffer.limit())
                it.put(propertiesBuffer)
            }
    
        when:
            def packet = new PublishAckInPacket(0b0100_0000 as byte)
            def result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.reason == reasonString
            packet.packetId == packetId
            packet.reasonCode == PublishAckReasonCode.PAYLOAD_FORMAT_INVALID
            packet.userProperties == userProperties
        when:
    
            dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putShort(packetId)
                it.put(PublishAckReasonCode.UNSPECIFIED_ERROR.value)
                it.putMbi(0)
            }
        
            packet = new PublishAckInPacket(0b0100_0000 as byte)
            result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.reason == ""
            packet.packetId == packetId
            packet.reasonCode == PublishAckReasonCode.UNSPECIFIED_ERROR
            packet.userProperties == Array.empty()
    }
}
