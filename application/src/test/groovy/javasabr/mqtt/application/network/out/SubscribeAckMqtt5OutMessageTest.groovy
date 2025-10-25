package javasabr.mqtt.application.network.out

import javasabr.mqtt.network.message.in.SubscribeAckMqttInMessage
import javasabr.mqtt.network.message.out.SubscribeAckMqtt5OutMessage
import javasabr.rlib.common.util.BufferUtils

class SubscribeAckMqtt5OutMessageTest extends BaseOutPacketTest {

  def "should write packet correctly"() {
    given:
        def packet = new SubscribeAckMqtt5OutMessage(
            packetId,
            subscribeAckReasonCodes,
            userProperties,
            reasonString)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt5Connection, it)
        }
        def reader = new SubscribeAckMqttInMessage(0b1001_0000 as byte)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCodes == subscribeAckReasonCodes
        reader.messageId == packetId
        reader.userProperties() == userProperties
        reader.reason == reasonString
  }
}
