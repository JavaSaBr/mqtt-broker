package javasabr.mqtt.application.network.out

import javasabr.mqtt.model.data.type.StringPair
import javasabr.mqtt.model.reason.code.PublishReleaseReasonCode
import javasabr.mqtt.network.packet.in.PublishReleaseInPacket
import javasabr.mqtt.network.packet.out.PublishRelease311OutPacket
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class PublishRelease311OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {
    given:
        def packet = new PublishRelease311OutPacket(packetId)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt311Connection, it)
        }
        def reader = new PublishReleaseInPacket(0b0110_0000 as byte)
        def result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCode() == PublishReleaseReasonCode.SUCCESS
        reader.packetId() == packetId
        reader.userProperties() == Array.empty(StringPair)
        reader.reason() == ""
  }
}
