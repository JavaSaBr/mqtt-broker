package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.network.message.in.PublishMqttInMessage
import javasabr.rlib.common.util.BufferUtils
import javasabr.rlib.common.util.NumberUtils

class PublishMqtt311OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def outMessage = new PublishMqtt311OutMessage(
            messageId,
            QoS.EXACTLY_ONCE,
            true,
            true,
            publishTopic,
            publishPayload)
    when:
        def typeAndFlags = outMessage.messageTypeAndFlags()
        byte type = NumberUtils.getHighByteBits(typeAndFlags);
        byte info = NumberUtils.getLowByteBits(typeAndFlags);
    then:
        MqttMessageType.fromByte(type) == MqttMessageType.PUBLISH
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt311Connection, it)
        }
        def reader = new PublishMqttInMessage(info)
        def result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.exception() == null
        reader.messageId() == messageId
        reader.qos() == QoS.EXACTLY_ONCE
        reader.retain()
        reader.duplicate()
        reader.payload() == publishPayload
        reader.rawTopicName() == publishTopic.rawTopic()
        reader.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
    when:
        def outMessage2 = new PublishMqtt311OutMessage(
            messageId,
            QoS.AT_MOST_ONCE,
            false,
            false,
            publishTopic,
            publishPayload)
        def typeAndFlags2 = outMessage2.messageTypeAndFlags()
        byte type2 = NumberUtils.getHighByteBits(typeAndFlags2);
        byte info2 = NumberUtils.getLowByteBits(typeAndFlags2);
    then:
        MqttMessageType.fromByte(type2) == MqttMessageType.PUBLISH
    when:
        def dataBuffer2 = BufferUtils.prepareBuffer(512) {
          outMessage2.write(defaultMqtt311Connection, it)
        }
        def reader2 = new PublishMqttInMessage(info2)
        def result2 = reader2.read(defaultMqtt311Connection, dataBuffer2, dataBuffer2.limit())
    then:
        result2
        reader2.exception() == null
        reader2.messageId() == 0
        reader2.qos() == QoS.AT_MOST_ONCE
        !reader2.retain()
        !reader2.duplicate()
        reader2.payload() == publishPayload
        reader2.rawTopicName() == publishTopic.rawTopic()
        reader2.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
  }
}
