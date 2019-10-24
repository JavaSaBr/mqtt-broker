package com.ss.mqtt.broker.test.network

import com.ss.mqtt.broker.model.AuthenticateReasonCode
import com.ss.mqtt.broker.model.PacketProperty
import com.ss.mqtt.broker.network.packet.in.AuthenticateInPacket
import com.ss.rlib.common.util.BufferUtils

class AuthenticateInPacketTest extends InPacketTest {

    def "should read packet correctly as mqtt 5.0"() {
        
        given:
            
            def propertiesBuffer = BufferUtils.prepareBuffer(512) {
                it.put(PacketProperty.AUTHENTICATION_METHOD, authMethod)
                it.put(PacketProperty.AUTHENTICATION_DATA, authData)
                it.put(PacketProperty.REASON_STRING, reasonString)
                it.put(PacketProperty.USER_PROPERTY, userProperties)
            }
    
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                it.put(AuthenticateReasonCode.SUCCESS.value)
                it.putMbi(propertiesBuffer.limit())
                it.put(propertiesBuffer)
            }
    
        when:
            def packet = new AuthenticateInPacket(0b11110000 as byte)
            def result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.getReasonCode() == AuthenticateReasonCode.SUCCESS
            packet.getAuthenticateMethod() == authMethod
            packet.getAuthenticateData() == authData
            packet.getReason() == reasonString
            packet.getUserProperties() == userProperties
    }
}
