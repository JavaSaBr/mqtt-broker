package javasabr.mqtt.service.publish.sender

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.reason.code.DisconnectReasonCode
import javasabr.mqtt.model.reason.code.PublishAckReasonCode
import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode
import javasabr.mqtt.model.reason.code.PublishReleaseReasonCode
import javasabr.mqtt.network.message.in.PublishAckMqttInMessage
import javasabr.mqtt.network.message.in.PublishCompleteMqttInMessage
import javasabr.mqtt.network.message.in.PublishReceivedMqttInMessage
import javasabr.mqtt.network.message.out.DisconnectMqtt5OutMessage
import javasabr.mqtt.network.message.out.PublishMqtt5OutMessage
import javasabr.mqtt.network.message.out.PublishReleaseMqtt5OutMessage
import javasabr.mqtt.service.TestExternalNetworkMqttUser

class Qos2SubscriberPublishSenderTest extends QosSubscriberPublishSenderTest {

  def "should deliver publish to subscriber"() {
    given:
        def sender = new Qos2SubscriberPublishSender(
            defaultMessageOutFactoryService, 
            defaultIncomingPublishStorage)
        def connection = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = connection.user() as TestExternalNetworkMqttUser
        def testTopicName = defaultTopicService.createTopicName(user, "Qos2SubscriberPublishSenderTest/1")
        def originalMessageId = 60
        def testPublish = prepareIncomingPublish(
            originalMessageId,
            QoS.EXACTLY_ONCE,
            testTopicName,
            testPayloadBytes)
    when:
        sender.sendToSubscriber(testPublish, user)
    then:
        with(user.nextSentMessage(PublishMqtt5OutMessage)) {
          qos() == QoS.EXACTLY_ONCE
          !duplicate()
          data() == testPublish.data()
          topicName() == testTopicName
          messageId() != MqttProperties.MESSAGE_ID_IS_NOT_SET
          topicAlias() == MqttProperties.TOPIC_ALIAS_NOT_SET
        }
  }

  def "should wait for receive-complete responses for publish"() {
    given:
        def sender = new Qos2SubscriberPublishSender(
            defaultMessageOutFactoryService,
            defaultIncomingPublishStorage)
        def connection = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = connection.user() as TestExternalNetworkMqttUser
        def session = user.session()
        def testTopicName = defaultTopicService.createTopicName(user, "Qos2SubscriberPublishSenderTest/2")
        def originalMessageId = 60
        def testPublish = prepareIncomingPublish(
            originalMessageId,
            QoS.EXACTLY_ONCE,
            testTopicName,
            testPayloadBytes)
    when:
        sender.sendToSubscriber(testPublish, user)
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
    when: 'send publish received'
        def publishReceived = PublishReceivedMqttInMessage
            .of(publish.messageId(), PublishReceivedReasonCode.SUCCESS)
        session
            .outProcessingPublishes()
            .apply(user, publishReceived)
    then:
        with(session.outMessageTracker()) {
          with(stored(publish.messageId())) {
            messageType() == MqttMessageType.PUBLISH_RELEASE
            reasonCode() == PublishReceivedReasonCode.SUCCESS
          }
        }
        with(session.outProcessingPublishes()) {
          size() == 1
        }
    when: 'user should receive publish release'
        def publishRelease = user.nextSentMessage(PublishReleaseMqtt5OutMessage)
    then:
        with(publishRelease) {
          reasonCode() == PublishReleaseReasonCode.SUCCESS
          messageId() == publish.messageId()
        }
    when: 'send publish complete'
        def publishComplete = PublishCompleteMqttInMessage
            .of(publish.messageId(), PublishCompletedReasonCode.SUCCESS)
        session
            .outProcessingPublishes()
            .apply(user, publishComplete)
    then:
        with(session.outMessageTracker()) {
          stored(publish.messageId()) == null
        }
        with(session.outProcessingPublishes()) {
          size() == 0
        }
    then:
        with(user) {
          isEmpty()
        }
  }

  def "should correctly handle publish receive when no stored trackable meta about the publish"() {
    given:
        def sender = new Qos2SubscriberPublishSender(
            defaultMessageOutFactoryService, 
            defaultIncomingPublishStorage)
        def connection = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = connection.user() as TestExternalNetworkMqttUser
        def session = user.session()
        def testTopicName = defaultTopicService.createTopicName(user, "Qos2SubscriberPublishSenderTest/3")
        def originalMessageId = 60
        def testPublish = prepareIncomingPublish(
            originalMessageId,
            QoS.EXACTLY_ONCE,
            testTopicName,
            testPayloadBytes)
    when:
        sender.sendToSubscriber(testPublish, user)
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
        def publishReceive = PublishReceivedMqttInMessage
            .of(publish.messageId(), PublishReceivedReasonCode.SUCCESS)
        session
            .outProcessingPublishes()
            .apply(user, publishReceive)
    then:
        with(session.outMessageTracker()) {
          stored(publish.messageId()) == null
        }
        with(session.outProcessingPublishes()) {
          size() == 0
        }
    then: 'user should not receive any new message'
        with(user.nextSentMessage(PublishReleaseMqtt5OutMessage)) {
          reasonCode() == PublishReleaseReasonCode.PACKET_IDENTIFIER_NOT_FOUND
          messageId() == publish.messageId()
        }
  }

