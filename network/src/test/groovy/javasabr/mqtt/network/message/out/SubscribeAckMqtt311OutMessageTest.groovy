package javasabr.mqtt.network.message.out

import javasabr.mqtt.network.message.MqttMessageType
import javasabr.mqtt.network.message.in.MqttInMessage
import javasabr.mqtt.network.message.in.SubscribeAckMqttInMessage
import javasabr.rlib.common.util.BufferUtils
import javasabr.rlib.common.util.NumberUtils

class SubscribeAckMqtt311OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def outMessage = new SubscribeAckMqtt311OutMessage(messageId, subscribeAckReasonCodes)
    when:
        def typeAndFlags = outMessage.messageTypeAndFlags()
        byte type = NumberUtils.getHighByteBits(typeAndFlags);
        byte info = NumberUtils.getLowByteBits(typeAndFlags);
    then:
        MqttMessageType.fromByte(type) == MqttMessageType.SUBSCRIBE_ACK
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt311Connection, it)
        }
        def reader = new SubscribeAckMqttInMessage(info)
        def result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.exception() == null
        reader.reasonCodes() == subscribeAckReasonCodes
        reader.messageId() == messageId
        reader.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
        reader.reason() == ""
  }
}
