package javasabr.mqtt.application.network.out

import javasabr.mqtt.model.reason.code.PublishAckReasonCode
import javasabr.mqtt.network.packet.in.PublishAckInPacket
import javasabr.mqtt.network.packet.out.PublishAck311OutPacket
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class PublishAck311OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {
    given:
        def packet = new PublishAck311OutPacket(packetId)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt311Connection, it)
        }
        def reader = new PublishAckInPacket(0b0100_0000 as byte)
        def result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCode() == PublishAckReasonCode.SUCCESS
        reader.messageId() == packetId
        reader.userProperties() == Array.empty()
        reader.reason() == ""
  }
}
