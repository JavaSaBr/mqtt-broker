package javasabr.mqtt.application.network.out

import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode
import javasabr.mqtt.network.packet.in.PublishReceivedInPacket
import javasabr.mqtt.network.packet.out.PublishReceived5OutPacket
import javasabr.rlib.common.util.BufferUtils

class PublishReceived5OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {
    given:
        def packet = new PublishReceived5OutPacket(
            packetId,
            PublishReceivedReasonCode.UNSPECIFIED_ERROR,
            userProperties,
            reasonString)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt5Connection, it)
        }
        def reader = new PublishReceivedInPacket(0b0101_0000 as byte)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCode() == PublishReceivedReasonCode.UNSPECIFIED_ERROR
        reader.messageId() == packetId
        reader.userProperties() == userProperties
        reader.reason() == reasonString
  }
}
