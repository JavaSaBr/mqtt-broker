package com.ss.mqtt.broker.test.network

import com.ss.mqtt.broker.model.PacketProperty
import com.ss.mqtt.broker.network.packet.in.UnsubscribeInPacket
import com.ss.rlib.common.util.BufferUtils
import com.ss.rlib.common.util.array.Array

class UnsubscribeInPacketTest extends InPacketTest {
    
    def "should read packet correctly as mqtt 3.1.1"() {
        
        given:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putShort(packetId)
                it.putString(topicFilter)
                it.putString(topicFilter2)
            }
        
        when:
            def packet = new UnsubscribeInPacket(0b1011_0000 as byte)
            def result = packet.read(mqtt311Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.getTopicFilters().size() == 2
            packet.getTopicFilters().get(0) == topicFilter
            packet.getTopicFilters().get(1) == topicFilter2
            packet.getPacketId() == packetId
            packet.getUserProperties() == Array.empty()
    }
    
    def "should read packet correctly as mqtt 5.0"() {
        
        given:
            
            def propertiesBuffer = BufferUtils.prepareBuffer(512) {
                it.putProperty(PacketProperty.USER_PROPERTY, userProperties)
            }
    
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putShort(packetId)
                it.putMbi(propertiesBuffer.limit())
                it.put(propertiesBuffer)
                it.putString(topicFilter)
                it.putString(topicFilter2)
            }
    
        when:
            def packet = new UnsubscribeInPacket(0b1011_0000 as byte)
            def result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.getTopicFilters().size() == 2
            packet.getTopicFilters().get(0) == topicFilter
            packet.getTopicFilters().get(1) == topicFilter2
            packet.getPacketId() == packetId
            packet.getUserProperties() == userProperties
        when:
    
            dataBuffer = BufferUtils.prepareBuffer(512) {
                it.putShort(packetId)
                it.putMbi(0)
                it.putString(topicFilter)
                it.putString(topicFilter2)
            }
        
            packet = new UnsubscribeInPacket(0b1011_0000 as byte)
            result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        then:
            result
            packet.getTopicFilters().size() == 2
            packet.getTopicFilters().get(0) == topicFilter
            packet.getTopicFilters().get(1) == topicFilter2
            packet.getPacketId() == packetId
            packet.getUserProperties() == Array.empty()
    }
}
