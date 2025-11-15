package javasabr.mqtt.network.message.out

import javasabr.mqtt.network.message.MqttMessageType
import javasabr.mqtt.network.message.in.SubscribeAckMqttInMessage
import javasabr.rlib.common.util.BufferUtils
import javasabr.rlib.common.util.NumberUtils

class SubscribeAckMqtt5OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def outMessage = new SubscribeAckMqtt5OutMessage(
            messageId,
            subscribeAckReasonCodes,
            userProperties,
            reasonString)
    when:
        def typeAndFlags = outMessage.messageTypeAndFlags()
        byte type = NumberUtils.getHighByteBits(typeAndFlags);
        byte info = NumberUtils.getLowByteBits(typeAndFlags);
    then:
        MqttMessageType.fromByte(type) == MqttMessageType.SUBSCRIBE_ACK
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt5Connection, it)
        }
        def reader = new SubscribeAckMqttInMessage(info)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.exception() == null
        reader.reasonCodes() == subscribeAckReasonCodes
        reader.messageId() == messageId
        reader.userProperties() == userProperties
        reader.reason() == reasonString
  }
}
