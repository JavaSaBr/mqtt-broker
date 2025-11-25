package javasabr.mqtt.service.publish.handler.impl

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.MqttProtocolErrors
import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.publishing.Publish
import javasabr.mqtt.model.reason.code.DisconnectReasonCode
import javasabr.mqtt.model.reason.code.PublishAckReasonCode
import javasabr.mqtt.model.subscription.Subscription
import javasabr.mqtt.network.message.out.DisconnectMqtt5OutMessage
import javasabr.mqtt.network.message.out.MqttOutMessage
import javasabr.mqtt.network.message.out.PublishAckMqtt5OutMessage
import javasabr.mqtt.network.message.out.PublishMqtt5OutMessage
import javasabr.mqtt.service.TestExternalMqttClient
import javasabr.rlib.collections.array.Array

class Qos1MqttPublishInMessageHandlerTest extends QosMqttPublishInMessageHandlerTest {

  def "should provide feedback for accepted publish with subscribers"() {
    given:
        def publishInHandler = new Qos1MqttPublishInMessageHandler(
            defaultSubscriptionService,
            defaultPublishDeliveringService,
            defaultMessageOutFactoryService)
        def subscriber1 = mockedExternalConnection(MqttVersion.MQTT_5)
        def subscriber2 = mockedExternalConnection(MqttVersion.MQTT_5)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def client1 = subscriber1.client() as TestExternalMqttClient
        def client2 = subscriber2.client() as TestExternalMqttClient
        def client3 = publisher.client() as TestExternalMqttClient
        def topicFilter = defaultTopicService.createTopicFilter(client1, "Qos1MqttPublishInMessageHandlerTest/1")
        def topicName = defaultTopicService.createTopicName(client1, "Qos1MqttPublishInMessageHandlerTest/1")
        def expectedMessageId = 35
        defaultSubscriptionService.subscribe(
            client1,
            client1.session(),
            Array.of(Subscription.minimal(topicFilter, QoS.AT_MOST_ONCE)))
        defaultSubscriptionService.subscribe(
            client2,
            client2.session(),
            Array.of(Subscription.minimal(topicFilter, QoS.AT_MOST_ONCE)))
        def inMessageTracker = client3
            .session()
            .inMessageTracker()
    when:
        publishInHandler.handle(client3, Publish.minimal(expectedMessageId, QoS.AT_MOST_ONCE, topicName, testPayload))
    then: 'sender should have feedback'
        def publishAck = client3.nextSentMessage(PublishAckMqtt5OutMessage)
        publishAck.reasonCode() == PublishAckReasonCode.SUCCESS
        publishAck.messageId() == expectedMessageId
        publishAck.reason() == null
        publishAck.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        inMessageTracker.stored(expectedMessageId) == null
    then: 'subscribers should receive the publish'
        def message1 = client1.nextSentMessage(PublishMqtt5OutMessage)
        message1.topicName() == topicName
        def message2 = client2.nextSentMessage(PublishMqtt5OutMessage)
        message2.topicName() == topicName
  }

  def "should provide feedback for accepted publish without any subscriber"() {
    given:
        def publishInHandler = new Qos1MqttPublishInMessageHandler(
            defaultSubscriptionService,
            defaultPublishDeliveringService,
            defaultMessageOutFactoryService)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def client = publisher.client() as TestExternalMqttClient
        def topicName = defaultTopicService.createTopicName(client, "Qos1MqttPublishInMessageHandlerTest/2")
        def expectedMessageId = 35
        def session = client.session()
        def inMessageTracker = session.inMessageTracker()
    when:
        publishInHandler.handle(client, Publish.minimal(expectedMessageId, QoS.AT_MOST_ONCE, topicName, testPayload))
    then: 'sender should have feedback that no matched subscribers'
        def publishAck = client.nextSentMessage(PublishAckMqtt5OutMessage)
        publishAck.reasonCode() == PublishAckReasonCode.NO_MATCHING_SUBSCRIBERS
        publishAck.messageId() == expectedMessageId
        publishAck.reason() == null
        publishAck.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        inMessageTracker.stored(expectedMessageId) == null
  }

  def "should disconnect by reason that message id is missed"() {
    given:
        def publishInHandler = new Qos1MqttPublishInMessageHandler(
            defaultSubscriptionService,
            defaultPublishDeliveringService,
            defaultMessageOutFactoryService)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def client = publisher.client() as TestExternalMqttClient
        def topicName = defaultTopicService.createTopicName(client, "Qos1MqttPublishInMessageHandlerTest/3")
    when:
        publishInHandler.handle(client, Publish.minimal(
            MqttProperties.MESSAGE_ID_IS_NOT_SET,
            QoS.AT_MOST_ONCE,
            topicName,
            testPayload))
    then:
        def disconnect = client.nextSentMessage(DisconnectMqtt5OutMessage)
        disconnect.reasonCode() == DisconnectReasonCode.PROTOCOL_ERROR
        disconnect.reason() == MqttProtocolErrors.MISSED_REQUIRED_MESSAGE_ID
        disconnect.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
  }

  def "should provide feedback that message id is already used"() {
    given:
        def publishInHandler = new Qos1MqttPublishInMessageHandler(
            defaultSubscriptionService,
            defaultPublishDeliveringService,
            defaultMessageOutFactoryService)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def client = publisher.client() as TestExternalMqttClient
        def topicName = defaultTopicService.createTopicName(client, "Qos1MqttPublishInMessageHandlerTest/4")
        def expectedMessageId = 35
        def session = client.session()
        def inMessageTracker = session.inMessageTracker()
        inMessageTracker.add(expectedMessageId, MqttMessageType.SUBSCRIBE)
    when:
        publishInHandler.handle(client, Publish.minimal(expectedMessageId, QoS.AT_MOST_ONCE, topicName, testPayload))
    then:
        def publishAck = client.nextSentMessage(PublishAckMqtt5OutMessage)
        publishAck.reasonCode() == PublishAckReasonCode.PACKET_IDENTIFIER_IN_USE
        publishAck.reason() == null
        publishAck.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        def trackedMessageMeta = inMessageTracker.stored(expectedMessageId)
        trackedMessageMeta != null
        trackedMessageMeta.messageType() == MqttMessageType.SUBSCRIBE
  }

  def "should skip handling duplicated publish"() {
    given:
        def publishInHandler = new Qos1MqttPublishInMessageHandler(
            defaultSubscriptionService,
            defaultPublishDeliveringService,
            defaultMessageOutFactoryService)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def client = publisher.client() as TestExternalMqttClient
        def topicName = defaultTopicService.createTopicName(client, "Qos1MqttPublishInMessageHandlerTest/5")
        def expectedMessageId = 35
        def session = client.session()
        def inMessageTracker = session.inMessageTracker()
        inMessageTracker.add(expectedMessageId, MqttMessageType.PUBLISH)
    when:
        publishInHandler.handle(client, Publish
            .minimal(expectedMessageId, QoS.AT_MOST_ONCE, topicName, testPayload)
            .withDuplicated())
    then:
        client.isEmpty()
  }
}
