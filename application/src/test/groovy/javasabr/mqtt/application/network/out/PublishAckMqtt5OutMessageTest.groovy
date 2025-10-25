package javasabr.mqtt.application.network.out

import javasabr.mqtt.model.reason.code.PublishAckReasonCode
import javasabr.mqtt.network.message.in.PublishAckMqttInMessage
import javasabr.mqtt.network.message.out.PublishAckMqtt5OutMessage
import javasabr.rlib.common.util.BufferUtils

class PublishAckMqtt5OutMessageTest extends BaseOutPacketTest {

  def "should write packet correctly"() {
    given:
        def packet = new PublishAckMqtt5OutMessage(
            packetId,
            PublishAckReasonCode.NOT_AUTHORIZED,
            userProperties,
            reasonString)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt5Connection, it)
        }
        def reader = new PublishAckMqttInMessage(0b0100_0000 as byte)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCode() == PublishAckReasonCode.NOT_AUTHORIZED
        reader.messageId() == packetId
        reader.userProperties() == userProperties
        reader.reason() == reasonString
  }
}
