package javasabr.mqtt.application.network.out

import javasabr.mqtt.network.packet.in.PublishReceivedInPacket
import javasabr.mqtt.network.packet.out.PublishReceived311OutPacket
import javasabr.mqtt.model.data.type.StringPair
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class PublishReceived311OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {
    given:
        def packet = new PublishReceived311OutPacket(packetId)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt311Connection, it)
        }
        def reader = new PublishReceivedInPacket(0b0101_0000 as byte)
        def result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCode() == PublishReceivedReasonCode.SUCCESS
        reader.packetId() == packetId
        reader.userProperties() == Array.empty(StringPair)
        reader.reason() == ""
  }
}
