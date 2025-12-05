package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.reason.code.PublishAckReasonCode
import javasabr.mqtt.network.message.in.PublishAckMqttInMessage
import javasabr.rlib.common.util.BufferUtils
import javasabr.rlib.common.util.NumberUtils

class PublishAckMqtt5OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def outMessage = new PublishAckMqtt5OutMessage(
            testMessageId,
            PublishAckReasonCode.NOT_AUTHORIZED,
            reasonString,
            testUserProperties)
    when:
        def typeAndFlags = outMessage.messageTypeAndFlags()
        byte type = NumberUtils.getHighByteBits(typeAndFlags);
        byte info = NumberUtils.getLowByteBits(typeAndFlags);
    then:
        MqttMessageType.fromByte(type) == MqttMessageType.PUBLISH_ACK
        info == PublishAckMqttInMessage.MESSAGE_FLAGS
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt5Connection, it)
        }
        def reader = new PublishAckMqttInMessage(info)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        with(reader) {
          exception() == null
          reasonCode() == PublishAckReasonCode.NOT_AUTHORIZED
          messageId() == testMessageId
          userProperties() == testUserProperties
          reason() == reasonString
        }
  }
}
