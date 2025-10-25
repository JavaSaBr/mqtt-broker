package javasabr.mqtt.application.network.in

import javasabr.mqtt.model.PacketProperty
import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode
import javasabr.mqtt.network.packet.in.PublishCompleteInPacket
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class PublishCompleteInPacketTest extends BaseInPacketTest {

  def "should read packet correctly as mqtt 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(packetId)
        }
    when:
        def packet = new PublishCompleteInPacket(0b0111_0000 as byte)
        def result = packet.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reason() == ""
        packet.messageId() == packetId
        packet.reasonCode() == PublishCompletedReasonCode.SUCCESS
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
          it.put(PublishCompletedReasonCode.PACKET_IDENTIFIER_NOT_FOUND.value)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
    when:
        def packet = new PublishCompleteInPacket(0b0111_0000 as byte)
        def result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reason() == reasonString
        packet.messageId() == packetId
        packet.reasonCode() == PublishCompletedReasonCode.PACKET_IDENTIFIER_NOT_FOUND
        packet.userProperties() == userProperties
    when:
        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(packetId)
          it.put(PublishCompletedReasonCode.PACKET_IDENTIFIER_NOT_FOUND.value)
          it.putMbi(0)
        }
        packet = new PublishCompleteInPacket(0b0111_0000 as byte)
        result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reason() == ""
        packet.messageId() == packetId
        packet.reasonCode() == PublishCompletedReasonCode.PACKET_IDENTIFIER_NOT_FOUND
        packet.userProperties() == Array.empty()
  }
}
