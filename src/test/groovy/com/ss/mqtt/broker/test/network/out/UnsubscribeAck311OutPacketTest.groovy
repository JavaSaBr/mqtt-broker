package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.model.data.type.StringPair
import com.ss.mqtt.broker.model.reason.code.UnsubscribeAckReasonCode
import com.ss.mqtt.broker.network.packet.in.UnsubscribeAckInPacket
import com.ss.mqtt.broker.network.packet.out.UnsubscribeAck311OutPacket
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class UnsubscribeAck311OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {

    given:
        def packet = new UnsubscribeAck311OutPacket(packetId)
    when:

        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(it)
        }

        def reader = new UnsubscribeAckInPacket(0b1011_0000 as byte)
        def result = reader.read(mqtt311Connection, dataBuffer, dataBuffer.limit())

    then:
        result
        reader.reasonCodes == Array.empty(UnsubscribeAckReasonCode)
        reader.packetId == packetId
        reader.userProperties == Array.empty(StringPair)
        reader.reason == ""
  }
}
