package com.ss.mqtt.broker.test.network

import com.ss.mqtt.broker.model.MqttVersion
import com.ss.mqtt.broker.model.StringPair
import com.ss.mqtt.broker.network.MqttConnection
import com.ss.rlib.common.util.array.Array
import com.ss.rlib.common.util.array.ArrayFactory
import com.ss.rlib.common.util.array.IntegerArray
import spock.lang.Shared
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class InPacketTest extends Specification {
    
    public static final clientId = "testClientId"
    public static final packetId = 1234 as short
    public static final userName = "testUser"
    public static final userPassword = "testPassword"
    public static final keepAlive = 120
    public static final sessionExpiryInterval = 300
    public static final messageExpiryInterval = 60
    public static final topicAlias = 252
    public static final receiveMaximum = 4096
    public static final maximumPacketSize = 1024
    public static final topicAliasMaximum = 32
    public static final subscriptionId = 637
    public static final subscriptionId2 = 623
    public static final requestResponseInformation = true
    public static final requestProblemInformation = true
    public static final authMethod = "testAuthMethod"
    public static final authData = "testAuthData".getBytes(StandardCharsets.UTF_8)
    public static final reasonString = "reasonString"
    public static final publishTopic = "publish/Topic"
    public static final responseTopic = "response/Topic"
    public static final topicFilter = "topic/Filter"
    public static final topicFilter2 = "topic/Filter2"
    public static final serverReference = "serverReference"
    public static final contentType = "application/json"
    public static final userProperties = ArrayFactory.asArray(
        new StringPair("key1", "val1"),
        new StringPair("key2", "val2"),
        new StringPair("key3", "val3"),
    )
    public static final subscriptionIds = IntegerArray.of(subscriptionId, subscriptionId2)
    public static final topicFilters = Array.of(topicFilter, topicFilter2)
    public static final publishPayload = "publishPayload".getBytes(StandardCharsets.UTF_8)
    public static final correlationData = "correlationData".getBytes(StandardCharsets.UTF_8)
    
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
