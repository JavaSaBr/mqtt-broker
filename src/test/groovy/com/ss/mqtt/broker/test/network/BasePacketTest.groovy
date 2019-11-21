package com.ss.mqtt.broker.test.network

import com.ss.mqtt.broker.config.MqttConnectionConfig
import com.ss.mqtt.broker.model.MqttVersion
import com.ss.mqtt.broker.model.QoS
import com.ss.mqtt.broker.model.data.type.StringPair
import com.ss.mqtt.broker.model.reason.code.SubscribeAckReasonCode
import com.ss.mqtt.broker.model.reason.code.UnsubscribeAckReasonCode

import com.ss.mqtt.broker.network.MqttConnection
import com.ss.mqtt.broker.network.client.MqttClient
import com.ss.rlib.common.util.array.Array
import com.ss.rlib.common.util.array.ArrayFactory
import com.ss.rlib.common.util.array.IntegerArray
import spock.lang.Shared
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class BasePacketTest extends UnitSpecification {
    
    public static final keepAliveEnabled = true
    public static final sessionsEnabled = true
    public static final retainAvailable = true
    public static final sharedSubscriptionAvailable = true
    public static final wildcardSubscriptionAvailable = true
    public static final subscriptionIdAvailable = true
    
    public static final maxQos = QoS.AT_MOST_ONCE_DELIVERY
    public static final sessionPresent = true
    public static final clientId = "testClientId"
    public static final packetId = 1234 as short
    public static final userName = "testUser"
    public static final userPassword = "testPassword".getBytes(StandardCharsets.UTF_8)
    public static final keepAlive = 120
    public static final sessionExpiryInterval = 300
    public static final messageExpiryInterval = 60
    public static final topicAlias = 252
    public static final receiveMaximum = 10
    public static final maximumPacketSize = 1024
    public static final topicAliasMaximum = 32
    public static final subscriptionId = 637
    public static final subscriptionId2 = 623
    public static final serverKeepAlive = 1200
    public static final requestResponseInformation = true
    public static final requestProblemInformation = true
    public static final responseInformation = "responseInformation"
    public static final authMethod = "testAuthMethod"
    public static final authData = "testAuthData".getBytes(StandardCharsets.UTF_8)
    public static final reasonString = "reasonString"
    public static final publishTopic = "publish/Topic"
    public static final responseTopic = "response/Topic"
    public static final topicFilter = "topic/Filter"
    public static final topicFilter2 = "topic/Filter2"
    public static final serverReference = "serverReference"
    public static final contentType = "application/json"
    public static final subscribeAckReasonCodes = ArrayFactory.asArray(
        SubscribeAckReasonCode.GRANTED_QOS_1,
        SubscribeAckReasonCode.GRANTED_QOS_0,
        SubscribeAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR
    )
    public static final unsubscribeAckReasonCodes = ArrayFactory.asArray(
        UnsubscribeAckReasonCode.SUCCESS,
        UnsubscribeAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR,
        UnsubscribeAckReasonCode.UNSPECIFIED_ERROR
    )
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
    MqttConnectionConfig mqttConnectionConfig = mqttConnectionConfig(
        maxQos,
        maximumPacketSize,
        serverKeepAlive,
        receiveMaximum,
        topicAliasMaximum,
        sessionExpiryInterval,
        keepAliveEnabled,
        sessionsEnabled,
        retainAvailable,
        wildcardSubscriptionAvailable,
        subscriptionIdAvailable,
        sharedSubscriptionAvailable
    )
    
    @Shared
    MqttConnection mqtt5Connection = mqttConnection(
        MqttVersion.MQTT_5,
        mqttConnectionConfig,
        sessionExpiryInterval,
        receiveMaximum,
        maximumPacketSize,
        clientId,
        serverKeepAlive,
        topicAliasMaximum
    )
    
    @Shared
    MqttConnection mqtt311Connection = mqttConnection(
        MqttVersion.MQTT_3_1_1,
        mqttConnectionConfig,
        sessionExpiryInterval,
        receiveMaximum,
        maximumPacketSize,
        clientId,
        serverKeepAlive,
        topicAliasMaximum
    )
    
    MqttClient defaultMqttClient()  {
        return mqttClient(
            mqttConnectionConfig,
            sessionExpiryInterval,
            receiveMaximum,
            maximumPacketSize,
            clientId,
            serverKeepAlive,
            topicAliasMaximum
        )
    }
        
}
