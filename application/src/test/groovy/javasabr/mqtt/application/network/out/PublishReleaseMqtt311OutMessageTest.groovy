package javasabr.mqtt.application.network.out

import javasabr.mqtt.model.data.type.StringPair
import javasabr.mqtt.model.reason.code.PublishReleaseReasonCode
import javasabr.mqtt.network.message.in.PublishReleaseMqttInMessage
import javasabr.mqtt.network.message.out.PublishReleaseMqtt311OutMessage
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class PublishReleaseMqtt311OutMessageTest extends BaseOutPacketTest {

  def "should write packet correctly"() {
    given:
        def packet = new PublishReleaseMqtt311OutMessage(packetId)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt311Connection, it)
        }
        def reader = new PublishReleaseMqttInMessage(0b0110_0000 as byte)
        def result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCode() == PublishReleaseReasonCode.SUCCESS
        reader.messageId() == packetId
        reader.userProperties() == Array.empty(StringPair)
        reader.reason() == ""
  }
}
