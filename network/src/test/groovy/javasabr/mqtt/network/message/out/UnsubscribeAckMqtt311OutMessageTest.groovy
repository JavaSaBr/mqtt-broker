package javasabr.mqtt.network.message.out

import javasabr.mqtt.network.message.MqttMessageType
import javasabr.mqtt.network.message.in.MqttInMessage
import javasabr.mqtt.network.message.in.UnsubscribeAckMqttInMessage
import javasabr.rlib.common.util.BufferUtils
import javasabr.rlib.common.util.NumberUtils

class UnsubscribeAckMqtt311OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def outMessage = new UnsubscribeAckMqtt311OutMessage(messageId)
    when:
        def typeAndFlags = outMessage.messageTypeAndFlags()
        byte type = NumberUtils.getHighByteBits(typeAndFlags);
        byte info = NumberUtils.getLowByteBits(typeAndFlags);
    then:
        MqttMessageType.fromByte(type) == MqttMessageType.UNSUBSCRIBE_ACK
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt311Connection, it)
        }
        def reader = new UnsubscribeAckMqttInMessage(info)
        def result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.exception() == null
        reader.reasonCodes() == UnsubscribeAckMqttInMessage.EMPTY_REASON_CODES
        reader.messageId() == messageId
        reader.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
        reader.reason() == ""
  }
}
