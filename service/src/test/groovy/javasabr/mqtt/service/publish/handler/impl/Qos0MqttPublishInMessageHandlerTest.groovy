package javasabr.mqtt.service.publish.handler.impl

import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.publishing.Publish
import javasabr.mqtt.model.subscribtion.Subscription
import javasabr.mqtt.network.message.out.PublishMqtt5OutMessage
import javasabr.mqtt.service.IntegrationServiceSpecification
import javasabr.mqtt.service.TestExternalMqttClient
import javasabr.rlib.collections.array.Array

class Qos0MqttPublishInMessageHandlerTest extends IntegrationServiceSpecification {

  def "should not provide any feedback for accepted publish"() {
    given:
        def publishInHandler = new Qos0MqttPublishInMessageHandler(
            defaultSubscriptionService,
            defaultPublishDeliveringService)
        def subscriber1 = mockedExternalConnection(MqttVersion.MQTT_5)
        def subscriber2 = mockedExternalConnection(MqttVersion.MQTT_5)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def client1 = subscriber1.client() as TestExternalMqttClient
        def client2 = subscriber2.client() as TestExternalMqttClient
        def client3 = publisher.client() as TestExternalMqttClient
        def topicFilter = defaultTopicService.createTopicFilter(client1, "Qos0MqttPublishInMessageHandlerTest/1")
        def topicName = defaultTopicService.createTopicName(client1, "Qos0MqttPublishInMessageHandlerTest/1")
        defaultSubscriptionService.subscribe(
            client1,
            client1.session(),
            Array.of(Subscription.minimal(topicFilter, QoS.AT_MOST_ONCE)))
        defaultSubscriptionService.subscribe(
            client2,
            client2.session(),
            Array.of(Subscription.minimal(topicFilter, QoS.AT_MOST_ONCE)))
    when:
        publishInHandler.handle(client3, Publish.minimal(QoS.AT_MOST_ONCE, topicName, testPayload))
    then: 'sender should not have any feedback'
        client3.isEmpty()
    then: 'subscribers should receive the publish'
        def message1 = client1.nextSentMessage(PublishMqtt5OutMessage)
        message1.topicName() == topicName
        def message2 = client2.nextSentMessage(PublishMqtt5OutMessage)
        message2.topicName() == topicName
  }
}
