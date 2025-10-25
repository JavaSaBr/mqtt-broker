package javasabr.mqtt.application.network.out

import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode
import javasabr.mqtt.network.message.in.PublishCompleteMqttInMessage
import javasabr.mqtt.network.message.out.PublishCompleteMqtt311OutMessage
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class PublishCompleteMqtt311OutMessageTest extends BaseOutPacketTest {

  def "should write packet correctly"() {
    given:
        def packet = new PublishCompleteMqtt311OutMessage(packetId)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt311Connection, it)
        }
        def reader = new PublishCompleteMqttInMessage(0b0111_0000 as byte)
        def result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCode() == PublishCompletedReasonCode.SUCCESS
        reader.messageId() == packetId
        reader.userProperties() == Array.empty()
        reader.reason() == ""
  }
}
