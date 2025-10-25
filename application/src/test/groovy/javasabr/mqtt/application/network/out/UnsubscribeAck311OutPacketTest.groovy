package javasabr.mqtt.application.network.out

import javasabr.mqtt.model.data.type.StringPair
import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode
import javasabr.mqtt.network.message.in.UnsubscribeAckMqttInMessage
import javasabr.mqtt.network.packet.out.UnsubscribeAck311OutPacket
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class UnsubscribeAck311OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {
    given:
        def packet = new UnsubscribeAck311OutPacket(packetId)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt311Connection, it)
        }
        def reader = new UnsubscribeAckMqttInMessage(0b1011_0000 as byte)
        def result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCodes() == Array.empty(UnsubscribeAckReasonCode)
        reader.messageId() == packetId
        reader.userProperties() == Array.empty(StringPair)
        reader.reason() == ""
  }
}
