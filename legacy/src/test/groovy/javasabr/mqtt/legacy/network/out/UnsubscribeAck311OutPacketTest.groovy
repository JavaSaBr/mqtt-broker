package javasabr.mqtt.legacy.network.out

import javasabr.mqtt.legacy.network.packet.in.UnsubscribeAckInPacket
import javasabr.mqtt.legacy.network.packet.out.UnsubscribeAck311OutPacket
import javasabr.mqtt.model.data.type.StringPair
import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class UnsubscribeAck311OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {

    given:
        def packet = new UnsubscribeAck311OutPacket(packetId)
    when:

        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(it)
        }

        def reader = new UnsubscribeAckInPacket(0b1011_0000 as byte)
        def result = reader.read(mqtt311Connection, dataBuffer, dataBuffer.limit())

    then:
        result
        reader.reasonCodes == Array.empty(UnsubscribeAckReasonCode)
        reader.packetId == packetId
        reader.userProperties == Array.empty(StringPair)
        reader.reason == ""
  }
}
