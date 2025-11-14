package javasabr.mqtt.network.message.out

import javasabr.mqtt.network.message.in.UnsubscribeAckMqttInMessage
import javasabr.rlib.common.util.BufferUtils

class UnsubscribeAckMqtt5OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def outMessage = new UnsubscribeAckMqtt5OutMessage(
            messageId,
            unsubscribeAckReasonCodes,
            userProperties,
            reasonString)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt5Connection, it)
        }
        def reader = new UnsubscribeAckMqttInMessage(0 as byte)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCodes() == unsubscribeAckReasonCodes
        reader.messageId() == messageId
        reader.userProperties() == userProperties
        reader.reason() == reasonString
  }
}
