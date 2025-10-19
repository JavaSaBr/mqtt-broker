package javasabr.mqtt.application.network.out

import javasabr.mqtt.network.packet.in.PublishCompleteInPacket
import javasabr.mqtt.network.packet.out.PublishComplete5OutPacket
import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode
import javasabr.rlib.common.util.BufferUtils

class PublishComplete5OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {
    given:
        def packet = new PublishComplete5OutPacket(
            packetId,
            PublishCompletedReasonCode.PACKET_IDENTIFIER_NOT_FOUND,
            userProperties,
            reasonString)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt5Connection, it)
        }
        def reader = new PublishCompleteInPacket(0b0111_0000 as byte)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCode() == PublishCompletedReasonCode.PACKET_IDENTIFIER_NOT_FOUND
        reader.packetId() == packetId
        reader.userProperties() == userProperties
        reader.reason() == reasonString
  }
}
