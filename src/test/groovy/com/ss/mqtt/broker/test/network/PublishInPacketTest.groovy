package com.ss.mqtt.broker.test.network

import com.ss.mqtt.broker.model.MqttPropertyConstants
import com.ss.mqtt.broker.model.PacketProperty
import com.ss.mqtt.broker.model.QoS
import com.ss.mqtt.broker.network.packet.in.PublishInPacket
import com.ss.rlib.common.util.ArrayUtils
import com.ss.rlib.common.util.BufferUtils
import com.ss.rlib.common.util.array.Array
import com.ss.rlib.common.util.array.IntegerArray

class PublishInPacketTest extends InPacketTest {
    
    def "should read packet correctly as mqtt 3.1.1"() {
        
        given:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putString(publishTopic)
                it.putShort(packetId)
                it.put(publishPayload)
            }
        
        when:
            def packet = new PublishInPacket(0b0110_0011 as byte)
            def result = packet.read(mqtt311Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.getQos() == QoS.AT_LEAST_ONCE_DELIVERY
            !packet.isDuplicate()
            packet.isRetained()
            packet.getResponseTopic() == ""
            packet.getSubscriptionIds() == IntegerArray.EMPTY
            packet.getContentType() == ""
            packet.getCorrelationData() == ArrayUtils.EMPTY_BYTE_ARRAY
            packet.getPayload() == publishPayload
            packet.getPacketId() == packetId
            packet.getUserProperties() == Array.empty()
            packet.getMessageExpiryInterval() == MqttPropertyConstants.MESSAGE_EXPIRY_INTERVAL_DEFAULT
            packet.getTopicAlias() == MqttPropertyConstants.TOPIC_ALIAS_DEFAULT
            packet.isPayloadFormatIndicator() == MqttPropertyConstants.PAYLOAD_FORMAT_INDICATOR_DEFAULT
    }
    
    def "should read packet correctly as mqtt 5.0"() {
        
        given:
            
            def propertiesBuffer = BufferUtils.prepareBuffer(512) {
                it.putProperty(PacketProperty.PAYLOAD_FORMAT_INDICATOR, 1)
                it.putProperty(PacketProperty.MESSAGE_EXPIRY_INTERVAL, messageExpiryInterval)
                it.putProperty(PacketProperty.TOPIC_ALIAS, topicAlias)
                it.putProperty(PacketProperty.RESPONSE_TOPIC, responseTopic)
                it.putProperty(PacketProperty.CORRELATION_DATA, correlationData)
                it.putProperty(PacketProperty.USER_PROPERTY, userProperties)
                it.putProperty(PacketProperty.SUBSCRIPTION_IDENTIFIER, subscriptionIds)
                it.putProperty(PacketProperty.CONTENT_TYPE, contentType)
            }
    
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putString(publishTopic)
                it.putShort(packetId)
                it.putMbi(propertiesBuffer.limit())
                it.put(propertiesBuffer)
                it.put(publishPayload)
            }
    
        when:
            def packet = new PublishInPacket(0b0110_0011 as byte)
            def result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.getQos() == QoS.AT_LEAST_ONCE_DELIVERY
            !packet.isDuplicate()
            packet.isRetained()
            packet.getResponseTopic() == responseTopic
            packet.getSubscriptionIds() == subscriptionIds
            packet.getContentType() == contentType
            packet.getCorrelationData() == correlationData
            packet.getPayload() == publishPayload
            packet.getPacketId() == packetId
            packet.getUserProperties() == userProperties
            packet.getMessageExpiryInterval() == messageExpiryInterval
            packet.getTopicAlias() == topicAlias
            packet.isPayloadFormatIndicator()
        when:
    
            dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putString(publishTopic)
                it.putShort(packetId)
                it.putMbi(0)
                it.put(publishPayload)
            }
        
            packet = new PublishInPacket(0b0110_0011 as byte)
            result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.getQos() == QoS.AT_LEAST_ONCE_DELIVERY
            !packet.isDuplicate()
            packet.isRetained()
            packet.getResponseTopic() == ""
            packet.getSubscriptionIds() == IntegerArray.EMPTY
            packet.getContentType() == ""
            packet.getCorrelationData() == ArrayUtils.EMPTY_BYTE_ARRAY
            packet.getPayload() == publishPayload
            packet.getPacketId() == packetId
            packet.getUserProperties() == Array.empty()
            packet.getMessageExpiryInterval() == MqttPropertyConstants.MESSAGE_EXPIRY_INTERVAL_DEFAULT
            packet.getTopicAlias() == MqttPropertyConstants.TOPIC_ALIAS_DEFAULT
            packet.isPayloadFormatIndicator() == MqttPropertyConstants.PAYLOAD_FORMAT_INDICATOR_DEFAULT
    }
}
