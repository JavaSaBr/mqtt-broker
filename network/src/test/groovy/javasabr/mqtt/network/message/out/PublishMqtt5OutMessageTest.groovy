package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.PayloadFormat
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.network.message.in.PublishMqttInMessage
import javasabr.rlib.common.util.BufferUtils
import javasabr.rlib.common.util.NumberUtils

class PublishMqtt5OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def outMessage = new PublishMqtt5OutMessage(
            testMessageId,
            QoS.EXACTLY_ONCE,
            true,
            true,
            publishTopic,
            publishPayload,
            testTopicAlias,
            PayloadFormat.BINARY,
            testResponseTopic,
            testCorrelationData,
            testUserProperties)
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
        with(reader) {
          exception() == null
          messageId() == testMessageId
          qos() == QoS.EXACTLY_ONCE
          retain()
          duplicate()
          payload() == publishPayload
          rawTopicName() == publishTopic.rawTopic()
          userProperties() == testUserProperties
          topicAlias() == testTopicAlias
          payloadFormat() == PayloadFormat.BINARY
          rawResponseTopicName() == testResponseTopic.rawTopic()
          correlationData() == testCorrelationData
        }
    when:
        def outMessage2 = new PublishMqtt5OutMessage(
            testMessageId,
            QoS.AT_MOST_ONCE,
            false,
            false,
            publishTopic,
            publishPayload,
            testTopicAlias,
            PayloadFormat.UTF8_STRING,
            testResponseTopic,
            testCorrelationData,
            testUserProperties)
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
        with(reader2) {
          exception() == null
          messageId() == 0
          qos() == QoS.AT_MOST_ONCE
          !retain()
          !duplicate()
          payload() == publishPayload
          rawTopicName() == publishTopic.rawTopic()
          userProperties() == testUserProperties
          topicAlias() == testTopicAlias
          payloadFormat() == PayloadFormat.UTF8_STRING
          rawResponseTopicName() == testResponseTopic.rawTopic()
          correlationData() == testCorrelationData
        }
  }
}
