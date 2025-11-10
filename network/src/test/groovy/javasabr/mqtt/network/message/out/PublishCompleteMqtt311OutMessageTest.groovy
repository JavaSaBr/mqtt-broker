package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode
import javasabr.mqtt.network.message.in.PublishCompleteMqttInMessage
import javasabr.rlib.common.util.BufferUtils

class PublishCompleteMqtt311OutMessageTest extends BaseMqttOutMessageTest {

  def "should write packet correctly"() {
    given:
        def packet = new PublishCompleteMqtt311OutMessage(messageId)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt311Connection, it)
        }
        def reader = new PublishCompleteMqttInMessage(0b0111_0000 as byte)
        def result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCode() == PublishCompletedReasonCode.SUCCESS
        reader.messageId() == messageId
        reader.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        reader.reason() == ""
  }
}
