package javasabr.mqtt.service.publish.sender

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.QoS
import javasabr.mqtt.network.message.out.PublishMqtt5OutMessage
import javasabr.mqtt.service.TestExternalNetworkMqttUser

import static javasabr.mqtt.model.subscription.TestPublishFactory.incomingPublish

class Qos0SubscriberPublishSenderTest extends QosSubscriberPublishSenderTest {

  def "should deliver publish to subscriber"() {
    given:
        def sender = new Qos0SubscriberPublishSender(defaultMessageOutFactoryService)
        def connection = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = connection.user() as TestExternalNetworkMqttUser
        def testTopicName = defaultTopicService.createTopicName(user, "Qos0MqttPublishOutMessageHandlerTest/1")
        def originalMessageId = 60
        def testPublish = incomingPublish(originalMessageId, QoS.EXACTLY_ONCE, testTopicName, testPayload)
            .withDuplicated()
    when:
        sender.sendToSubscriber(testPublish, user)
    then:
        with(user.nextSentMessage(PublishMqtt5OutMessage)) {
          qos() == QoS.AT_MOST_ONCE
          !duplicate()
          data() == testPayload
          topicName() == testTopicName
          messageId() == MqttProperties.MESSAGE_ID_IS_NOT_SET
          topicAlias() == MqttProperties.TOPIC_ALIAS_NOT_SET
        }
  }
}