  def "should handle as protocol error receiving unexpected response message for first stage"() {
    given:
        def sender = new Qos2SubscriberPublishSender(
            defaultMessageOutFactoryService, 
            defaultIncomingPublishStorage)
        def connection = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = connection.user() as TestExternalNetworkMqttUser
        def session = user.session()
        def testTopicName = defaultTopicService.createTopicName(user, "Qos2SubscriberPublishSenderTest/4")
        def originalMessageId = 60
        def testPublish = prepareIncomingPublish(
            originalMessageId,
            QoS.EXACTLY_ONCE,
            testTopicName,
            testPayloadBytes)
    when:
        sender.sendToSubscriber(testPublish, user)
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
    when: 'send unexpected publish ack to get protocol error'
        def publishAck = PublishAckMqttInMessage
            .of(publish.messageId(), PublishAckReasonCode.SUCCESS)
        session
            .outProcessingPublishes()
            .apply(user, publishAck)
    then:
        with(user.nextSentMessage(DisconnectMqtt5OutMessage)) {
          reasonCode() == DisconnectReasonCode.PROTOCOL_ERROR
          reason() == "Unexpected response packet:'$MqttMessageType.PUBLISH_ACK', expected:'$MqttMessageType.PUBLISH_RECEIVED'"
        }
  }

  def "should handle as protocol error receiving unexpected response message for second stage"() {
    given:
        def sender = new Qos2SubscriberPublishSender(
            defaultMessageOutFactoryService, 
            defaultIncomingPublishStorage)
        def connection = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = connection.user() as TestExternalNetworkMqttUser
        def session = user.session()
        def testTopicName = defaultTopicService.createTopicName(user, "Qos2SubscriberPublishSenderTest/4")
        def originalMessageId = 60
        def testPublish = prepareIncomingPublish(
            originalMessageId,
            QoS.EXACTLY_ONCE,
            testTopicName,
            testPayloadBytes)
    when:
        sender.sendToSubscriber(testPublish, user)
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
    when: 'send publish received'
        def publishReceived = PublishReceivedMqttInMessage
            .of(publish.messageId(), PublishReceivedReasonCode.SUCCESS)
        session
            .outProcessingPublishes()
            .apply(user, publishReceived)
    then:
        with(user.nextSentMessage(PublishReleaseMqtt5OutMessage)) {
          reasonCode() == PublishReleaseReasonCode.SUCCESS
          messageId() == publish.messageId()
        }
    when: 'send unexpected publish ack to get protocol error'
        def publishAck = PublishAckMqttInMessage
            .of(publish.messageId(), PublishAckReasonCode.SUCCESS)
        session
            .outProcessingPublishes()
            .apply(user, publishAck)
    then:
        with(user.nextSentMessage(DisconnectMqtt5OutMessage)) {
          reasonCode() == DisconnectReasonCode.PROTOCOL_ERROR
          reason() == "Unexpected response packet:'$MqttMessageType.PUBLISH_ACK', expected:'$MqttMessageType.PUBLISH_COMPLETE'"
        }
  }

  def "should handle as protocol error for unexpected flow state for publish received"() {
    given:
        def sender = new Qos2SubscriberPublishSender(
            defaultMessageOutFactoryService, 
            defaultIncomingPublishStorage)
        def connection = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = connection.user() as TestExternalNetworkMqttUser
        def session = user.session()
        def testTopicName = defaultTopicService.createTopicName(user, "Qos2SubscriberPublishSenderTest/5")
        def originalMessageId = 60
        def testPublish = prepareIncomingPublish(
            originalMessageId,
            QoS.EXACTLY_ONCE,
            testTopicName,
            testPayloadBytes)
    when:
        sender.sendToSubscriber(testPublish, user)
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
    when: 'change trackable info to publish release and send publish received'
        session
            .outMessageTracker()
            .update(publish.messageId(), MqttMessageType.PUBLISH_RELEASE, PublishReleaseReasonCode.SUCCESS)
        def publishReceived = PublishReceivedMqttInMessage
            .of(publish.messageId(), PublishReceivedReasonCode.SUCCESS)
        session
            .outProcessingPublishes()
            .apply(user, publishReceived)
    then:
        with(user.nextSentMessage(DisconnectMqtt5OutMessage)) {
          reasonCode() == DisconnectReasonCode.PROTOCOL_ERROR
          reason() == "Unexpected flow state:'$MqttMessageType.PUBLISH_RELEASE', expected:'$MqttMessageType.PUBLISH'"
        }
  }

  def "should handle as protocol error for unexpected flow state for publish complete"() {
    given:
        def sender = new Qos2SubscriberPublishSender(
            defaultMessageOutFactoryService, 
            defaultIncomingPublishStorage)
        def connection = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = connection.user() as TestExternalNetworkMqttUser
        def session = user.session()
        def testTopicName = defaultTopicService.createTopicName(user, "Qos2SubscriberPublishSenderTest/6")
        def originalMessageId = 60
        def testPublish = prepareIncomingPublish(
            originalMessageId,
            QoS.EXACTLY_ONCE,
            testTopicName,
            testPayloadBytes)
    when:
        sender.sendToSubscriber(testPublish, user)
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
    when: 'send publish complete'
        def publishComplete = PublishCompleteMqttInMessage
            .of(publish.messageId(), PublishCompletedReasonCode.SUCCESS)
        session
            .outProcessingPublishes()
            .apply(user, publishComplete)
    then:
        with(user.nextSentMessage(DisconnectMqtt5OutMessage)) {
          reasonCode() == DisconnectReasonCode.PROTOCOL_ERROR
          reason() == "Unexpected flow state:'$MqttMessageType.PUBLISH', expected:'$MqttMessageType.PUBLISH_RELEASE'"
        }
  }
}
