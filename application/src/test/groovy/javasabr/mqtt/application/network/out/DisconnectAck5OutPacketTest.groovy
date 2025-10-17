package javasabr.mqtt.application.network.out

import javasabr.mqtt.network.packet.in.DisconnectInPacket
import javasabr.mqtt.network.packet.out.Disconnect5OutPacket
import javasabr.mqtt.model.reason.code.DisconnectReasonCode
import javasabr.rlib.common.util.BufferUtils

class DisconnectAck5OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {

    given:

        def packet = new Disconnect5OutPacket(
            DisconnectReasonCode.PACKET_TOO_LARGE,
            userProperties,
            reasonString,
            serverReference,
            sessionExpiryInterval
        )

    when:

        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(mqtt5Connection, it)
        }

        def reader = new DisconnectInPacket(0b1110_0000 as byte)
        def result = reader.read(mqtt5Connection, dataBuffer, dataBuffer.limit())

    then:
        result
        reader.reasonCode == DisconnectReasonCode.PACKET_TOO_LARGE
        reader.userProperties == userProperties
        reader.reason == reasonString
        reader.serverReference == serverReference
        reader.sessionExpiryInterval == sessionExpiryInterval
  }
}
