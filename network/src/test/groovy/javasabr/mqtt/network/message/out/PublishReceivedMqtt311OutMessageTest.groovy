package javasabr.mqtt.network.message.out


import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode
import javasabr.mqtt.network.message.in.PublishReceivedMqttInMessage
import javasabr.rlib.common.util.BufferUtils
import javasabr.rlib.common.util.NumberUtils

class PublishReceivedMqtt311OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def outMessage = new PublishReceivedMqtt311OutMessage(messageId)
    when:
        def typeAndFlags = outMessage.messageTypeAndFlags()
        byte type = NumberUtils.getHighByteBits(typeAndFlags);
        byte info = NumberUtils.getLowByteBits(typeAndFlags);
    then:
        MqttMessageType.fromByte(type) == MqttMessageType.PUBLISH_RECEIVED
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt311Connection, it)
        }
        def reader = new PublishReceivedMqttInMessage(0 as byte)
        def result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCode() == PublishReceivedReasonCode.SUCCESS
        reader.messageId() == messageId
        reader.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        reader.reason() == null
  }
}
