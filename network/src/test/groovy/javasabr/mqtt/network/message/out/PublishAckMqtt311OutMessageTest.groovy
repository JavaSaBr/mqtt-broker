package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.reason.code.PublishAckReasonCode
import javasabr.mqtt.network.message.in.PublishAckMqttInMessage
import javasabr.rlib.common.util.BufferUtils

class PublishAckMqtt311OutMessageTest extends BaseMqttOutMessageTest {

  def "should write packet correctly"() {
    given:
        def packet = new PublishAckMqtt311OutMessage(messageId)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt311Connection, it)
        }
        def reader = new PublishAckMqttInMessage(0b0100_0000 as byte)
        def result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCode() == PublishAckReasonCode.SUCCESS
        reader.messageId() == messageId
        reader.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        reader.reason() == ""
  }
}
