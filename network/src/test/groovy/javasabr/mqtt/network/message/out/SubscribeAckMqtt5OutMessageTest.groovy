package javasabr.mqtt.network.message.out

import javasabr.mqtt.network.message.in.SubscribeAckMqttInMessage
import javasabr.rlib.common.util.BufferUtils

class SubscribeAckMqtt5OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def outMessage = new SubscribeAckMqtt5OutMessage(
            messageId,
            subscribeAckReasonCodes,
            userProperties,
            reasonString)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt5Connection, it)
        }
        def reader = new SubscribeAckMqttInMessage(0 as byte)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCodes() == subscribeAckReasonCodes
        reader.messageId() == messageId
        reader.userProperties() == userProperties
        reader.reason() == reasonString
  }
}
