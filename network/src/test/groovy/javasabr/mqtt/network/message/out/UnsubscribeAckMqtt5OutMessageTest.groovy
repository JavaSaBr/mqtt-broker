package javasabr.mqtt.network.message.out

import javasabr.mqtt.network.message.in.UnsubscribeAckMqttInMessage
import javasabr.rlib.common.util.BufferUtils

class UnsubscribeAckMqtt5OutMessageTest extends BaseMqttOutMessageTest {

  def "should write packet correctly"() {
    given:
        def packet = new UnsubscribeAckMqtt5OutMessage(
            messageId,
            unsubscribeAckReasonCodes,
            userProperties,
            reasonString)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt5Connection, it)
        }
        def reader = new UnsubscribeAckMqttInMessage(0b1011_0000 as byte)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCodes() == unsubscribeAckReasonCodes
        reader.messageId() == messageId
        reader.userProperties() == userProperties
        reader.reason() == reasonString
  }
}
