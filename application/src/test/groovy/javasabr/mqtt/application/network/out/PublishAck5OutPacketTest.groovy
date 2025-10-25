package javasabr.mqtt.application.network.out

import javasabr.mqtt.model.reason.code.PublishAckReasonCode
import javasabr.mqtt.network.packet.in.PublishAckInPacket
import javasabr.mqtt.network.packet.out.PublishAck5OutPacket
import javasabr.rlib.common.util.BufferUtils

class PublishAck5OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {
    given:
        def packet = new PublishAck5OutPacket(
            packetId,
            PublishAckReasonCode.NOT_AUTHORIZED,
            userProperties,
            reasonString)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt5Connection, it)
        }
        def reader = new PublishAckInPacket(0b0100_0000 as byte)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCode() == PublishAckReasonCode.NOT_AUTHORIZED
        reader.messageId() == packetId
        reader.userProperties() == userProperties
        reader.reason() == reasonString
  }
}
