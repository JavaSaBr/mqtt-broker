package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.PayloadFormat
import javasabr.mqtt.model.QoS
import javasabr.mqtt.network.message.MqttMessageType
import javasabr.mqtt.network.message.in.PublishMqttInMessage
import javasabr.rlib.common.util.BufferUtils
import javasabr.rlib.common.util.NumberUtils

class PublishMqtt5OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def outMessage = new PublishMqtt5OutMessage(
            messageId,
            QoS.EXACTLY_ONCE,
            true,
            true,
            publishTopic,
            publishPayload,
            topicAlias,
            PayloadFormat.BINARY,
            responseTopic,
            correlationData,
            userProperties)
    when:
        def typeAndFlags = outMessage.messageTypeAndFlags()
        byte type = NumberUtils.getHighByteBits(typeAndFlags);
        byte info = NumberUtils.getLowByteBits(typeAndFlags);
    then:
        MqttMessageType.fromByte(type) == MqttMessageType.PUBLISH
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt5Connection, it)
        }
        def reader = new PublishMqttInMessage(info)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.exception() == null
        reader.messageId() == messageId
        reader.qos() == QoS.EXACTLY_ONCE
        reader.retain()
        reader.duplicate()
        reader.payload() == publishPayload
        reader.rawTopicName() == publishTopic.rawTopic()
        reader.userProperties() == userProperties
        reader.topicAlias() == topicAlias
        reader.payloadFormat() == PayloadFormat.BINARY
        reader.rawResponseTopicName() == responseTopic.rawTopic()
        reader.correlationData() == correlationData
    when:
        def outMessage2 = new PublishMqtt5OutMessage(
            messageId,
            QoS.AT_MOST_ONCE,
            false,
            false,
            publishTopic,
            publishPayload,
            topicAlias,
            PayloadFormat.UTF8_STRING,
            responseTopic,
            correlationData,
            userProperties)
        def typeAndFlags2 = outMessage2.messageTypeAndFlags()
        byte type2 = NumberUtils.getHighByteBits(typeAndFlags2);
        byte info2 = NumberUtils.getLowByteBits(typeAndFlags2);
    then:
        MqttMessageType.fromByte(type2) == MqttMessageType.PUBLISH
    when:
        def dataBuffer2 = BufferUtils.prepareBuffer(512) {
          outMessage2.write(defaultMqtt5Connection, it)
        }
        def reader2 = new PublishMqttInMessage(info2)
        def result2 = reader2.read(defaultMqtt5Connection, dataBuffer2, dataBuffer2.limit())
    then:
        result2
        reader2.exception() == null
        reader2.messageId() == 0
        reader2.qos() == QoS.AT_MOST_ONCE
        !reader2.retain()
        !reader2.duplicate()
        reader2.payload() == publishPayload
        reader2.rawTopicName() == publishTopic.rawTopic()
        reader2.userProperties() == userProperties
        reader2.topicAlias() == topicAlias
        reader2.payloadFormat() == PayloadFormat.UTF8_STRING
        reader2.rawResponseTopicName() == responseTopic.rawTopic()
        reader2.correlationData() == correlationData
  }
}
