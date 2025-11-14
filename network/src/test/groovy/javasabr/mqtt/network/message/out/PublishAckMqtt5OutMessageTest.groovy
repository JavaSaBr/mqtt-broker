package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.reason.code.PublishAckReasonCode
import javasabr.mqtt.network.message.in.PublishAckMqttInMessage
import javasabr.rlib.common.util.BufferUtils

class PublishAckMqtt5OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def outMessage = new PublishAckMqtt5OutMessage(
            messageId,
            PublishAckReasonCode.NOT_AUTHORIZED,
            userProperties,
            reasonString)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt5Connection, it)
        }
        def reader = new PublishAckMqttInMessage(0b0100_0000 as byte)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCode() == PublishAckReasonCode.NOT_AUTHORIZED
        reader.messageId() == messageId
        reader.userProperties() == userProperties
        reader.reason() == reasonString
  }
}
