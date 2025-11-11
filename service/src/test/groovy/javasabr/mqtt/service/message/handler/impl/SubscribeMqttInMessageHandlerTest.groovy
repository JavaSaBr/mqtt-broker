package javasabr.mqtt.service.message.handler.impl

import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.reason.code.DisconnectReasonCode
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode
import javasabr.mqtt.model.subscribtion.RequestedSubscription
import javasabr.mqtt.network.message.in.SubscribeMqttInMessage
import javasabr.mqtt.network.message.out.DisconnectMqtt5OutMessage
import javasabr.mqtt.network.message.out.SubscribeAckMqtt5OutMessage
import javasabr.mqtt.network.util.ExtraErrorReasons
import javasabr.mqtt.service.IntegrationServiceSpecification
import javasabr.mqtt.service.TestExternalMqttClient
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.MutableArray
import javasabr.rlib.common.util.ThreadUtils
import javasabr.rlib.logger.api.LoggerLevel
import javasabr.rlib.logger.api.LoggerManager

class SubscribeMqttInMessageHandlerTest extends IntegrationServiceSpecification {

  static {
    LoggerManager.enable(SubscribeMqttInMessageHandler,  LoggerLevel.WARNING)
    LoggerManager.enable(SubscribeMqttInMessageHandler,  LoggerLevel.INFO)
  }

