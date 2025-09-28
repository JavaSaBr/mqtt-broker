package javasabr.mqtt.legacy.network.out

import javasabr.mqtt.legacy.network.packet.in.UnsubscribeAckInPacket
import javasabr.mqtt.legacy.network.packet.out.UnsubscribeAck5OutPacket
import javasabr.rlib.common.util.BufferUtils

class UnsubscribeAck5OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {

    given:

        def packet = new UnsubscribeAck5OutPacket(
            packetId,
            unsubscribeAckReasonCodes,
            userProperties,
            reasonString
        )

    when:

        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(it)
        }

        def reader = new UnsubscribeAckInPacket(0b1011_0000 as byte)
        def result = reader.read(mqtt5Connection, dataBuffer, dataBuffer.limit())

    then:
        result
        reader.reasonCodes == unsubscribeAckReasonCodes
        reader.packetId == packetId
        reader.userProperties == userProperties
        reader.reason == reasonString
  }
}
