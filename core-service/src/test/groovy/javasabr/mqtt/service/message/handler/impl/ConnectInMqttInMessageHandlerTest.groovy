package javasabr.mqtt.service.message.handler.impl

import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.reason.code.PublishAckReasonCode
import javasabr.mqtt.network.message.in.PublishMqttInMessage
import javasabr.mqtt.network.message.out.PublishAckMqtt5OutMessage
import javasabr.mqtt.service.IntegrationServiceSpecification
import javasabr.mqtt.service.TestExternalNetworkMqttUser

class ConnectInMqttInMessageHandlerTest extends IntegrationServiceSpecification {

  def "should update topic name alias mapping in session in MQTT 5"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def messageHandler = new ConnectInMqttInMessageHandler(
            publishReceivingService,
            defaultMessageOutFactoryService,
            defaultTopicService,
            disabledAclService,
            publishInFieldValidators)
        def expectedMessageId = 15
        def expectedTopicAlias = 5
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
        def expectedTopicName = defaultTopicService.createTopicName(mqttUser, "topic/name1")
    when:
        def publishMessage = new PublishMqttInMessage(0b0110_0011 as byte) {{
          messageId = expectedMessageId
          payload = [1, 2, 3, 4, 5]
          rawTopicName = expectedTopicName.rawTopic()
          topicAlias = expectedTopicAlias
        }}
        messageHandler.processValidMessage(mqttConnection, publishMessage)
    then:
        def publishAck = mqttUser.nextSentMessage(PublishAckMqtt5OutMessage)
        publishAck.reasonCode() == PublishAckReasonCode.NO_MATCHING_SUBSCRIBERS
        publishAck.reason() == null
        mqttUser.session()
            .topicNameMapping()
            .resolve(expectedTopicAlias) == expectedTopicName
  }
}
