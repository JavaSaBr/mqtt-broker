package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode
import javasabr.mqtt.network.message.in.PublishReceivedMqttInMessage
import javasabr.rlib.common.util.BufferUtils
import javasabr.rlib.common.util.NumberUtils

class PublishReceivedMqtt5OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def outMessage = new PublishReceivedMqtt5OutMessage(
            testMessageId,
            PublishReceivedReasonCode.UNSPECIFIED_ERROR,
            testUserProperties,
            reasonString)
    when:
        def typeAndFlags = outMessage.messageTypeAndFlags()
        byte type = NumberUtils.getHighByteBits(typeAndFlags);
        byte info = NumberUtils.getLowByteBits(typeAndFlags);
    then:
        MqttMessageType.fromByte(type) == MqttMessageType.PUBLISH_RECEIVED
        info == PublishReceivedMqttInMessage.MESSAGE_FLAGS
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt5Connection, it)
        }
        def reader = new PublishReceivedMqttInMessage(info)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        with(reader) {
          reasonCode() == PublishReceivedReasonCode.UNSPECIFIED_ERROR
          messageId() == testMessageId
          userProperties() == testUserProperties
          reason() == reasonString
        }
  }
}
