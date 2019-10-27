package com.ss.mqtt.broker.test.network

import com.ss.mqtt.broker.model.MqttPropertyConstants
import com.ss.mqtt.broker.model.PacketProperty
import com.ss.mqtt.broker.model.QoS
import com.ss.mqtt.broker.model.SubscribeRetainHandling
import com.ss.mqtt.broker.network.packet.in.SubscribeInPacket
import com.ss.rlib.common.util.BufferUtils
import com.ss.rlib.common.util.array.Array

class SubscribeInPacketTest extends InPacketTest {
    
    def "should read packet correctly as mqtt 3.1.1"() {
        
        given:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putShort(packetId)
                it.putString(topicFilter)
                it.put(0b0000_0001 as byte)
                it.putString(topicFilter2)
                it.put(0b0000_0010 as byte)
            }
        
        when:
            def packet = new SubscribeInPacket(0b1000_0000 as byte)
            def result = packet.read(mqtt311Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.getTopicFilters().size() == 2
            packet.getTopicFilters().get(0).getQos() == QoS.AT_LEAST_ONCE_DELIVERY
            packet.getTopicFilters().get(0).getTopicFilter() == topicFilter
            packet.getTopicFilters().get(0).isNoLocal()
            !packet.getTopicFilters().get(0).isRetainAsPublished()
            packet.getTopicFilters().get(0).getRetainHandling() == SubscribeRetainHandling.SEND_AT_THE_TIME_OF_SUBSCRIBE
            packet.getTopicFilters().get(1).getQos() == QoS.EXACTLY_ONCE_DELIVERY
            packet.getTopicFilters().get(1).getTopicFilter() == topicFilter2
            packet.getTopicFilters().get(1).isNoLocal()
            !packet.getTopicFilters().get(1).isRetainAsPublished()
            packet.getTopicFilters().get(1).getRetainHandling() == SubscribeRetainHandling.SEND_AT_THE_TIME_OF_SUBSCRIBE
            packet.getPacketId() == packetId
            packet.getUserProperties() == Array.empty()
            packet.getSubscriptionId() == MqttPropertyConstants.SUBSCRIPTION_ID_NOT_DEFINED
    }
    
    def "should read packet correctly as mqtt 5.0"() {
        
        given:
            
            def propertiesBuffer = BufferUtils.prepareBuffer(512) {
                it.putProperty(PacketProperty.SUBSCRIPTION_IDENTIFIER, subscriptionId)
                it.putProperty(PacketProperty.USER_PROPERTY, userProperties)
            }
    
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putShort(packetId)
                it.putMbi(propertiesBuffer.limit())
                it.put(propertiesBuffer)
                it.putString(topicFilter)
                it.put(0b0000_1001 as byte)
                it.putString(topicFilter2)
                it.put(0b0001_0110 as byte)
            }
    
        when:
            def packet = new SubscribeInPacket(0b0110_0000 as byte)
            def result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.getTopicFilters().size() == 2
            packet.getTopicFilters().get(0).getQos() == QoS.AT_LEAST_ONCE_DELIVERY
            packet.getTopicFilters().get(0).getTopicFilter() == topicFilter
            !packet.getTopicFilters().get(0).isNoLocal()
            packet.getTopicFilters().get(0).isRetainAsPublished()
            packet.getTopicFilters().get(0).getRetainHandling() == SubscribeRetainHandling.SEND_AT_THE_TIME_OF_SUBSCRIBE
            packet.getTopicFilters().get(1).getQos() == QoS.EXACTLY_ONCE_DELIVERY
            packet.getTopicFilters().get(1).getTopicFilter() == topicFilter2
            packet.getTopicFilters().get(1).isNoLocal()
            !packet.getTopicFilters().get(1).isRetainAsPublished()
            packet.getTopicFilters().get(1).getRetainHandling() == SubscribeRetainHandling.SEND_AT_SUBSCRIBE_ONLY_IF_THE_SUBSCRIPTION_DOES_NOT_CURRENTLY_EXIST
            packet.getPacketId() == packetId
            packet.getUserProperties() == userProperties
            packet.getSubscriptionId() == subscriptionId
        when:
    
            dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putShort(packetId)
                it.putMbi(0)
                it.putString(topicFilter)
                it.put(0b0000_0001 as byte)
                it.putString(topicFilter2)
                it.put(0b0000_0010 as byte)
            }
        
            packet = new SubscribeInPacket(0b0110_0000 as byte)
            result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.getTopicFilters().size() == 2
            packet.getTopicFilters().get(0).getQos() == QoS.AT_LEAST_ONCE_DELIVERY
            packet.getTopicFilters().get(0).getTopicFilter() == topicFilter
            !packet.getTopicFilters().get(0).isNoLocal()
            !packet.getTopicFilters().get(0).isRetainAsPublished()
            packet.getTopicFilters().get(0).getRetainHandling() == SubscribeRetainHandling.SEND_AT_THE_TIME_OF_SUBSCRIBE
            packet.getTopicFilters().get(1).getQos() == QoS.EXACTLY_ONCE_DELIVERY
            packet.getTopicFilters().get(1).getTopicFilter() == topicFilter2
            !packet.getTopicFilters().get(1).isNoLocal()
            !packet.getTopicFilters().get(1).isRetainAsPublished()
            packet.getTopicFilters().get(1).getRetainHandling() == SubscribeRetainHandling.SEND_AT_THE_TIME_OF_SUBSCRIBE
            packet.getPacketId() == packetId
            packet.getUserProperties() == Array.empty()
            packet.getSubscriptionId() == MqttPropertyConstants.SUBSCRIPTION_ID_NOT_DEFINED
    }
}
