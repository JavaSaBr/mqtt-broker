package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.reason.code.PublishReleaseReasonCode
import javasabr.mqtt.network.message.in.PublishReleaseMqttInMessage
import javasabr.rlib.common.util.BufferUtils
import javasabr.rlib.common.util.NumberUtils

class PublishReleaseMqtt311OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def outMessage = new PublishReleaseMqtt311OutMessage(testMessageId)
    when:
        def typeAndFlags = outMessage.messageTypeAndFlags()
        byte type = NumberUtils.getHighByteBits(typeAndFlags);
        byte info = NumberUtils.getLowByteBits(typeAndFlags);
    then:
        MqttMessageType.fromByte(type) == MqttMessageType.PUBLISH_RELEASE
        info == PublishReleaseMqttInMessage.MESSAGE_FLAGS
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt311Connection, it)
        }
        def reader = new PublishReleaseMqttInMessage(info)
        def result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        with(reader) {
          reasonCode() == PublishReleaseReasonCode.SUCCESS
          messageId() == testMessageId
          userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
          reason() == null
        }
  }
}
