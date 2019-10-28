package com.ss.mqtt.broker.test.network

import com.ss.mqtt.broker.model.DisconnectReasonCode
import com.ss.mqtt.broker.model.PacketProperty
import com.ss.mqtt.broker.network.packet.in.DisconnectInPacket
import com.ss.rlib.common.util.BufferUtils
import com.ss.rlib.common.util.array.Array

class DisconnectInPacketTest extends InPacketTest {
    
    def "should read packet correctly as mqtt 5.0"() {
        
        given:
            
            def propertiesBuffer = BufferUtils.prepareBuffer(512) {
                it.putProperty(PacketProperty.SESSION_EXPIRY_INTERVAL, sessionExpiryInterval)
                it.putProperty(PacketProperty.REASON_STRING, reasonString)
                it.putProperty(PacketProperty.SERVER_REFERENCE, serverReference)
                it.putProperty(PacketProperty.USER_PROPERTY, userProperties)
            }
    
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                it.put(DisconnectReasonCode.QUOTA_EXCEEDED.value)
                it.putMbi(propertiesBuffer.limit())
                it.put(propertiesBuffer)
            }
    
        when:
            def packet = new DisconnectInPacket(0b1110_0000 as byte)
            def result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.reason == reasonString
            packet.serverReference == serverReference
            packet.reasonCode == DisconnectReasonCode.QUOTA_EXCEEDED
            packet.sessionExpiryInterval == sessionExpiryInterval
            packet.userProperties == userProperties
        when:
    
            propertiesBuffer = BufferUtils.prepareBuffer(512) {
                it.putProperty(PacketProperty.SESSION_EXPIRY_INTERVAL, sessionExpiryInterval)
                it.putProperty(PacketProperty.SERVER_REFERENCE, serverReference)
            }
    
            dataBuffer = BufferUtils.prepareBuffer(512) {
                it.put(DisconnectReasonCode.PACKET_TOO_LARGE.value)
                it.putMbi(propertiesBuffer.limit())
                it.put(propertiesBuffer)
            }
        
            packet = new DisconnectInPacket(0b1110_0000 as byte)
            result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.reason == ""
            packet.serverReference == serverReference
            packet.reasonCode == DisconnectReasonCode.PACKET_TOO_LARGE
            packet.sessionExpiryInterval == sessionExpiryInterval
            packet.userProperties == Array.empty()
    }
}
