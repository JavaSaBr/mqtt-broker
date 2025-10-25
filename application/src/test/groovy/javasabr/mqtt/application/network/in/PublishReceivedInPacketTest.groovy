package javasabr.mqtt.application.network.in

import javasabr.mqtt.model.PacketProperty
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode
import javasabr.mqtt.network.packet.in.PublishReceivedInPacket
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class PublishReceivedInPacketTest extends BaseInPacketTest {

  def "should read packet correctly as mqtt 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(packetId)
        }
    when:
        def packet = new PublishReceivedInPacket(0b0101_0000 as byte)
        def result = packet.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reason() == ""
        packet.messageId() == packetId
        packet.reasonCode() == PublishReceivedReasonCode.SUCCESS
        packet.userProperties() == Array.empty()
  }

  def "should read packet correctly as mqtt 5.0"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(PacketProperty.REASON_STRING, reasonString)
          it.putProperty(PacketProperty.USER_PROPERTY, userProperties)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(packetId)
          it.put(PublishReceivedReasonCode.QUOTA_EXCEEDED.value)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
    when:
        def packet = new PublishReceivedInPacket(0b0101_0000 as byte)
        def result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reason() == reasonString
        packet.messageId() == packetId
        packet.reasonCode() == PublishReceivedReasonCode.QUOTA_EXCEEDED
        packet.userProperties() == userProperties
    when:
        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(packetId)
          it.put(PublishReceivedReasonCode.IMPLEMENTATION_SPECIFIC_ERROR.value)
          it.putMbi(0)
        }
        packet = new PublishReceivedInPacket(0b0101_0000 as byte)
        result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reason() == ""
        packet.messageId() == packetId
        packet.reasonCode() == PublishReceivedReasonCode.IMPLEMENTATION_SPECIFIC_ERROR
        packet.userProperties() == Array.empty()
  }
}
