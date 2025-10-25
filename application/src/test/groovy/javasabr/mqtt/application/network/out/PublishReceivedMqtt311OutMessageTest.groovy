package javasabr.mqtt.application.network.out

import javasabr.mqtt.model.data.type.StringPair
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode
import javasabr.mqtt.network.message.in.PublishReceivedMqttInMessage
import javasabr.mqtt.network.message.out.PublishReceivedMqtt311OutMessage
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class PublishReceivedMqtt311OutMessageTest extends BaseOutPacketTest {

  def "should write packet correctly"() {
    given:
        def packet = new PublishReceivedMqtt311OutMessage(packetId)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt311Connection, it)
        }
        def reader = new PublishReceivedMqttInMessage(0b0101_0000 as byte)
        def result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCode() == PublishReceivedReasonCode.SUCCESS
        reader.messageId() == packetId
        reader.userProperties() == Array.empty(StringPair)
        reader.reason() == ""
  }
}
