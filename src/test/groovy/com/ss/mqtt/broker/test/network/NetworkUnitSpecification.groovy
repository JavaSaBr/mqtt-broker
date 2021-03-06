package com.ss.mqtt.broker.test.network

import com.ss.mqtt.broker.config.MqttConnectionConfig
import com.ss.mqtt.broker.model.MqttVersion
import com.ss.mqtt.broker.model.QoS
import com.ss.mqtt.broker.model.SubscribeRetainHandling
import com.ss.mqtt.broker.model.SubscribeTopicFilter
import com.ss.mqtt.broker.model.data.type.StringPair
import com.ss.mqtt.broker.model.reason.code.SubscribeAckReasonCode
import com.ss.mqtt.broker.model.reason.code.UnsubscribeAckReasonCode
import com.ss.mqtt.broker.network.MqttConnection
import com.ss.mqtt.broker.network.client.MqttClient
import com.ss.mqtt.broker.test.UnitSpecification
import com.ss.rlib.common.util.array.Array
import com.ss.rlib.common.util.array.ArrayFactory
import com.ss.rlib.common.util.array.IntegerArray
import spock.lang.Shared

import java.nio.charset.StandardCharsets

import static com.ss.mqtt.broker.util.TopicUtils.buildTopicFilter
import static com.ss.mqtt.broker.util.TopicUtils.buildTopicName

class NetworkUnitSpecification extends UnitSpecification {
    
    public static final keepAliveEnabled = true
    public static final sessionsEnabled = true
    public static final retainAvailable = true
    public static final sharedSubscriptionAvailable = true
    public static final wildcardSubscriptionAvailable = true
    public static final subscriptionIdAvailable = true
    
    public static final maxQos = QoS.AT_MOST_ONCE
    public static final sessionPresent = true
    public static final cleanStart = false
    public static final willRetain = false
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
    public static final publishTopic = buildTopicName("publish/Topic")
    public static final responseTopic = "response/Topic"
    public static final topicFilter = "topic/Filter"
    public static final topicFilter1Obj311 = new SubscribeTopicFilter(buildTopicFilter(topicFilter), QoS.AT_LEAST_ONCE)
    public static final topicFilter1Obj5 = new SubscribeTopicFilter(
        buildTopicFilter(topicFilter),
        QoS.AT_LEAST_ONCE,
        SubscribeRetainHandling.DO_NOT_SEND,
        true,
        false,
    )
    public static final topicFilter2 = "topic/Filter2"
    public static final topicFilter2Obj311 = new SubscribeTopicFilter(buildTopicFilter(topicFilter2), QoS.EXACTLY_ONCE)
    public static final topicFilter2Obj5 = new SubscribeTopicFilter(
        buildTopicFilter(topicFilter2),
        QoS.EXACTLY_ONCE,
        SubscribeRetainHandling.DO_NOT_SEND,
        true,
        false,
    )
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
    public static final topicFiltersObj311 = Array.of(topicFilter1Obj311, topicFilter2Obj311)
    public static final topicFiltersObj5 = Array.of(topicFilter1Obj5, topicFilter2Obj5)
    public static final publishPayload = "publishPayload".getBytes(StandardCharsets.UTF_8)
    public static final correlationData = "correlationData".getBytes(StandardCharsets.UTF_8)
    
    @Shared
    MqttClient defaultMqttClient = defaultMqttClient()
    
    @Shared
    MqttConnectionConfig mqttConnectionConfig = defaultMqttConnectionConfig()
    
    @Shared
    MqttConnection mqtt5Connection = defaultMqttConnection(MqttVersion.MQTT_5)
    
    @Shared
    MqttConnection mqtt311Connection = defaultMqttConnection(MqttVersion.MQTT_3_1_1)
    
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
    
    MqttConnectionConfig defaultMqttConnectionConfig() {
        return mqttConnectionConfig(
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
    }
    
    
    MqttConnection defaultMqttConnection(MqttVersion mqttVersion)  {
      return mqttConnection(
          mqttVersion,
          mqttConnectionConfig,
          sessionExpiryInterval,
          receiveMaximum,
          maximumPacketSize,
          clientId,
          serverKeepAlive,
          topicAliasMaximum
      )
    }
    
    
    static MqttConnectionConfig mqttConnectionConfig(
        QoS maxQos,
        int maximumPacketSize,
        int serverKeepAlive,
        int receiveMaximum,
        int topicAliasMaximum,
        long sessionExpiryInterval,
        boolean keepAliveEnabled,
        boolean sessionsEnabled,
        boolean retainAvailable,
        boolean wildcardSubscriptionAvailable,
        boolean subscriptionIdAvailable,
        boolean sharedSubscriptionAvailable
    
    ) {
        return new MqttConnectionConfig(
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
    }
    
    MqttConnection mqttConnection(
        MqttVersion mqttVersion,
        MqttConnectionConfig mqttConnectionConfig,
        long sessionExpiryInterval,
        int receiveMaximum,
        int maximumPacketSize,
        String clientId,
        int serverKeepAlive,
        int topicAliasMaximum
    ) {
        return Stub(MqttConnection) {
            isSupported(_ as MqttVersion) >> { MqttVersion version -> mqttVersion >= version }
            getConfig() >> mqttConnectionConfig
            getClient() >> mqttClient(
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
    
    MqttClient mqttClient(
        MqttConnectionConfig mqttConnectionConfig,
        long sessionExpiryInterval,
        int receiveMaximum,
        int maximumPacketSize,
        String clientId,
        int serverKeepAlive,
        int topicAliasMaximum
    ) {
        return Stub(MqttClient.UnsafeMqttClient) {
            getConnectionConfig() >> mqttConnectionConfig
            getSessionExpiryInterval() >> sessionExpiryInterval
            getReceiveMax() >> receiveMaximum
            getMaximumPacketSize() >> maximumPacketSize
            getClientId() >> clientId
            getKeepAlive() >> serverKeepAlive
            getTopicAliasMaximum() >> topicAliasMaximum
        }
    }
}
