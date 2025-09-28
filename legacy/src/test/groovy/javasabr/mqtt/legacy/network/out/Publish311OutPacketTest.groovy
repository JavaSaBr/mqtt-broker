package javasabr.mqtt.legacy.network.out

import javasabr.mqtt.legacy.network.packet.in.PublishInPacket
import javasabr.mqtt.legacy.network.packet.out.Publish311OutPacket
import javasabr.mqtt.model.QoS
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class Publish311OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {

    given:
        def packet = new Publish311OutPacket(
            packetId,
            QoS.EXACTLY_ONCE,
            true,
            true,
            publishTopic.toString(),
            publishPayload
        )
    when:

        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(it)
        }

        def reader = new PublishInPacket(0b0011_1101 as byte)
        def result = reader.read(mqtt311Connection, dataBuffer, dataBuffer.limit())

    then:
        result
        reader.packetId == packetId
        reader.qos == QoS.EXACTLY_ONCE
        reader.retained
        reader.duplicate
        reader.payload == publishPayload
        reader.topicName == publishTopic
        reader.userProperties == Array.empty()
    when:

        packet = new Publish311OutPacket(
            packetId,
            QoS.AT_MOST_ONCE,
            false,
            false,
            publishTopic.toString(),
            publishPayload
        )

        dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(it)
        }

        reader = new PublishInPacket(0b0011_0000 as byte)
        result = reader.read(mqtt311Connection, dataBuffer, dataBuffer.limit())

    then:
        result
        reader.packetId == 0
        reader.qos == QoS.AT_MOST_ONCE
        !reader.retained
        !reader.duplicate
        reader.payload == publishPayload
        reader.topicName == publishTopic
        reader.userProperties == Array.empty()
  }
}
