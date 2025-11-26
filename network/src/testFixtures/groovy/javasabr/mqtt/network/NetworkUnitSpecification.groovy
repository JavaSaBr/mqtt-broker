package javasabr.mqtt.network


import javasabr.mqtt.model.MqttClientConnectionConfig
import javasabr.mqtt.model.MqttServerConnectionConfig
import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.SubscribeRetainHandling
import javasabr.mqtt.model.data.type.StringPair
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode
import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode
import javasabr.mqtt.model.subscription.Subscription
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.mqtt.model.topic.TopicName
import javasabr.mqtt.test.support.UnitSpecification
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.IntArray
import spock.lang.Shared

import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger

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
  public static final mqtt311ClientId = "testMqtt311ClientId"
  public static final mqtt5ClientId = "testMqtt5ClientId"
  public static final messageId = 1234 as short
  public static final userName = "testUser"
  public static final userPassword = "testPassword".getBytes(StandardCharsets.UTF_8)
  public static final keepAlive = 120
  public static final sessionExpiryInterval = 300
  public static final messageExpiryInterval = 60
  public static final topicAlias = 252
  public static final receiveMaxPublishes = 10
  public static final maxMessageSize = 1024
  public static final maxStringLength = 256
  public static final maxBinarySize = 1024
  public static final maxTopicLevels = 10
  public static final topicAliasMaxValue = 32
  public static final subscriptionId = 637
  public static final subscriptionId2 = 623
  public static final serverKeepAlive = 1200
  public static final requestResponseInformation = true
  public static final requestProblemInformation = true
  public static final responseInformation = "responseInformation"
  public static final authMethod = "testAuthMethod"
  public static final authData = "testAuthData".getBytes(StandardCharsets.UTF_8)
  public static final reasonString = "reasonString"
  public static final publishTopic = TopicName.valueOf("publish/Topic")
  public static final responseTopic = TopicName.valueOf("response/Topic")
  public static final topicFilter = "topic/Filter"
  public static final topicFilter1Obj311 = Subscription.minimal(TopicFilter.valueOf(topicFilter), QoS.AT_LEAST_ONCE)
  public static final topicFilter1Obj5 = new Subscription(
      TopicFilter.valueOf(topicFilter),
      15,
      QoS.AT_LEAST_ONCE,
      SubscribeRetainHandling.DO_NOT_SEND,
      true,
      false)

  public static final topicFilter2 = "topic/Filter2"
  public static final topicFilter2Obj311 = Subscription.minimal(TopicFilter.valueOf(topicFilter2), QoS.EXACTLY_ONCE)
  public static final topicFilter2Obj5 = new Subscription(
      TopicFilter.valueOf(topicFilter2),
      15,
      QoS.EXACTLY_ONCE,
      SubscribeRetainHandling.DO_NOT_SEND,
      true,
      false)
  public static final topicFilter3 = "topic/Filter3"
  public static final topicFilter4 = "topic/Filter4"

  public static final serverReference = "serverReference"
  public static final contentType = "application/json"
  public static final subscribeAckReasonCodes = Array.typed(
      SubscribeAckReasonCode,
      SubscribeAckReasonCode.GRANTED_QOS_1,
      SubscribeAckReasonCode.GRANTED_QOS_0,
      SubscribeAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR)

  public static final unsubscribeAckReasonCodes = Array.typed(
      UnsubscribeAckReasonCode,
      UnsubscribeAckReasonCode.SUCCESS,
      UnsubscribeAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR,
      UnsubscribeAckReasonCode.UNSPECIFIED_ERROR)

  public static final userProperties = Array.typed(
      StringPair,
      new StringPair("key1", "val1"),
      new StringPair("key2", "val2"),
      new StringPair("key3", "val3"))

  public static final subscriptionIds = IntArray.of(subscriptionId, subscriptionId2)
  public static final topicFilters = Array.of(topicFilter, topicFilter2)
  public static final subscriptionsObj311 = Array.of(topicFilter1Obj311, topicFilter2Obj311)
  public static final topicFiltersObj5 = Array.of(topicFilter1Obj5, topicFilter2Obj5)
  public static final publishPayload = "publishPayload".getBytes(StandardCharsets.UTF_8)
  public static final correlationData = "correlationData".getBytes(StandardCharsets.UTF_8)
  public static final clientIdGenerator = new AtomicInteger(1)

  @Shared
  MqttServerConnectionConfig defaultServerConnectionConfig = defaultServerConnectionConfig()

  @Shared
  MqttClient defaultMqtt311Client = mqttClient(defaultMqtt311ClientConnectionConfig(), mqtt311ClientId)

  @Shared
  MqttClient defaultMqtt5Client = mqttClient(defaultMqtt5ClientConnectionConfig(), mqtt5ClientId)

  @Shared
  MqttConnection defaultMqtt5Connection = mqtt5Connection();

  @Shared
  MqttConnection defaultMqtt311Connection = mqtt311Connection();

  MqttServerConnectionConfig defaultServerConnectionConfig() {
    return serverConnectionConfig(
        maxQos,
        maxMessageSize,
        maxStringLength,
        maxBinarySize,
        maxTopicLevels,
        serverKeepAlive,
        receiveMaxPublishes,
        topicAliasMaxValue,
        sessionExpiryInterval,
        keepAliveEnabled,
        sessionsEnabled,
        retainAvailable,
        wildcardSubscriptionAvailable,
        subscriptionIdAvailable,
        sharedSubscriptionAvailable)
  }

  MqttClientConnectionConfig defaultMqtt311ClientConnectionConfig() {
    return clientConnectionConfig(
        defaultServerConnectionConfig,
        maxQos,
        MqttVersion.MQTT_3_1_1,
        sessionExpiryInterval,
        receiveMaxPublishes,
        maxMessageSize,
        topicAliasMaxValue,
        keepAlive,
        false,
        false)
  }

  MqttClientConnectionConfig defaultMqtt5ClientConnectionConfig() {
    return clientConnectionConfig(
        defaultServerConnectionConfig,
        maxQos,
        MqttVersion.MQTT_5,
        sessionExpiryInterval,
        receiveMaxPublishes,
        maxMessageSize,
        topicAliasMaxValue,
        keepAlive,
        false,
        false)
  }

  MqttConnection mqtt311Connection() {
    return mqttConnection(
        defaultServerConnectionConfig(),
        defaultMqtt311ClientConnectionConfig(),
        mqtt311ClientId)
  }

  MqttConnection mqtt5Connection() {
    return mqttConnection(
        defaultServerConnectionConfig(),
        defaultMqtt5ClientConnectionConfig(),
        mqtt5ClientId)
  }

  static MqttServerConnectionConfig serverConnectionConfig(
      QoS maxQos,
      int maxPacketSize,
      int maxStringLength,
      int maxBinarySize,
      int maxTopicLevels,
      int serverKeepAlive,
      int receiveMaxPublishes,
      int topicAliasMaxValue,
      long sessionExpiryInterval,
      boolean keepAliveEnabled,
      boolean sessionsEnabled,
      boolean retainAvailable,
      boolean wildcardSubscriptionAvailable,
      boolean subscriptionIdAvailable,
      boolean sharedSubscriptionAvailable) {
    return new MqttServerConnectionConfig(
        maxQos,
        maxPacketSize,
        maxStringLength,
        maxBinarySize,
        maxTopicLevels,
        serverKeepAlive,
        receiveMaxPublishes,
        topicAliasMaxValue,
        sessionExpiryInterval,
        keepAliveEnabled,
        sessionsEnabled,
        retainAvailable,
        wildcardSubscriptionAvailable,
        subscriptionIdAvailable,
        sharedSubscriptionAvailable)
  }

  static MqttClientConnectionConfig clientConnectionConfig(
      MqttServerConnectionConfig serverConnectionConfig,
      QoS maxQos,
      MqttVersion mqttVersion,
      long sessionExpiryInterval,
      int receiveMaxPublishes,
      int maxPacketSize,
      int topicAliasMaxValue,
      int keepAlive,
      boolean requestResponseInformation,
      boolean requestProblemInformation) {
    return new MqttClientConnectionConfig(
        serverConnectionConfig,
        maxQos,
        mqttVersion,
        sessionExpiryInterval,
        receiveMaxPublishes,
        maxPacketSize,
        topicAliasMaxValue,
        keepAlive,
        requestResponseInformation,
        requestProblemInformation)
  }

  MqttConnection mqttConnection(
      MqttServerConnectionConfig serverConfig,
      MqttClientConnectionConfig clientConfig,
      String clientId) {
    return Stub(MqttConnection) {
      isSupported(_) >> { MqttVersion version ->
        clientConfig.mqttVersion().include(version)
      }
      serverConnectionConfig() >> serverConfig
      clientConnectionConfig() >> clientConfig
      client() >> mqttClient(clientConfig, clientId)
    }
  }

  MqttClient mqttClient(MqttClientConnectionConfig clientConfig, String id) {
    return Stub(MqttClient.UnsafeMqttClient) {
      connectionConfig() >> clientConfig
      clientId() >> id
      toString() >> id
    }
  }

  MqttClient newMqtt311Client() {
    def config = defaultMqtt311ClientConnectionConfig()
    def id = "generatedClient_${clientIdGenerator.incrementAndGet()}"
    return Stub(MqttClient.UnsafeMqttClient) {
      connectionConfig() >> config
      clientId() >> id
      toString() >> id
    }
  }
}