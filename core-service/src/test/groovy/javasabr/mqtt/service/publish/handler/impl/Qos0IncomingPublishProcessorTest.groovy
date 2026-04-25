package javasabr.mqtt.service.publish.handler.impl

import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.publishing.Publish
import javasabr.mqtt.model.subscription.Subscription
import javasabr.mqtt.network.message.out.PublishMqtt5OutMessage
import javasabr.mqtt.service.TestExternalNetworkMqttUser
import javasabr.rlib.collections.array.Array

class Qos0IncomingPublishProcessorTest extends QosIncomingPublishProcessorTest {

  def "should not provide any feedback for accepted publish with subscribers"() {
    given:
        def publishInHandler = new Qos0IncomingPublishProcessor(
            defaultSubscriptionService,
            defaultPublishDeliveringService,
            defaultMessageOutFactoryService,
            inMemoryRetainMessageService)
        def subscriber1 = mockedExternalConnection(MqttVersion.MQTT_5)
        def subscriber2 = mockedExternalConnection(MqttVersion.MQTT_5)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def user1 = subscriber1.user() as TestExternalNetworkMqttUser
        def user2 = subscriber2.user() as TestExternalNetworkMqttUser
        def user3 = publisher.user() as TestExternalNetworkMqttUser
        def topicFilter = defaultTopicService.createTopicFilter(user1, "Qos0MqttPublishInMessageHandlerTest/1")
        def expectedTopicName = defaultTopicService.createTopicName(user1, "Qos0MqttPublishInMessageHandlerTest/1")
        defaultSubscriptionService.subscribe(
            user1,
            user1.session(),
            Array.of(Subscription.minimal(topicFilter, QoS.AT_MOST_ONCE)))
        defaultSubscriptionService.subscribe(
            user2,
            user2.session(),
            Array.of(Subscription.minimal(topicFilter, QoS.AT_MOST_ONCE)))
    when:
        publishInHandler.process(user3, Publish.minimal(QoS.AT_MOST_ONCE, expectedTopicName, testPayload))
    then: 'sender should not have any feedback'
        user3.isEmpty()
    then: 'subscribers should receive the publish'
        with(user1.nextSentMessage(PublishMqtt5OutMessage)) {
          topicName() == expectedTopicName
        }
        with(user2.nextSentMessage(PublishMqtt5OutMessage)) {
          topicName() == expectedTopicName
        }
  }

  def "should not provide any feedback for accepted publish without any subscriber"() {
    given:
        def publishInHandler = new Qos0IncomingPublishProcessor(
            defaultSubscriptionService,
            defaultPublishDeliveringService,
            defaultMessageOutFactoryService,
            inMemoryRetainMessageService)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = publisher.user() as TestExternalNetworkMqttUser
        def topicName = defaultTopicService.createTopicName(user, "Qos0MqttPublishInMessageHandlerTest/2")
    when:
        publishInHandler.process(user, Publish.minimal(QoS.AT_MOST_ONCE, topicName, testPayload))
    then: 'sender should not have any feedback'
        user.isEmpty()
  }
}
