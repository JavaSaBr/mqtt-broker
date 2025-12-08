package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode
import javasabr.mqtt.network.message.in.PublishCompleteMqttInMessage
import javasabr.rlib.common.util.BufferUtils
import javasabr.rlib.common.util.NumberUtils

class PublishCompleteMqtt311OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def outMessage = new PublishCompleteMqtt311OutMessage(testMessageId)
    when:
        def typeAndFlags = outMessage.messageTypeAndFlags()
        byte type = NumberUtils.getHighByteBits(typeAndFlags);
        byte info = NumberUtils.getLowByteBits(typeAndFlags);
    then:
        MqttMessageType.fromByte(type) == MqttMessageType.PUBLISH_COMPLETE
        info == PublishCompleteMqttInMessage.MESSAGE_FLAGS
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt311Connection, it)
        }
        def reader = new PublishCompleteMqttInMessage(info)
        def result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        with(reader) {
          reasonCode() == PublishCompletedReasonCode.SUCCESS
          messageId() == testMessageId
          userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
          reason() == null
        }
  }
}
