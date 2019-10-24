package com.ss.mqtt.broker.test.network

import com.ss.mqtt.broker.model.MqttVersion
import com.ss.mqtt.broker.model.StringPair
import com.ss.mqtt.broker.network.MqttConnection
import com.ss.rlib.common.util.array.ArrayFactory
import spock.lang.Shared
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class InPacketTest extends Specification {
  
    public static final authMethod = "testAuthMethod"
    public static final authData = "testAuthData".getBytes(StandardCharsets.UTF_8)
    public static final reasonString = "reasonString"
    public static final userProperties = ArrayFactory.asArray(
        new StringPair("key1", "val1"),
        new StringPair("key2", "val2"),
        new StringPair("key3", "val3"),
    )
    
    @Shared
    MqttConnection mqtt5Connection = Stub(MqttConnection) {
        isSupported(MqttVersion.MQTT_5) >> true
    }
    
    @Shared
    MqttConnection mqtt311Connection = Stub(MqttConnection) {
        isSupported(MqttVersion.MQTT_3_1_1) >> true
        isSupported(MqttVersion.MQTT_5) >> false
    }
}
