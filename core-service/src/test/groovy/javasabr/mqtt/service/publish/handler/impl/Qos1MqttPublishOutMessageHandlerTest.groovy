package javasabr.mqtt.service.publish.handler.impl

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.publishing.Publish
import javasabr.mqtt.model.reason.code.DisconnectReasonCode
import javasabr.mqtt.model.reason.code.PublishAckReasonCode
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode
import javasabr.mqtt.model.reason.code.PublishReleaseReasonCode
import javasabr.mqtt.model.subscriber.SingleSubscriber
import javasabr.mqtt.model.subscription.Subscription
import javasabr.mqtt.network.message.in.PublishAckMqttInMessage
import javasabr.mqtt.network.message.in.PublishReceivedMqttInMessage
import javasabr.mqtt.network.message.out.DisconnectMqtt5OutMessage
import javasabr.mqtt.network.message.out.PublishMqtt5OutMessage
import javasabr.mqtt.service.TestExternalNetworkMqttUser
import javasabr.mqtt.service.publish.handler.PublishHandlingResult

class Qos1MqttPublishOutMessageHandlerTest extends QosMqttPublishOutMessageHandlerTest {

  def "should deliver publish to subscriber"() {
    given:
        def publishOutHandler = new Qos1MqttPublishOutMessageHandler(
            defaultSubscriptionService,
            defaultMessageOutFactoryService)
        def connection = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = connection.user() as TestExternalNetworkMqttUser
        def testTopicName = defaultTopicService.createTopicName(user, "Qos1MqttPublishOutMessageHandlerTest/1")
        def topicFilter = defaultTopicService.createTopicFilter(user, "Qos1MqttPublishOutMessageHandlerTest/1")
        def subscription = Subscription.minimal(topicFilter, QoS.AT_MOST_ONCE)
        def subscriber = new SingleSubscriber(user, subscription)
        def originalMessageId = 60
        def publish = Publish.minimal(originalMessageId, QoS.EXACTLY_ONCE, testTopicName, testPayload)
            .withDuplicated()
    when:
        def result = publishOutHandler.handle(publish, subscriber)
    then:
        result == PublishHandlingResult.SUCCESS
        with(user.nextSentMessage(PublishMqtt5OutMessage)) {
          qos() == QoS.AT_LEAST_ONCE
          !duplicate()
          payload() == testPayload
          topicName() == testTopicName
          messageId() != MqttProperties.MESSAGE_ID_IS_NOT_SET
          topicAlias() == MqttProperties.TOPIC_ALIAS_NOT_SET
        }
  }

  def "should wait for ack response for publish"() {
    given:
        def publishOutHandler = new Qos1MqttPublishOutMessageHandler(
            defaultSubscriptionService,
            defaultMessageOutFactoryService)
        def connection = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = connection.user() as TestExternalNetworkMqttUser
        def session = user.session()
        def testTopicName = defaultTopicService.createTopicName(user, "Qos1MqttPublishOutMessageHandlerTest/2")
        def topicFilter = defaultTopicService.createTopicFilter(user, "Qos1MqttPublishOutMessageHandlerTest/2")
        def subscription = Subscription.minimal(topicFilter, QoS.AT_MOST_ONCE)
        def subscriber = new SingleSubscriber(user, subscription)
        def originalMessageId = 60
        def publish = Publish.minimal(originalMessageId, QoS.EXACTLY_ONCE, testTopicName, testPayload)
            .withDuplicated()
    when:
        def result = publishOutHandler.handle(publish, subscriber)
        def receivedPublish = user.nextSentMessage(PublishMqtt5OutMessage)
    then:
        result == PublishHandlingResult.SUCCESS
        with(session.outMessageTracker()) {
          with(stored(receivedPublish.messageId())) {
            messageType() == MqttMessageType.PUBLISH
            reasonCode() == null
          }
        }
        with(session.outProcessingPublishes()) {
          size() == 1
        }
    when: 'send publish ack'
        def publishAck = PublishAckMqttInMessage
            .of(receivedPublish.messageId(), PublishAckReasonCode.SUCCESS)
        session
            .outProcessingPublishes()
            .apply(user, publishAck)
    then:
        with(session.outMessageTracker()) {
          stored(receivedPublish.messageId()) == null
        }
        with(session.outProcessingPublishes()) {
          size() == 0
        }
    then: 'user should not receive any new message'
        with(user) {
          isEmpty()
        }
  }

