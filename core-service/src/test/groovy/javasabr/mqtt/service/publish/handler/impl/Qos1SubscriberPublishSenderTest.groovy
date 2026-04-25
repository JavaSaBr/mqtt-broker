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
import javasabr.mqtt.network.message.in.PublishAckMqttInMessage
import javasabr.mqtt.network.message.in.PublishReceivedMqttInMessage
import javasabr.mqtt.network.message.out.DisconnectMqtt5OutMessage
import javasabr.mqtt.network.message.out.PublishMqtt5OutMessage
import javasabr.mqtt.service.TestExternalNetworkMqttUser

class Qos1SubscriberPublishSenderTest extends QosSubscriberPublishSenderTest {

  def "should deliver publish to subscriber"() {
    given:
        def publishOutHandler = new Qos1SubscriberPublishSender(defaultMessageOutFactoryService)
        def connection = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = connection.user() as TestExternalNetworkMqttUser
        def testTopicName = defaultTopicService.createTopicName(user, "Qos1MqttPublishOutMessageHandlerTest/1")
        def originalMessageId = 60
        def testPublish = Publish.minimal(originalMessageId, QoS.EXACTLY_ONCE, testTopicName, testPayload)
            .withDuplicated()
    when:
        publishOutHandler.handle(testPublish, user)
    then:
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
        def publishOutHandler = new Qos1SubscriberPublishSender(defaultMessageOutFactoryService)
        def connection = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = connection.user() as TestExternalNetworkMqttUser
        def session = user.session()
        def testTopicName = defaultTopicService.createTopicName(user, "Qos1MqttPublishOutMessageHandlerTest/2")
        def originalMessageId = 60
        def testPublish = Publish.minimal(originalMessageId, QoS.EXACTLY_ONCE, testTopicName, testPayload)
            .withDuplicated()
    when:
        publishOutHandler.handle(testPublish, user)
    then:
        def publish = user.nextSentMessage(PublishMqtt5OutMessage)
        with(session.outMessageTracker()) {
          with(stored(publish.messageId())) {
            messageType() == MqttMessageType.PUBLISH
            reasonCode() == null
          }
        }
        with(session.outProcessingPublishes()) {
          size() == 1
        }
    when: 'send publish ack'
        def publishAck = PublishAckMqttInMessage
            .of(publish.messageId(), PublishAckReasonCode.SUCCESS)
        session
            .outProcessingPublishes()
            .apply(user, publishAck)
    then:
        with(session.outMessageTracker()) {
          stored(publish.messageId()) == null
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
        def publishOutHandler = new Qos1SubscriberPublishSender(defaultMessageOutFactoryService)
        def connection = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = connection.user() as TestExternalNetworkMqttUser
        def session = user.session()
        def testTopicName = defaultTopicService.createTopicName(user, "Qos1MqttPublishOutMessageHandlerTest/3")
        def originalMessageId = 60
        def testPublish = Publish.minimal(originalMessageId, QoS.EXACTLY_ONCE, testTopicName, testPayload)
            .withDuplicated()
    when:
        publishOutHandler.handle(testPublish, user)
    then:
        def publish = user.nextSentMessage(PublishMqtt5OutMessage)
        with(session.outMessageTracker()) {
          with(stored(publish.messageId())) {
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
            .remove(publish.messageId())
        def publishAck = PublishAckMqttInMessage
            .of(publish.messageId(), PublishAckReasonCode.SUCCESS)
        session
            .outProcessingPublishes()
            .apply(user, publishAck)
    then:
        with(session.outMessageTracker()) {
          stored(publish.messageId()) == null
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
        def publishOutHandler = new Qos1SubscriberPublishSender(defaultMessageOutFactoryService)
        def connection = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = connection.user() as TestExternalNetworkMqttUser
        def session = user.session()
        def testTopicName = defaultTopicService.createTopicName(user, "Qos1MqttPublishOutMessageHandlerTest/4")
        def originalMessageId = 60
        def testPublish = Publish.minimal(originalMessageId, QoS.EXACTLY_ONCE, testTopicName, testPayload)
            .withDuplicated()
    when:
        publishOutHandler.handle(testPublish, user)
    then:
        def publish = user.nextSentMessage(PublishMqtt5OutMessage)
        with(session.outMessageTracker()) {
          with(stored(publish.messageId())) {
            messageType() == MqttMessageType.PUBLISH
            reasonCode() == null
          }
        }
        with(session.outProcessingPublishes()) {
          size() == 1
        }
    when: 'send unexpected publish receive to get protocol error'
        def publishReceive = PublishReceivedMqttInMessage
            .of(publish.messageId(), PublishReceivedReasonCode.SUCCESS)
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
        def publishOutHandler = new Qos1SubscriberPublishSender(defaultMessageOutFactoryService)
        def connection = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = connection.user() as TestExternalNetworkMqttUser
        def session = user.session()
        def testTopicName = defaultTopicService.createTopicName(user, "Qos1MqttPublishOutMessageHandlerTest/5")
        def originalMessageId = 60
        def testPublish = Publish.minimal(originalMessageId, QoS.EXACTLY_ONCE, testTopicName, testPayload)
            .withDuplicated()
    when:
        publishOutHandler.handle(testPublish, user)
    then:
        def publish = user.nextSentMessage(PublishMqtt5OutMessage)
        with(session.outMessageTracker()) {
          with(stored(publish.messageId())) {
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
            .update(publish.messageId(), MqttMessageType.PUBLISH_RELEASE, PublishReleaseReasonCode.SUCCESS)
        def publishAck = PublishAckMqttInMessage
            .of(publish.messageId(), PublishAckReasonCode.SUCCESS)
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