  def "should close connection by reason that session is already closed"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def messageHandler = new SubscribeMqttInMessageHandler(
            defaultSubscriptionService,
            defaultMessageOutFactoryService,
            defaultTopicService)
        def mqttClient = mqttConnection.client() as TestExternalMqttClient
        mqttClient.session(null)
    when:
        def subscribeMessage = new SubscribeMqttInMessage(SubscribeMqttInMessage.MESSAGE_FLAGS)
        messageHandler.processValidMessage(mqttConnection, subscribeMessage)
    then:
        def disconnectReason = mqttClient.nextSentMessage(DisconnectMqtt5OutMessage)
        disconnectReason.reasonCode() == DisconnectReasonCode.UNSPECIFIED_ERROR
            && disconnectReason.reason() == ExtraErrorReasons.SESSION_IS_ALREADY_CLOSED
            && disconnectReason.serverReference() == ""
  }

  def "should response that message id is in use"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def messageHandler = new SubscribeMqttInMessageHandler(
            defaultSubscriptionService,
            defaultMessageOutFactoryService,
            defaultTopicService)
        def expectedMessageId = 15
        def mqttClient = mqttConnection.client() as TestExternalMqttClient
        def session = mqttClient.session()
        session.inMessageTracker().add(expectedMessageId)
    when:
        def subscribeMessage = new SubscribeMqttInMessage(SubscribeMqttInMessage.MESSAGE_FLAGS) {{
          this.messageId = expectedMessageId
          this.subscriptions = MutableArray.ofType(RequestedSubscription)
          this.subscriptions.addAll(Array.of(
              RequestedSubscription.minimal("topic1", QoS.EXACTLY_ONCE),
              RequestedSubscription.minimal("topic2", QoS.EXACTLY_ONCE)))
        }}
        messageHandler.processValidMessage(mqttConnection, subscribeMessage)
    then:
        def subscribeAck = mqttClient.nextSentMessage(SubscribeAckMqtt5OutMessage)
        def reasonCodes = subscribeAck.reasonCodes()
        reasonCodes.size() == 2
            && reasonCodes.get(0) == SubscribeAckReasonCode.PACKET_IDENTIFIER_IN_USE
            && reasonCodes.get(1) == SubscribeAckReasonCode.PACKET_IDENTIFIER_IN_USE
            && subscribeAck.messageId() == expectedMessageId
  }

  def "should response that subscription id is not supported"() {
    given:
        def serverConfig = defaultExternalServerConnectionConfig
            .withSubscriptionIdAvailable(false)
        def mqttConnection = mockedExternalConnection(serverConfig, MqttVersion.MQTT_5)
        def messageHandler = new SubscribeMqttInMessageHandler(
            defaultSubscriptionService,
            defaultMessageOutFactoryService,
            defaultTopicService)
        def expectedMessageId = 15
        def mqttClient = mqttConnection.client() as TestExternalMqttClient
    when:
        def subscribeMessage = new SubscribeMqttInMessage(SubscribeMqttInMessage.MESSAGE_FLAGS) {{
          this.messageId = expectedMessageId
          this.subscriptionId = 25
          this.subscriptions = MutableArray.ofType(RequestedSubscription)
          this.subscriptions.addAll(Array.of(
              RequestedSubscription.minimal("topic1", QoS.EXACTLY_ONCE),
              RequestedSubscription.minimal("topic2", QoS.EXACTLY_ONCE)))
        }}
        messageHandler.processValidMessage(mqttConnection, subscribeMessage)
    then:
        def subscribeAck = mqttClient.nextSentMessage(SubscribeAckMqtt5OutMessage)
        def reasonCodes = subscribeAck.reasonCodes()
        reasonCodes.size() == 2
            && reasonCodes.get(0) == SubscribeAckReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED
            && reasonCodes.get(1) == SubscribeAckReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED
            && subscribeAck.messageId() == expectedMessageId
  }

  def "should subscribe with lower QoS by server limitation"() {
    given:
        def serverConfig = defaultExternalServerConnectionConfig
            .withMaxQos(QoS.AT_MOST_ONCE)
        def mqttConnection = mockedExternalConnection(serverConfig, MqttVersion.MQTT_5)
        def messageHandler = new SubscribeMqttInMessageHandler(
            defaultSubscriptionService,
            defaultMessageOutFactoryService,
            defaultTopicService)
        def expectedMessageId = 15
        def mqttClient = mqttConnection.client() as TestExternalMqttClient
    when:
        def subscribeMessage = new SubscribeMqttInMessage(SubscribeMqttInMessage.MESSAGE_FLAGS) {{
          this.messageId = expectedMessageId
          this.subscriptionId = 25
          this.subscriptions = MutableArray.ofType(RequestedSubscription)
          this.subscriptions.addAll(Array.of(
              RequestedSubscription.minimal("topic1", QoS.EXACTLY_ONCE),
              RequestedSubscription.minimal("topic2", QoS.EXACTLY_ONCE)))
        }}
        messageHandler.processValidMessage(mqttConnection, subscribeMessage)
    then:
        def subscribeAck = mqttClient.nextSentMessage(SubscribeAckMqtt5OutMessage)
        def reasonCodes = subscribeAck.reasonCodes()
        reasonCodes.size() == 2
            && reasonCodes.get(0) == SubscribeAckReasonCode.GRANTED_QOS_0
            && reasonCodes.get(1) == SubscribeAckReasonCode.GRANTED_QOS_0
            && subscribeAck.messageId() == expectedMessageId
  }

  def "should close connection by trying to subscribe not supported wildcard topic filter"() {
    given:
        def serverConfig = defaultExternalServerConnectionConfig
            .withWildcardSubscriptionAvailable(false)
        def mqttConnection = mockedExternalConnection(serverConfig, MqttVersion.MQTT_5)
        def messageHandler = new SubscribeMqttInMessageHandler(
            defaultSubscriptionService,
            defaultMessageOutFactoryService,
            defaultTopicService)
        def expectedMessageId = 15
        def mqttClient = mqttConnection.client() as TestExternalMqttClient
    when:
        def subscribeMessage = new SubscribeMqttInMessage(SubscribeMqttInMessage.MESSAGE_FLAGS) {{
          this.messageId = expectedMessageId
          this.subscriptions = MutableArray.ofType(RequestedSubscription)
          this.subscriptions.addAll(Array.of(
              RequestedSubscription.minimal("topic1/#", QoS.EXACTLY_ONCE),
              RequestedSubscription.minimal("topic2/+", QoS.EXACTLY_ONCE)))
        }}
        messageHandler.processValidMessage(mqttConnection, subscribeMessage)
    then:
        def subscribeAck = mqttClient.nextSentMessage(SubscribeAckMqtt5OutMessage)
        def reasonCodes = subscribeAck.reasonCodes()
        reasonCodes.size() == 2
            && reasonCodes.get(0) == SubscribeAckReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED
            && reasonCodes.get(1) == SubscribeAckReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED
            && subscribeAck.messageId() == expectedMessageId
        def disconnectReason = mqttClient.nextSentMessage(DisconnectMqtt5OutMessage)
        disconnectReason.reasonCode() == DisconnectReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED
            && disconnectReason.reason() == ""
            && disconnectReason.serverReference() == ""
  }

  def "should close connection by trying to subscribe not supported shared topic filter"() {
    given:
        def serverConfig = defaultExternalServerConnectionConfig
            .withSharedSubscriptionAvailable(false)
        def mqttConnection = mockedExternalConnection(serverConfig, MqttVersion.MQTT_5)
        def messageHandler = new SubscribeMqttInMessageHandler(
            defaultSubscriptionService,
            defaultMessageOutFactoryService,
            defaultTopicService)
        def expectedMessageId = 15
        def mqttClient = mqttConnection.client() as TestExternalMqttClient
    when:
        def subscribeMessage = new SubscribeMqttInMessage(SubscribeMqttInMessage.MESSAGE_FLAGS) {{
          this.messageId = expectedMessageId
          this.subscriptions = MutableArray.ofType(RequestedSubscription)
          this.subscriptions.addAll(Array.of(
              RequestedSubscription.minimal("\$share/group1/topic1/#", QoS.EXACTLY_ONCE),
              RequestedSubscription.minimal("\$share/group1/topic2/+", QoS.EXACTLY_ONCE)))
        }}
        messageHandler.processValidMessage(mqttConnection, subscribeMessage)
    then:
        def subscribeAck = mqttClient.nextSentMessage(SubscribeAckMqtt5OutMessage)
        def reasonCodes = subscribeAck.reasonCodes()
        reasonCodes.size() == 2
            && reasonCodes.get(0) == SubscribeAckReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED
            && reasonCodes.get(1) == SubscribeAckReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED
            && subscribeAck.messageId() == expectedMessageId
        def disconnectReason = mqttClient.nextSentMessage(DisconnectMqtt5OutMessage)
        disconnectReason.reasonCode() == DisconnectReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED
            && disconnectReason.reason() == ""
            && disconnectReason.serverReference() == ""
  }

  def "should close connection by reason MQTT protocol error"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def messageHandler = new SubscribeMqttInMessageHandler(
            defaultSubscriptionService,
            defaultMessageOutFactoryService,
            defaultTopicService)
        def mqttClient = mqttConnection.client() as TestExternalMqttClient
    when:
        def subscribeMessage = new SubscribeMqttInMessage(0 as byte)
        messageHandler.processInvalidMessage(mqttConnection, subscribeMessage)
    then:
        def disconnectReason = mqttClient.nextSentMessage(DisconnectMqtt5OutMessage)
        disconnectReason.reasonCode() == DisconnectReasonCode.MALFORMED_PACKET
            && disconnectReason.reason() == "Unexpected flags bits:0b0000_0000"
            && disconnectReason.serverReference() == ""
  }

  def "should reuse the same message if from previous request"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def messageHandler = new SubscribeMqttInMessageHandler(
            defaultSubscriptionService,
            defaultMessageOutFactoryService,
            defaultTopicService)
        def expectedMessageId = 15
        def mqttClient = mqttConnection.client() as TestExternalMqttClient
    when:
        def subscribeMessage = new SubscribeMqttInMessage(SubscribeMqttInMessage.MESSAGE_FLAGS) {{
          this.messageId = expectedMessageId
          this.subscriptions = MutableArray.ofType(RequestedSubscription)
          this.subscriptions.addAll(Array.of(RequestedSubscription.minimal("topic1", QoS.EXACTLY_ONCE)))
        }}
        messageHandler.processValidMessage(mqttConnection, subscribeMessage)
    then:
        def subscribeAck = mqttClient.nextSentMessage(SubscribeAckMqtt5OutMessage)
        def reasonCodes = subscribeAck.reasonCodes()
        reasonCodes.size() == 1
            && reasonCodes.get(0) == SubscribeAckReasonCode.GRANTED_QOS_2
            && subscribeAck.messageId() == expectedMessageId
    when:
        ThreadUtils.sleep(300)
        def subscribeMessage2 = new SubscribeMqttInMessage(SubscribeMqttInMessage.MESSAGE_FLAGS) {{
          this.messageId = expectedMessageId
          this.subscriptions = MutableArray.ofType(RequestedSubscription)
          this.subscriptions.addAll(Array.of(RequestedSubscription.minimal("topic2", QoS.EXACTLY_ONCE)))
        }}
        messageHandler.processValidMessage(mqttConnection, subscribeMessage2)
    then:
        def subscribeAck2 = mqttClient.nextSentMessage(SubscribeAckMqtt5OutMessage)
        def reasonCodes2 = subscribeAck2.reasonCodes()
        reasonCodes2.size() == 1
            && reasonCodes2.get(0) == SubscribeAckReasonCode.GRANTED_QOS_2
            && subscribeAck2.messageId() == expectedMessageId
  }

  def "should response that message id is in use because previous is still in progress"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def messageHandler = new SubscribeMqttInMessageHandler(
            defaultSubscriptionService,
            defaultMessageOutFactoryService,
            defaultTopicService)
        def expectedMessageId = 15
        def mqttClient = mqttConnection.client() as TestExternalMqttClient
        mqttClient.returnCompletedFeatures(false)
    when:
        def subscribeMessage = new SubscribeMqttInMessage(SubscribeMqttInMessage.MESSAGE_FLAGS) {{
          this.messageId = expectedMessageId
          this.subscriptions = MutableArray.ofType(RequestedSubscription)
          this.subscriptions.addAll(Array.of(RequestedSubscription.minimal("topic2", QoS.EXACTLY_ONCE)))
        }}
        messageHandler.processValidMessage(mqttConnection, subscribeMessage)
    then:
        def subscribeAck = mqttClient.nextSentMessage(SubscribeAckMqtt5OutMessage)
        def reasonCodes = subscribeAck.reasonCodes()
        reasonCodes.size() == 1
            && reasonCodes.get(0) == SubscribeAckReasonCode.GRANTED_QOS_2
            && subscribeAck.messageId() == expectedMessageId
    when:
        ThreadUtils.sleep(300)
        def subscribeMessage2 = new SubscribeMqttInMessage(SubscribeMqttInMessage.MESSAGE_FLAGS) {{
          this.messageId = expectedMessageId
          this.subscriptions = MutableArray.ofType(RequestedSubscription)
          this.subscriptions.addAll(Array.of(RequestedSubscription.minimal("topic2", QoS.EXACTLY_ONCE)))
        }}
        messageHandler.processValidMessage(mqttConnection, subscribeMessage2)
    then:
        def subscribeAck2 = mqttClient.nextSentMessage(SubscribeAckMqtt5OutMessage)
        def reasonCodes2 = subscribeAck2.reasonCodes()
        reasonCodes2.size() == 1
            && reasonCodes2.get(0) == SubscribeAckReasonCode.PACKET_IDENTIFIER_IN_USE
            && subscribeAck2.messageId() == expectedMessageId
  }
}