  def "should correctly handle publish ack when no stored trackable meta about the publish"() {
    given:
        def publishOutHandler = new Qos1MqttPublishOutMessageHandler(
            defaultSubscriptionService,
            defaultMessageOutFactoryService)
        def connection = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = connection.user() as TestExternalNetworkMqttUser
        def session = user.session()
        def testTopicName = defaultTopicService.createTopicName(user, "Qos1MqttPublishOutMessageHandlerTest/3")
        def topicFilter = defaultTopicService.createTopicFilter(user, "Qos1MqttPublishOutMessageHandlerTest/3")
        def subscription = Subscription.minimal(topicFilter, QoS.AT_MOST_ONCE)
        def subscriber = new SingleSubscriber(user, subscription)
        def originalMessageId = 60
        def publish = Publish.minimal(originalMessageId, QoS.EXACTLY_ONCE, testTopicName, testPayload)
            .withDuplicated()
    when:
        def result = publishOutHandler.handle(publish, subscriber)
        def receivedPublish = user.nextSentMessage(PublishMqtt5OutMessage)
    then:
        result == PublishHandlingResult.SUCCESS
        with(session.outMessageTracker()) {
          with(stored(receivedPublish.messageId())) {
            messageType() == MqttMessageType.PUBLISH
            reasonCode() == null
          }
        }
        with(session.outProcessingPublishes()) {
          size() == 1
        }
    when: 'remove trackable info and send publish ack'
        session
            .outMessageTracker()
            .remove(receivedPublish.messageId())
        def publishAck = PublishAckMqttInMessage
            .of(receivedPublish.messageId(), PublishAckReasonCode.SUCCESS)
        session
            .outProcessingPublishes()
            .apply(user, publishAck)
    then:
        with(session.outMessageTracker()) {
          stored(receivedPublish.messageId()) == null
        }
        with(session.outProcessingPublishes()) {
          size() == 0
        }
    then: 'user should not receive any new message'
        with(user) {
          isEmpty()
        }
  }

  def "should handle as protocol error receiving unexpected response message"() {
    given:
        def publishOutHandler = new Qos1MqttPublishOutMessageHandler(
            defaultSubscriptionService,
            defaultMessageOutFactoryService)
        def connection = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = connection.user() as TestExternalNetworkMqttUser
        def session = user.session()
        def testTopicName = defaultTopicService.createTopicName(user, "Qos1MqttPublishOutMessageHandlerTest/4")
        def topicFilter = defaultTopicService.createTopicFilter(user, "Qos1MqttPublishOutMessageHandlerTest/4")
        def subscription = Subscription.minimal(topicFilter, QoS.AT_MOST_ONCE)
        def subscriber = new SingleSubscriber(user, subscription)
        def originalMessageId = 60
        def publish = Publish.minimal(originalMessageId, QoS.EXACTLY_ONCE, testTopicName, testPayload)
            .withDuplicated()
    when:
        def result = publishOutHandler.handle(publish, subscriber)
        def receivedPublish = user.nextSentMessage(PublishMqtt5OutMessage)
    then:
        result == PublishHandlingResult.SUCCESS
        with(session.outMessageTracker()) {
          with(stored(receivedPublish.messageId())) {
            messageType() == MqttMessageType.PUBLISH
            reasonCode() == null
          }
        }
        with(session.outProcessingPublishes()) {
          size() == 1
        }
    when: 'send unexpected publish receive to get protocol error'
        def publishReceive = PublishReceivedMqttInMessage
            .of(receivedPublish.messageId(), PublishReceivedReasonCode.SUCCESS)
        session
            .outProcessingPublishes()
            .apply(user, publishReceive)
    then:
        with(user.nextSentMessage(DisconnectMqtt5OutMessage)) {
          reasonCode() == DisconnectReasonCode.PROTOCOL_ERROR
          reason() == "Unexpected response packet:'$MqttMessageType.PUBLISH_RECEIVED', expected:'$MqttMessageType.PUBLISH_ACK'"
        }
  }

  def "should handle as protocol error for unexpected flow state"() {
    given:
        def publishOutHandler = new Qos1MqttPublishOutMessageHandler(
            defaultSubscriptionService,
            defaultMessageOutFactoryService)
        def connection = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = connection.user() as TestExternalNetworkMqttUser
        def session = user.session()
        def testTopicName = defaultTopicService.createTopicName(user, "Qos1MqttPublishOutMessageHandlerTest/5")
        def topicFilter = defaultTopicService.createTopicFilter(user, "Qos1MqttPublishOutMessageHandlerTest/5")
        def subscription = Subscription.minimal(topicFilter, QoS.AT_MOST_ONCE)
        def subscriber = new SingleSubscriber(user, subscription)
        def originalMessageId = 60
        def publish = Publish.minimal(originalMessageId, QoS.EXACTLY_ONCE, testTopicName, testPayload)
            .withDuplicated()
    when:
        def result = publishOutHandler.handle(publish, subscriber)
        def receivedPublish = user.nextSentMessage(PublishMqtt5OutMessage)
    then:
        result == PublishHandlingResult.SUCCESS
        with(session.outMessageTracker()) {
          with(stored(receivedPublish.messageId())) {
            messageType() == MqttMessageType.PUBLISH
            reasonCode() == null
          }
        }
        with(session.outProcessingPublishes()) {
          size() == 1
        }
    when: 'change trackable info to publish release and send publish ack'
        session
            .outMessageTracker()
            .update(receivedPublish.messageId(), MqttMessageType.PUBLISH_RELEASE, PublishReleaseReasonCode.SUCCESS)
        def publishAck = PublishAckMqttInMessage
            .of(receivedPublish.messageId(), PublishAckReasonCode.SUCCESS)
        session
            .outProcessingPublishes()
            .apply(user, publishAck)
    then:
        with(user.nextSentMessage(DisconnectMqtt5OutMessage)) {
          reasonCode() == DisconnectReasonCode.PROTOCOL_ERROR
          reason() == "Unexpected flow state:'$MqttMessageType.PUBLISH_RELEASE', expected:'$MqttMessageType.PUBLISH'"
        }
  }
}
