package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.QoS
import javasabr.mqtt.network.message.in.PublishMqttInMessage
import javasabr.rlib.common.util.BufferUtils

class PublishMqtt311OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def outMessage = new PublishMqtt311OutMessage(
            messageId,
            QoS.EXACTLY_ONCE,
            true,
            true,
            publishTopic.toString(),
            publishPayload)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt311Connection, it)
        }
        def inMessage = new PublishMqttInMessage(0b0011_1101 as byte)
        def result = inMessage.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.messageId() == messageId
        inMessage.qos() == QoS.EXACTLY_ONCE
        inMessage.retained()
        inMessage.duplicate()
        inMessage.payload() == publishPayload
        inMessage.rawTopicName() == publishTopic.rawTopic()
        inMessage.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
    when:
        outMessage = new PublishMqtt311OutMessage(
            messageId,
            QoS.AT_MOST_ONCE,
            false,
            false,
            publishTopic.toString(),
            publishPayload)
        dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt311Connection, it)
        }
        inMessage = new PublishMqttInMessage(0b0011_0000 as byte)
        result = inMessage.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.messageId() == 0
        inMessage.qos() == QoS.AT_MOST_ONCE
        !inMessage.retained()
        !inMessage.duplicate()
        inMessage.payload() == publishPayload
        inMessage.rawTopicName() == publishTopic.rawTopic()
        inMessage.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
  }
}
