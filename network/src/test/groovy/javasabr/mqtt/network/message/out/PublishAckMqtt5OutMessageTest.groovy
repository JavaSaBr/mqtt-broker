package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.reason.code.PublishAckReasonCode
import javasabr.mqtt.network.message.MqttMessageType
import javasabr.mqtt.network.message.in.PublishAckMqttInMessage
import javasabr.rlib.common.util.BufferUtils
import javasabr.rlib.common.util.NumberUtils

class PublishAckMqtt5OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def outMessage = new PublishAckMqtt5OutMessage(
            messageId,
            PublishAckReasonCode.NOT_AUTHORIZED,
            userProperties,
            reasonString)
    when:
        def typeAndFlags = outMessage.messageTypeAndFlags()
        byte type = NumberUtils.getHighByteBits(typeAndFlags);
        byte info = NumberUtils.getLowByteBits(typeAndFlags);
    then:
        MqttMessageType.fromByte(type) == MqttMessageType.PUBLISH_ACK
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt5Connection, it)
        }
        def reader = new PublishAckMqttInMessage(info)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.exception() == null
        reader.reasonCode() == PublishAckReasonCode.NOT_AUTHORIZED
        reader.messageId() == messageId
        reader.userProperties() == userProperties
        reader.reason() == reasonString
  }
}
