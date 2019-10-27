package com.ss.mqtt.broker.test.network

import com.ss.mqtt.broker.model.MqttVersion
import com.ss.mqtt.broker.model.PacketProperty
import com.ss.mqtt.broker.network.packet.in.ConnectInPacket
import com.ss.rlib.common.util.ArrayUtils
import com.ss.rlib.common.util.BufferUtils

class ConnectInPacketTest extends InPacketTest {
    
    def "should read packet correctly as mqtt 3.1.1"() {
        
        given:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putString("MQTT")
                it.put(4 as byte)
                it.put(0b11000010 as byte)
                it.putShort(keepAlive as short)
                it.putString(clientId)
                it.putString(userPassword)
                it.putString(userName)
            }
        
        when:
            def packet = new ConnectInPacket(0b0001_0000 as byte)
            def result = packet.read(mqtt311Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.getClientId() == clientId
            packet.getMqttVersion() == MqttVersion.MQTT_3_1_1
            packet.getPassword() == userPassword
            packet.getUsername() == userName
            packet.getWillTopic() == ""
            packet.getWillQos() == 0
            packet.getWillPayload() == ArrayUtils.EMPTY_BYTE_ARRAY
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
                it.putString(userPassword)
                it.putString(userName)
            }
    
        when:
            def packet = new ConnectInPacket(0b0001_0000 as byte)
            def result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.getKeepAlive() == keepAlive
            packet.getAuthenticationMethod() == authMethod
            packet.getAuthenticationData() == authData
            packet.getClientId() == clientId
            packet.getMqttVersion() == MqttVersion.MQTT_5
            packet.getMaximumPacketSize() == maximumPacketSize
            packet.getPassword() == userPassword
            packet.getUsername() == userName
            packet.getTopicAliasMaximum() == topicAliasMaximum
            packet.getSessionExpiryInterval() == sessionExpiryInterval
            packet.getReceiveMax() == receiveMaximum
            packet.getWillTopic() == ""
            packet.getWillQos() == 0
            packet.getWillPayload() == ArrayUtils.EMPTY_BYTE_ARRAY
            packet.getUserProperties() == userProperties
    }
}
