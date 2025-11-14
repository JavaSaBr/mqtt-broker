package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.PayloadFormat
import javasabr.mqtt.model.QoS
import javasabr.mqtt.network.message.in.PublishMqttInMessage
import javasabr.rlib.common.util.BufferUtils

class PublishMqtt5OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def outMessage = new PublishMqtt5OutMessage(
            messageId,
            QoS.EXACTLY_ONCE,
            true,
            true,
            publishTopic.toString(),
            publishPayload,
            topicAlias,
            false,
            responseTopic,
            correlationData,
            userProperties)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt5Connection, it)
        }
        def inMessage = new PublishMqttInMessage(0b0011_1101 as byte)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.messageId() == messageId
        inMessage.qos() == QoS.EXACTLY_ONCE
        inMessage.retain()
        inMessage.duplicate()
        inMessage.payload() == publishPayload
        inMessage.rawTopicName() == publishTopic.rawTopic()
        inMessage.userProperties() == userProperties
        inMessage.topicAlias() == topicAlias
        inMessage.payloadFormat() == PayloadFormat.BINARY
        inMessage.rawResponseTopicName() == responseTopic
        inMessage.correlationData() == correlationData
    when:
        outMessage = new PublishMqtt5OutMessage(
            messageId,
            QoS.AT_MOST_ONCE,
            false,
            false,
            publishTopic.toString(),
            publishPayload,
            topicAlias,
            true,
            responseTopic,
            correlationData,
            userProperties)

        dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt5Connection, it)
        }

        inMessage = new PublishMqttInMessage(0b0011_0000 as byte)
        result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.messageId() == 0
        inMessage.qos() == QoS.AT_MOST_ONCE
        !inMessage.retain()
        !inMessage.duplicate()
        inMessage.payload() == publishPayload
        inMessage.rawTopicName() == publishTopic.rawTopic()
        inMessage.userProperties() == userProperties
        inMessage.topicAlias() == topicAlias
        inMessage.payloadFormat() == PayloadFormat.UTF8_STRING
        inMessage.rawResponseTopicName() == responseTopic
        inMessage.correlationData() == correlationData
  }
}
