package com.ss.mqtt.broker.test.network.out

import javasabr.mqtt.legacy.model.reason.code.DisconnectReasonCode
import javasabr.mqtt.legacy.network.packet.in.DisconnectInPacket
import javasabr.mqtt.legacy.network.packet.out.Disconnect5OutPacket
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
          packet.write(it)
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
