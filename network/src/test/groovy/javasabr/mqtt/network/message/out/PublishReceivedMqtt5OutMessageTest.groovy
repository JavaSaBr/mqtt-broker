package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode
import javasabr.mqtt.network.message.in.PublishReceivedMqttInMessage
import javasabr.rlib.common.util.BufferUtils

class PublishReceivedMqtt5OutMessageTest extends BaseMqttOutMessageTest {

  def "should write packet correctly"() {
    given:
        def packet = new PublishReceivedMqtt5OutMessage(
            messageId,
            PublishReceivedReasonCode.UNSPECIFIED_ERROR,
            userProperties,
            reasonString)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt5Connection, it)
        }
        def reader = new PublishReceivedMqttInMessage(0b0101_0000 as byte)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCode() == PublishReceivedReasonCode.UNSPECIFIED_ERROR
        reader.messageId() == messageId
        reader.userProperties() == userProperties
        reader.reason() == reasonString
  }
}
