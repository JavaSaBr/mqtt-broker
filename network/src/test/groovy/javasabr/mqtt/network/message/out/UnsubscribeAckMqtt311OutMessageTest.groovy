package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.data.type.StringPair
import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode
import javasabr.mqtt.network.message.in.UnsubscribeAckMqttInMessage
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class UnsubscribeAckMqtt311OutMessageTest extends BaseMqttOutMessageTest {

  def "should write packet correctly"() {
    given:
        def packet = new UnsubscribeAckMqtt311OutMessage(packetId)
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
