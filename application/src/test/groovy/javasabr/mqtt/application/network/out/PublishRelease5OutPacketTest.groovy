package javasabr.mqtt.application.network.out

import javasabr.mqtt.model.reason.code.PublishReleaseReasonCode
import javasabr.mqtt.network.packet.in.PublishReleaseInPacket
import javasabr.mqtt.network.packet.out.PublishRelease5OutPacket
import javasabr.rlib.common.util.BufferUtils

class PublishRelease5OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {
    given:
        def packet = new PublishRelease5OutPacket(
            packetId,
            PublishReleaseReasonCode.PACKET_IDENTIFIER_NOT_FOUND,
            userProperties,
            reasonString)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt5Connection, it)
        }
        def reader = new PublishReleaseInPacket(0b0110_0000 as byte)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCode() == PublishReleaseReasonCode.PACKET_IDENTIFIER_NOT_FOUND
        reader.packetId() == packetId
        reader.userProperties() == userProperties
        reader.reason() == reasonString
  }
}
