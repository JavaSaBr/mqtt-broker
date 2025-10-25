package javasabr.mqtt.application.network.out

import javasabr.mqtt.network.message.in.UnsubscribeAckMqttInMessage
import javasabr.mqtt.network.packet.out.UnsubscribeAck5OutPacket
import javasabr.rlib.common.util.BufferUtils

class UnsubscribeAck5OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {
    given:
        def packet = new UnsubscribeAck5OutPacket(
            packetId,
            unsubscribeAckReasonCodes,
            userProperties,
            reasonString)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt5Connection, it)
        }
        def reader = new UnsubscribeAckMqttInMessage(0b1011_0000 as byte)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCodes() == unsubscribeAckReasonCodes
        reader.messageId() == packetId
        reader.userProperties() == userProperties
        reader.reason() == reasonString
  }
}
