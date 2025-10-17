package javasabr.mqtt.application.network.out

import javasabr.mqtt.network.packet.in.PublishAckInPacket
import javasabr.mqtt.network.packet.out.PublishAck311OutPacket
import javasabr.mqtt.model.reason.code.PublishAckReasonCode
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class PublishAck311OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {

    given:
        def packet = new PublishAck311OutPacket(packetId)
    when:

        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(mqtt311Connection, it)
        }

        def reader = new PublishAckInPacket(0b0100_0000 as byte)
        def result = reader.read(mqtt311Connection, dataBuffer, dataBuffer.limit())

    then:
        result
        reader.reasonCode == PublishAckReasonCode.SUCCESS
        reader.packetId == packetId
        reader.userProperties == Array.empty()
        reader.reason == ""
  }
}
