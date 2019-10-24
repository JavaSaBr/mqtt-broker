package com.ss.mqtt.broker.test.network

import com.ss.mqtt.broker.model.AuthenticateReasonCode
import com.ss.mqtt.broker.model.MqttVersion
import com.ss.mqtt.broker.model.PacketProperty
import com.ss.mqtt.broker.network.MqttConnection
import com.ss.mqtt.broker.network.packet.in.AuthenticateInPacket
import com.ss.mqtt.broker.util.MqttDataUtils

import java.nio.ByteBuffer

class AuthenticateInPacketTest extends InPacketTest {

    def "should read packet correctly as mqtt 5.0"() {
        
        given:
            
            def connection = Stub(MqttConnection) {
                isSupported(MqttVersion.MQTT_5) >> true
            }
        
            def propertiesBuffer = ByteBuffer.allocate(1024)
        
            writer.writeProperty(propertiesBuffer, PacketProperty.AUTHENTICATION_METHOD, authMethod)
            writer.writeProperty(propertiesBuffer, PacketProperty.AUTHENTICATION_DATA, authData)
            writer.writeProperty(propertiesBuffer, PacketProperty.REASON_STRING, reasonString)
            writer.writeUserProperties(propertiesBuffer, userProperties)
    
            propertiesBuffer.flip()
    
            def propertiesSize = propertiesBuffer.limit()
            def buffer = ByteBuffer.allocate(1024)
                .put(AuthenticateReasonCode.SUCCESS.value)
        
            MqttDataUtils.writeMbi(propertiesSize, buffer)
    
            buffer.put(propertiesBuffer)
            buffer.flip()
    
        when:
            def packet = new AuthenticateInPacket(0b11110000 as byte)
            def result = packet.read(connection, buffer, buffer.limit())
        then:
            result
            packet.getReasonCode() == AuthenticateReasonCode.SUCCESS
            packet.getAuthenticateMethod() == authMethod
            packet.getAuthenticateData() == authData
            packet.getReason() == reasonString
            packet.getUserProperties() == userProperties
    }
}
