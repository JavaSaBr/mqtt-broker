package com.ss.mqtt.broker.test.network.in

import com.ss.mqtt.broker.exception.MalformedPacketMqttException
import com.ss.mqtt.broker.model.MqttVersion
import com.ss.mqtt.broker.model.PacketProperty
import com.ss.mqtt.broker.network.packet.in.ConnectInPacket
import com.ss.rlib.common.util.ArrayUtils
import com.ss.rlib.common.util.BufferUtils

class ConnectInPacketTest extends BaseInPacketTest {
    
    def "should read packet correctly as mqtt 3.1.1"() {
        
        given:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putString("MQTT")
                it.put(4 as byte)
                it.put(0b11000010 as byte)
                it.putShort(keepAlive as short)
                it.putString(clientId)
                it.putString(userName)
                it.putBytes(userPassword)
            }
        
        when:
            def packet = new ConnectInPacket(0b0001_0000 as byte)
            def result = packet.read(mqtt311Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.clientId == clientId
            packet.mqttVersion == MqttVersion.MQTT_3_1_1
            packet.password == userPassword
            packet.username == userName
            packet.willTopic == ""
            packet.willQos == 0
            packet.willPayload == ArrayUtils.EMPTY_BYTE_ARRAY
    }
    
    def "should read packet correctly as mqtt 5.0"() {
        
        given:
            
            def propertiesBuffer = BufferUtils.prepareBuffer(512) {
                it.putProperty(PacketProperty.SESSION_EXPIRY_INTERVAL, sessionExpiryInterval)
                it.putProperty(PacketProperty.RECEIVE_MAXIMUM, receiveMaximum)
                it.putProperty(PacketProperty.MAXIMUM_PACKET_SIZE, maximumPacketSize)
                it.putProperty(PacketProperty.TOPIC_ALIAS_MAXIMUM, topicAliasMaximum)
                it.putProperty(PacketProperty.REQUEST_RESPONSE_INFORMATION, requestResponseInformation ? 1 : 0)
                it.putProperty(PacketProperty.REQUEST_PROBLEM_INFORMATION, requestProblemInformation ? 1 : 0)
                it.putProperty(PacketProperty.AUTHENTICATION_METHOD, authMethod)
                it.putProperty(PacketProperty.AUTHENTICATION_DATA, authData)
                it.putProperty(PacketProperty.USER_PROPERTY, userProperties)
            }
    
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putString("MQTT")
                it.put(5 as byte)
                it.put(0b11000010 as byte)
                it.putShort(keepAlive as short)
                it.putMbi(propertiesBuffer.limit())
                it.put(propertiesBuffer)
                it.putString(clientId)
                it.putString(userName)
                it.putBytes(userPassword)
            }
    
        when:
            def packet = new ConnectInPacket(0b0001_0000 as byte)
            def result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.keepAlive == keepAlive
            packet.authenticationMethod == authMethod
            packet.authenticationData == authData
            packet.clientId == clientId
            packet.mqttVersion == MqttVersion.MQTT_5
            packet.maximumPacketSize == maximumPacketSize
            packet.password == userPassword
            packet.username == userName
            packet.topicAliasMaximum == topicAliasMaximum
            packet.sessionExpiryInterval == sessionExpiryInterval
            packet.receiveMax == receiveMaximum
            packet.willTopic == ""
            packet.willQos == 0
            packet.willPayload == ArrayUtils.EMPTY_BYTE_ARRAY
            packet.userProperties == userProperties
    }
    
    def "should not read packet correctly with invalid UTF8 strings"(byte[] stringBytes) {
        
        given:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putString("MQTT")
                it.put(5 as byte)
                it.put(0b11000010 as byte)
                it.putShort(keepAlive as short)
                it.putMbi(0)
                it.putBytes(stringBytes)
                it.putString(userName)
                it.putBytes(userPassword)
            }
        
        when:
            def packet = new ConnectInPacket(0b0001_0000 as byte)
            def result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            !result
            packet.exception instanceof MalformedPacketMqttException
        where:
            stringBytes << [
                // https://www.cl.cam.ac.uk/~mgk25/ucs/examples/UTF-8-test.txt
                [0xF4, 0x90, 0x80, 0x80] as byte[],
                [0xFE, 0xFE, 0xFF, 0xFF] as byte[],
                [0xFC, 0x80, 0x80, 0x80, 0x80, 0xAF] as byte[],
                [0xED, 0xAF, 0xBF, 0xED, 0xBF, 0xBF] as byte[],
            ]
    }
}
