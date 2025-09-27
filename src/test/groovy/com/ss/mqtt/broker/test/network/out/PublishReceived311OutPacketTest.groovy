package com.ss.mqtt.broker.test.network.out

import javasabr.mqtt.legacy.model.data.type.StringPair
import javasabr.mqtt.legacy.model.reason.code.PublishReceivedReasonCode
import javasabr.mqtt.legacy.network.packet.in.PublishReceivedInPacket
import javasabr.mqtt.legacy.network.packet.out.PublishReceived311OutPacket
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class PublishReceived311OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {

    given:
        def packet = new PublishReceived311OutPacket(packetId)
    when:

        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(it)
        }

        def reader = new PublishReceivedInPacket(0b0101_0000 as byte)
        def result = reader.read(mqtt311Connection, dataBuffer, dataBuffer.limit())

    then:
        result
        reader.reasonCode == PublishReceivedReasonCode.SUCCESS
        reader.packetId == packetId
        reader.userProperties == Array.empty(StringPair)
        reader.reason == ""
  }
}
