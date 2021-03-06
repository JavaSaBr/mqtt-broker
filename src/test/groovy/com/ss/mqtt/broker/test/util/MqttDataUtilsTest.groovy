package com.ss.mqtt.broker.test.util

import com.ss.mqtt.broker.test.UnitSpecification
import com.ss.mqtt.broker.util.MqttDataUtils

import java.nio.ByteBuffer

class MqttDataUtilsTest extends UnitSpecification {
    
    def "should write integer to MQTT multi byte integer successful"(int value, int expectedBytes) {
        given:
            def buffer = ByteBuffer.allocate(4)
        when:
            MqttDataUtils.writeMbi(value, buffer)
        then:
            buffer.position() == expectedBytes
        where:
            value << [10, 1000, 40_000, 500_000, 1_000_000, MqttDataUtils.MAX_MBI]
            expectedBytes << [1, 2, 3, 3, 3, 4]
    }
    
    def "should failed writing too big integer to MQTT multi byte integer"(int value) {
        given:
            def buffer = ByteBuffer.allocate(10)
        when:
            MqttDataUtils.writeMbi(value, buffer)
        then:
            thrown IllegalArgumentException
        where:
            value << [1_000_000_000, 2_000_000_000, 5_000_000_000]
    }
    
    def "should read integer from MQTT multi byte integer successful"(int value) {
        given:
            def buffer = ByteBuffer.allocate(5)
            MqttDataUtils.writeMbi(value, buffer).flip()
        when:
            def read = MqttDataUtils.readMbi(buffer)
        then:
            read == value
        where:
            value << [10, 1000, 40_000, 500_000, 1_000_000, MqttDataUtils.MAX_MBI]
    }
    
    def "should failed reading integer from MQTT multi byte integer"(int value, int position) {
        given:
            
            def buffer = ByteBuffer.allocate(10)
            
            MqttDataUtils.writeMbi(value, buffer)
                .position(position)
                .flip()
        
        when:
            def read = MqttDataUtils.readMbi(buffer)
        then:
            read == -1
        where:
            value << [10, 1000, 40_000, 500_000, 1_000_000, MqttDataUtils.MAX_MBI]
            position << [0, 0, 1, 1, 2, 2]
    }
}
