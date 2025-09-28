package javasabr.mqtt.application.network.in

import javasabr.mqtt.network.packet.in.UnsubscribeAckInPacket
import javasabr.mqtt.model.PacketProperty
import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class UnsubscribeAckInPacketTest extends BaseInPacketTest {

  def "should read packet correctly as mqtt 3.1.1"() {

    given:

        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(packetId)
        }

    when:
        def packet = new UnsubscribeAckInPacket(0b1011_0000 as byte)
        def result = packet.read(mqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reason == ""
        packet.packetId == packetId
        packet.reasonCodes == Array.empty()
  }

  def "should read packet correctly as mqtt 5.0"() {

    given:

        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(PacketProperty.REASON_STRING, reasonString)
          it.putProperty(PacketProperty.USER_PROPERTY, userProperties)
        }

        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(packetId)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
          it.put(UnsubscribeAckReasonCode.SUCCESS.value)
          it.put(UnsubscribeAckReasonCode.SUCCESS.value)
          it.put(UnsubscribeAckReasonCode.NOT_AUTHORIZED.value)
          it.put(UnsubscribeAckReasonCode.UNSPECIFIED_ERROR.value)
        }

    when:
        def packet = new UnsubscribeAckInPacket(0b1011_0000 as byte)
        def result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reason == reasonString
        packet.packetId == packetId
        packet.reasonCodes.size() == 4
        packet.reasonCodes.get(0) == UnsubscribeAckReasonCode.SUCCESS
        packet.reasonCodes.get(1) == UnsubscribeAckReasonCode.SUCCESS
        packet.reasonCodes.get(2) == UnsubscribeAckReasonCode.NOT_AUTHORIZED
        packet.reasonCodes.get(3) == UnsubscribeAckReasonCode.UNSPECIFIED_ERROR
        packet.userProperties == userProperties
    when:

        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(packetId)
          it.putMbi(0)
          it.put(UnsubscribeAckReasonCode.UNSPECIFIED_ERROR.value)
          it.put(UnsubscribeAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR.value)
        }

        packet = new UnsubscribeAckInPacket(0b1011_0000 as byte)
        result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reason == ""
        packet.packetId == packetId
        packet.reasonCodes.size() == 2
        packet.reasonCodes.get(0) == UnsubscribeAckReasonCode.UNSPECIFIED_ERROR
        packet.reasonCodes.get(1) == UnsubscribeAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR
        packet.userProperties == Array.empty()
  }
}
