package com.ss.mqtt.broker.test.network

import com.ss.mqtt.broker.model.StringPair
import com.ss.mqtt.broker.network.MqttClient
import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket
import com.ss.rlib.common.util.array.ArrayFactory
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class InPacketTest extends Specification {
  
    static final authMethod = "testAuthMethod"
    static final authData = "testAuthData".getBytes(StandardCharsets.UTF_8)
    static final reasonString = "reasonString"
    static final userProperties = ArrayFactory.asArray(
        new StringPair("key1", "val1"),
        new StringPair("key2", "val2"),
        new StringPair("key3", "val3"),
    )
    
    @Shared
    MqttWritablePacket writer = new MqttWritablePacket(Stub(MqttClient)) {
       
        @Override
        protected void writeImpl(ByteBuffer buffer) {
        }
    }
}
