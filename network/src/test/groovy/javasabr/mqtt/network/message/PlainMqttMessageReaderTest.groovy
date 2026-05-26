package javasabr.mqtt.network.message


import javasabr.mqtt.network.MqttConnection
import javasabr.mqtt.network.NetworkUnitSpecification
import javasabr.mqtt.network.message.plain.PlainMqttMessageReader
import javasabr.rlib.network.Network
import javasabr.rlib.network.ServerNetworkConfig
import javasabr.rlib.network.impl.DefaultBufferAllocator

import java.nio.ByteBuffer
import java.util.function.Consumer

class PlainMqttMessageReaderTest extends NetworkUnitSpecification {

  def "readFullPacketLength should restore buffer position if MBI is incomplete"() {
    given:
        def config = ServerNetworkConfig.SimpleServerNetworkConfig.builder()
            .readBufferSize(1024)
            .pendingBufferSize(1024)
            .build()
        def network = Stub(Network) {
          config() >> config
        }
        def connection = Stub(MqttConnection) {
          network() >> network
          bufferAllocator() >> new DefaultBufferAllocator(config)
        }

        def reader = new PlainMqttMessageReader(
            connection,
            { -> },
            { p -> } as Consumer,
            { p -> } as Consumer,
            10,
            new MqttPacketCodec())

        def buffer = ByteBuffer.allocate(10)
        buffer.put((byte) 0x10)
        buffer.put((byte) 0x80)
        buffer.flip()

        def originalPosition = buffer.position()

    when:
        def length = reader.readFullPacketLength(buffer)

    then:
        length == -1
        buffer.position() == originalPosition
  }
}
