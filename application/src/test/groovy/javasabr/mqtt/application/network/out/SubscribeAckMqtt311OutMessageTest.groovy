package javasabr.mqtt.application.network.out

import javasabr.mqtt.model.data.type.StringPair
import javasabr.mqtt.network.message.in.SubscribeAckMqttInMessage
import javasabr.mqtt.network.message.out.SubscribeAckMqtt311OutMessage
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class SubscribeAckMqtt311OutMessageTest extends BaseOutPacketTest {

  def "should write packet correctly"() {
    given:
        def packet = new SubscribeAckMqtt311OutMessage(subscribeAckReasonCodes, packetId)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt311Connection, it)
        }
        def reader = new SubscribeAckMqttInMessage(0b1001_0000 as byte)
        def result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCodes == subscribeAckReasonCodes
        reader.messageId == packetId
        reader.userProperties() == Array.empty(StringPair)
        reader.reason == ""
  }
}
