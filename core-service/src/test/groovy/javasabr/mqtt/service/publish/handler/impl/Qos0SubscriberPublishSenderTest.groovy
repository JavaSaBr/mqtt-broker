package javasabr.mqtt.service.publish.handler.impl

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.publishing.Publish
import javasabr.mqtt.network.message.out.PublishMqtt5OutMessage
import javasabr.mqtt.service.TestExternalNetworkMqttUser

class Qos0SubscriberPublishSenderTest extends QosSubscriberPublishSenderTest {

  def "should deliver publish to subscriber"() {
    given:
        def publishOutHandler = new Qos0SubscriberPublishSender(defaultMessageOutFactoryService)
        def connection = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = connection.user() as TestExternalNetworkMqttUser
        def testTopicName = defaultTopicService.createTopicName(user, "Qos0MqttPublishOutMessageHandlerTest/1")
        def originalMessageId = 60
        def testPublish = Publish.minimal(originalMessageId, QoS.EXACTLY_ONCE, testTopicName, testPayload)
            .withDuplicated()
    when:
        publishOutHandler.handle(testPublish, user)
    then:
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
