package com.ss.mqtt.broker.test.network.in

import com.ss.mqtt.broker.model.PacketProperty
import com.ss.mqtt.broker.model.reason.code.SubscribeAckReasonCode
import com.ss.mqtt.broker.network.packet.in.SubscribeAckInPacket
import com.ss.rlib.common.util.BufferUtils
import com.ss.rlib.common.util.array.Array

class SubscribeAckInPacketTest extends BaseInPacketTest {
    
    def "should read packet correctly as mqtt 3.1.1"() {
        
        given:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putShort(packetId)
                it.put(SubscribeAckReasonCode.GRANTED_QOS_0.value)
                it.put(SubscribeAckReasonCode.GRANTED_QOS_2.value)
                it.put(SubscribeAckReasonCode.GRANTED_QOS_1.value)
                it.put(SubscribeAckReasonCode.UNSPECIFIED_ERROR.value)
            }
        
        when:
            def packet = new SubscribeAckInPacket(0b1001_0000 as byte)
            def result = packet.read(mqtt311Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.reason == ""
            packet.packetId == packetId
            packet.reasonCodes.size() == 4
            packet.reasonCodes.get(0) == SubscribeAckReasonCode.GRANTED_QOS_0
            packet.reasonCodes.get(1) == SubscribeAckReasonCode.GRANTED_QOS_2
            packet.reasonCodes.get(2) == SubscribeAckReasonCode.GRANTED_QOS_1
            packet.reasonCodes.get(3) == SubscribeAckReasonCode.UNSPECIFIED_ERROR
    }
    
    def "should read packet correctly as mqtt 5.0"() {
        
        given:
            
            def propertiesBuffer = BufferUtils.prepareBuffer(512) {
                it.putProperty(PacketProperty.REASON_STRING, reasonString)
                it.putProperty(PacketProperty.USER_PROPERTY, userProperties)
            }
    
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putShort(packetId)
                it.putMbi(propertiesBuffer.limit())
                it.put(propertiesBuffer)
                it.put(SubscribeAckReasonCode.GRANTED_QOS_0.value)
                it.put(SubscribeAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR.value)
                it.put(SubscribeAckReasonCode.GRANTED_QOS_1.value)
                it.put(SubscribeAckReasonCode.UNSPECIFIED_ERROR.value)
            }
    
        when:
            def packet = new SubscribeAckInPacket(0b1001_0000 as byte)
            def result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.reason == reasonString
            packet.packetId == packetId
            packet.reasonCodes.size() == 4
            packet.reasonCodes.get(0) == SubscribeAckReasonCode.GRANTED_QOS_0
            packet.reasonCodes.get(1) == SubscribeAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR
            packet.reasonCodes.get(2) == SubscribeAckReasonCode.GRANTED_QOS_1
            packet.reasonCodes.get(3) == SubscribeAckReasonCode.UNSPECIFIED_ERROR
            packet.userProperties == userProperties
        when:
    
            dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putShort(packetId)
                it.putMbi(0)
                it.put(SubscribeAckReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED.value)
                it.put(SubscribeAckReasonCode.GRANTED_QOS_2.value)
                it.put(SubscribeAckReasonCode.GRANTED_QOS_1.value)
                it.put(SubscribeAckReasonCode.UNSPECIFIED_ERROR.value)
            }
        
            packet = new SubscribeAckInPacket(0b1001_0000 as byte)
            result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.reason == ""
            packet.packetId == packetId
            packet.reasonCodes.size() == 4
            packet.reasonCodes.get(0) == SubscribeAckReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED
            packet.reasonCodes.get(1) == SubscribeAckReasonCode.GRANTED_QOS_2
            packet.reasonCodes.get(2) == SubscribeAckReasonCode.GRANTED_QOS_1
            packet.reasonCodes.get(3) == SubscribeAckReasonCode.UNSPECIFIED_ERROR
            packet.userProperties == Array.empty()
    }
}
