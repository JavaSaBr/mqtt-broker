package javasabr.mqtt.service.publish.handler.impl

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.publishing.Publish
import javasabr.mqtt.model.subscriber.SingleSubscriber
import javasabr.mqtt.model.subscription.Subscription
import javasabr.mqtt.network.message.out.PublishMqtt5OutMessage
import javasabr.mqtt.service.TestExternalNetworkMqttUser
import javasabr.mqtt.service.publish.handler.PublishHandlingResult

class Qos0MqttPublishOutMessageHandlerTest extends QosMqttPublishOutMessageHandlerTest {

  def "should deliver publish to subscriber"() {
    given:
        def publishOutHandler = new Qos0MqttPublishOutMessageHandler(defaultMessageOutFactoryService)
        def connection = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = connection.user() as TestExternalNetworkMqttUser
        def testTopicName = defaultTopicService.createTopicName(user, "Qos0MqttPublishOutMessageHandlerTest/1")
        def topicFilter = defaultTopicService.createTopicFilter(user, "Qos0MqttPublishOutMessageHandlerTest/1")
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
          qos() == QoS.AT_MOST_ONCE
          !duplicate()
          payload() == testPayload
          topicName() == testTopicName
          messageId() == MqttProperties.MESSAGE_ID_IS_NOT_SET
          topicAlias() == MqttProperties.TOPIC_ALIAS_NOT_SET
        }
  }
}
