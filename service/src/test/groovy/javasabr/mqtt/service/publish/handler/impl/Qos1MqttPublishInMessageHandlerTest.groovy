package javasabr.mqtt.service.publish.handler.impl

import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.publishing.Publish
import javasabr.mqtt.model.reason.code.PublishAckReasonCode
import javasabr.mqtt.model.subscribtion.Subscription
import javasabr.mqtt.network.message.out.MqttOutMessage
import javasabr.mqtt.network.message.out.PublishAckMqtt5OutMessage
import javasabr.mqtt.network.message.out.PublishMqtt5OutMessage
import javasabr.mqtt.service.IntegrationServiceSpecification
import javasabr.mqtt.service.TestExternalMqttClient
import javasabr.rlib.collections.array.Array

class Qos1MqttPublishInMessageHandlerTest extends IntegrationServiceSpecification {

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
        inMessageTracker.add(expectedMessageId)
    when:
        publishInHandler.handle(client3, Publish.minimal(expectedMessageId, QoS.AT_MOST_ONCE, topicName, testPayload))
    then: 'sender should have feedback'
        def publishAck = client3.nextSentMessage(PublishAckMqtt5OutMessage)
        publishAck.reasonCode() == PublishAckReasonCode.SUCCESS
        publishAck.messageId() == expectedMessageId
        publishAck.reason() == ""
        publishAck.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        !inMessageTracker.isInUse(expectedMessageId)
    then: 'subscribers should receive the publish'
        def message1 = client1.nextSentMessage(PublishMqtt5OutMessage)
        message1.topicName() == topicName
        def message2 = client2.nextSentMessage(PublishMqtt5OutMessage)
        message2.topicName() == topicName
  }

  def "should not provide any feedback for accepted publish without any subscriber"() {
    given:
        def publishInHandler = new Qos1MqttPublishInMessageHandler(
            defaultSubscriptionService,
            defaultPublishDeliveringService,
            defaultMessageOutFactoryService)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def client = publisher.client() as TestExternalMqttClient
        def topicName = defaultTopicService.createTopicName(client, "Qos1MqttPublishInMessageHandlerTest/2")
        def expectedMessageId = 35
        def inMessageTracker = client
            .session()
            .inMessageTracker()
        inMessageTracker.add(expectedMessageId)
    when:
        publishInHandler.handle(client, Publish.minimal(expectedMessageId, QoS.AT_MOST_ONCE, topicName, testPayload))
    then: 'sender should have feedback that no matched subscribers'
        def publishAck = client.nextSentMessage(PublishAckMqtt5OutMessage)
        publishAck.reasonCode() == PublishAckReasonCode.NO_MATCHING_SUBSCRIBERS
        publishAck.messageId() == expectedMessageId
        publishAck.reason() == ""
        publishAck.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        !inMessageTracker.isInUse(expectedMessageId)
  }
}
