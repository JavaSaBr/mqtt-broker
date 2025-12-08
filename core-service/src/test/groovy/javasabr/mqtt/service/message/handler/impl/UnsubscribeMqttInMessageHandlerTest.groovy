package javasabr.mqtt.service.message.handler.impl

import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.reason.code.DisconnectReasonCode
import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode
import javasabr.mqtt.model.subscription.Subscription
import javasabr.mqtt.network.message.in.UnsubscribeMqttInMessage
import javasabr.mqtt.network.message.out.DisconnectMqtt5OutMessage
import javasabr.mqtt.network.message.out.UnsubscribeAckMqtt5OutMessage
import javasabr.mqtt.network.util.ExtraErrorReasons
import javasabr.mqtt.service.IntegrationServiceSpecification
import javasabr.mqtt.service.TestExternalNetworkMqttUser
import javasabr.mqtt.service.impl.InMemorySubscriptionService
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.MutableArray
import javasabr.rlib.common.util.ThreadUtils

class UnsubscribeMqttInMessageHandlerTest extends IntegrationServiceSpecification {

  def "should close connection by reason that session is already closed"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def messageHandler = new UnsubscribeMqttInMessageHandler(
            defaultSubscriptionService,
            defaultMessageOutFactoryService,
            defaultTopicService)
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
        mqttUser.session(null)
    when:
        def unsubscribeMessage = new UnsubscribeMqttInMessage(UnsubscribeMqttInMessage.MESSAGE_FLAGS)
        messageHandler.processValidMessage(mqttConnection, unsubscribeMessage)
    then:
        def disconnectReason = mqttUser.nextSentMessage(DisconnectMqtt5OutMessage)
        disconnectReason.reasonCode() == DisconnectReasonCode.UNSPECIFIED_ERROR
        disconnectReason.reason() == ExtraErrorReasons.SESSION_IS_ALREADY_CLOSED
        disconnectReason.serverReference() == null
  }

  def "should response that message id is in use"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def messageHandler = new UnsubscribeMqttInMessageHandler(
            defaultSubscriptionService,
            defaultMessageOutFactoryService,
            defaultTopicService)
        def expectedMessageId = 15
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
        def session = mqttUser.session()
        def inMessageTracker = session.inMessageTracker()
        inMessageTracker.add(expectedMessageId, MqttMessageType.UNSUBSCRIBE_ACK)
    when:
        def unsubscribeMessage = new UnsubscribeMqttInMessage(UnsubscribeMqttInMessage.MESSAGE_FLAGS) {{
          this.messageId = expectedMessageId
          this.rawTopicFilters = MutableArray.ofType(String)
          this.rawTopicFilters.addAll(Array.of("topic1", "topic2"))
        }}
        messageHandler.processValidMessage(mqttConnection, unsubscribeMessage)
    then:
        def unsubscribeAck = mqttUser.nextSentMessage(UnsubscribeAckMqtt5OutMessage)
        def reasonCodes = unsubscribeAck.reasonCodes()
        reasonCodes.size() == 2
        reasonCodes.get(0) == UnsubscribeAckReasonCode.PACKET_IDENTIFIER_IN_USE
        reasonCodes.get(1) == UnsubscribeAckReasonCode.PACKET_IDENTIFIER_IN_USE
        unsubscribeAck.messageId() == expectedMessageId
  }

  def "should response with expected results"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def subscriptionService = new InMemorySubscriptionService(defaultRetainMessageService)
        def messageHandler = new UnsubscribeMqttInMessageHandler(
            subscriptionService,
            defaultMessageOutFactoryService,
            defaultTopicService)
        def expectedMessageId = 15
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
        def session = mqttUser.session()
        def topicFilter1 = defaultTopicService.createTopicFilter(mqttUser, "topic/exist")
        def topicFilter2 = defaultTopicService.createTopicFilter(mqttUser, "topic/exist2")
        def topicFilter3 = defaultTopicService.createTopicFilter(mqttUser, "topic/exist3")
        subscriptionService.subscribe(
            mqttUser,
            session,
            Array.of(
                Subscription.minimal(topicFilter1, QoS.AT_MOST_ONCE),
                Subscription.minimal(topicFilter2, QoS.AT_MOST_ONCE),
                Subscription.minimal(topicFilter3, QoS.AT_MOST_ONCE)))
    when:
        def unsubscribeMessage = new UnsubscribeMqttInMessage(UnsubscribeMqttInMessage.MESSAGE_FLAGS) {{
          this.messageId = expectedMessageId
          this.rawTopicFilters = MutableArray.ofType(String)
          this.rawTopicFilters.addAll(Array.of(topicFilter1.rawTopic(), topicFilter2.rawTopic(), "topic/notexist", "topic/invalid##"))
        }}
        messageHandler.processValidMessage(mqttConnection, unsubscribeMessage)
    then:
        def unsubscribeAck = mqttUser.nextSentMessage(UnsubscribeAckMqtt5OutMessage)
        def reasonCodes = unsubscribeAck.reasonCodes()
        reasonCodes.size() == 4
        reasonCodes.get(0) == UnsubscribeAckReasonCode.SUCCESS
        reasonCodes.get(1) == UnsubscribeAckReasonCode.SUCCESS
        reasonCodes.get(2) == UnsubscribeAckReasonCode.NO_SUBSCRIPTION_EXISTED
        reasonCodes.get(3) == UnsubscribeAckReasonCode.TOPIC_FILTER_INVALID
        unsubscribeAck.messageId() == expectedMessageId
    when:
        def topicName1 = defaultTopicService.createTopicName(mqttUser, "topic/exist")
        def topicName2 = defaultTopicService.createTopicName(mqttUser, "topic/exist2")
        def topicName3 = defaultTopicService.createTopicName(mqttUser, "topic/exist3")
        def subscribers1 = subscriptionService.findSubscribers(topicName1)
        def subscribers2 = subscriptionService.findSubscribers(topicName2)
        def subscribers3 = subscriptionService.findSubscribers(topicName3)
    then:
        subscribers1.isEmpty()
        subscribers2.isEmpty()
        subscribers3.size() == 1 && subscribers3.first().user() == mqttUser
  }

  def "should close connection by reason MQTT protocol error"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def messageHandler = new UnsubscribeMqttInMessageHandler(
            defaultSubscriptionService,
            defaultMessageOutFactoryService,
            defaultTopicService)
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
    when:
        def unsubscribeMessage = new UnsubscribeMqttInMessage(0 as byte)
        messageHandler.processInvalidMessage(mqttConnection, unsubscribeMessage)
    then:
        def disconnectReason = mqttUser.nextSentMessage(DisconnectMqtt5OutMessage)
        disconnectReason.reasonCode() == DisconnectReasonCode.MALFORMED_PACKET
        disconnectReason.reason() == "Unexpected message flags:[0b0000_0000] in message:[$MqttMessageType.UNSUBSCRIBE]"
        disconnectReason.serverReference() == null
  }

  def "should reuse the same message if from previous request"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def messageHandler = new UnsubscribeMqttInMessageHandler(
            defaultSubscriptionService,
            defaultMessageOutFactoryService,
            defaultTopicService)
        def expectedMessageId = 15
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
    when:
        def unsubscribeMessage = new UnsubscribeMqttInMessage(UnsubscribeMqttInMessage.MESSAGE_FLAGS) {{
          this.messageId = expectedMessageId
          this.rawTopicFilters = MutableArray.ofType(String)
          this.rawTopicFilters.addAll(Array.of("topic1"))
        }}
        messageHandler.processValidMessage(mqttConnection, unsubscribeMessage)
    then:
        def unsubscribeAck = mqttUser.nextSentMessage(UnsubscribeAckMqtt5OutMessage)
        def reasonCodes = unsubscribeAck.reasonCodes()
        reasonCodes.size() == 1
        reasonCodes.get(0) == UnsubscribeAckReasonCode.NO_SUBSCRIPTION_EXISTED
        unsubscribeAck.messageId() == expectedMessageId
    when:
        ThreadUtils.sleep(300)
        def unsubscribeMessage2 = new UnsubscribeMqttInMessage(UnsubscribeMqttInMessage.MESSAGE_FLAGS) {{
          this.messageId = expectedMessageId
          this.rawTopicFilters = MutableArray.ofType(String)
          this.rawTopicFilters.addAll(Array.of("topic2"))
        }}
        messageHandler.processValidMessage(mqttConnection, unsubscribeMessage2)
    then:
        def unsubscribeAck2 = mqttUser.nextSentMessage(UnsubscribeAckMqtt5OutMessage)
        def reasonCodes2 = unsubscribeAck2.reasonCodes()
        reasonCodes2.size() == 1
        reasonCodes2.get(0) == UnsubscribeAckReasonCode.NO_SUBSCRIPTION_EXISTED
        unsubscribeAck2.messageId() == expectedMessageId
  }

  def "should response that message id is in use because previous is still in progress"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def messageHandler = new UnsubscribeMqttInMessageHandler(
            defaultSubscriptionService,
            defaultMessageOutFactoryService,
            defaultTopicService)
        def expectedMessageId = 15
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
        mqttUser.returnCompletedFeatures(false)
    when:
        def unsubscribeMessage = new UnsubscribeMqttInMessage(UnsubscribeMqttInMessage.MESSAGE_FLAGS) {{
          this.messageId = expectedMessageId
          this.rawTopicFilters = MutableArray.ofType(String)
          this.rawTopicFilters.addAll(Array.of("topic1"))
        }}
        messageHandler.processValidMessage(mqttConnection, unsubscribeMessage)
    then:
        def unsubscribeAck = mqttUser.nextSentMessage(UnsubscribeAckMqtt5OutMessage)
        def reasonCodes = unsubscribeAck.reasonCodes()
        reasonCodes.size() == 1
        reasonCodes.get(0) == UnsubscribeAckReasonCode.NO_SUBSCRIPTION_EXISTED
        unsubscribeAck.messageId() == expectedMessageId
    when:
        ThreadUtils.sleep(300)
        def unsubscribeMessage2 = new UnsubscribeMqttInMessage(UnsubscribeMqttInMessage.MESSAGE_FLAGS) {{
          this.messageId = expectedMessageId
          this.rawTopicFilters = MutableArray.ofType(String)
          this.rawTopicFilters.addAll(Array.of("topic2"))
        }}
        messageHandler.processValidMessage(mqttConnection, unsubscribeMessage2)
    then:
        def unsubscribeAck2 = mqttUser.nextSentMessage(UnsubscribeAckMqtt5OutMessage)
        def reasonCodes2 = unsubscribeAck2.reasonCodes()
        reasonCodes2.size() == 1
        reasonCodes2.get(0) == UnsubscribeAckReasonCode.PACKET_IDENTIFIER_IN_USE
        unsubscribeAck2.messageId() == expectedMessageId
  }
}
