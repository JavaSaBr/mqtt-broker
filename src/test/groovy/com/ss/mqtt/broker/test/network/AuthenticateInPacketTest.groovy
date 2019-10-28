package com.ss.mqtt.broker.test.network

import com.ss.mqtt.broker.model.AuthenticateReasonCode
import com.ss.mqtt.broker.model.PacketProperty
import com.ss.mqtt.broker.network.packet.in.AuthenticateInPacket
import com.ss.rlib.common.util.BufferUtils
import com.ss.rlib.common.util.array.Array

class AuthenticateInPacketTest extends InPacketTest {

    def "should read packet correctly as mqtt 5.0"() {
        
        given:
            
            def propertiesBuffer = BufferUtils.prepareBuffer(512) {
                it.putProperty(PacketProperty.AUTHENTICATION_METHOD, authMethod)
                it.putProperty(PacketProperty.AUTHENTICATION_DATA, authData)
                it.putProperty(PacketProperty.REASON_STRING, reasonString)
                it.putProperty(PacketProperty.USER_PROPERTY, userProperties)
            }
    
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                it.put(AuthenticateReasonCode.SUCCESS.value)
                it.putMbi(propertiesBuffer.limit())
                it.put(propertiesBuffer)
            }
    
        when:
            def packet = new AuthenticateInPacket(0b1111_0000 as byte)
            def result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.getReasonCode() == AuthenticateReasonCode.SUCCESS
            packet.getAuthenticationMethod() == authMethod
            packet.getAuthenticationData() == authData
            packet.getReason() == reasonString
            packet.getUserProperties() == userProperties
        when:
    
            propertiesBuffer = BufferUtils.prepareBuffer(512) {
                it.putProperty(PacketProperty.AUTHENTICATION_METHOD, authMethod)
                it.putProperty(PacketProperty.REASON_STRING, reasonString)
                it.putProperty(PacketProperty.USER_PROPERTY, userProperties)
                it.putProperty(PacketProperty.AUTHENTICATION_DATA, authData)
            }
    
            dataBuffer = BufferUtils.prepareBuffer(512) {
                it.put(AuthenticateReasonCode.CONTINUE_AUTHENTICATION.value)
                it.putMbi(propertiesBuffer.limit())
                it.put(propertiesBuffer)
            }
        
            packet = new AuthenticateInPacket(0b1111_0000 as byte)
            result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        
        then:
            result
            packet.getReasonCode() == AuthenticateReasonCode.CONTINUE_AUTHENTICATION
            packet.getAuthenticationMethod() == authMethod
            packet.getAuthenticationData() == authData
            packet.getReason() == reasonString
            packet.getUserProperties() == userProperties
        when:
            
            propertiesBuffer = BufferUtils.prepareBuffer(512) {
                it.putProperty(PacketProperty.AUTHENTICATION_METHOD, authMethod)
                it.putProperty(PacketProperty.AUTHENTICATION_DATA, authData)
            }
    
            dataBuffer = BufferUtils.prepareBuffer(512) {
                it.put(AuthenticateReasonCode.CONTINUE_AUTHENTICATION.value)
                it.putMbi(propertiesBuffer.limit())
                it.put(propertiesBuffer)
            }
            
            packet = new AuthenticateInPacket(0b1111_0000 as byte)
            result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.getReasonCode() == AuthenticateReasonCode.CONTINUE_AUTHENTICATION
            packet.getAuthenticationMethod() == authMethod
            packet.getAuthenticationData() == authData
            packet.getReason() == ""
            packet.getUserProperties() == Array.empty()
    }
}
