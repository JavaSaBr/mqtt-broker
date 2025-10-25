package javasabr.mqtt.application.mock

import javasabr.mqtt.network.MqttConnection
import javasabr.mqtt.network.message.MqttMessageType
import javasabr.mqtt.network.message.in.ConnectAckMqttInMessage
import javasabr.mqtt.network.message.in.MqttInMessage
import javasabr.mqtt.network.message.in.PublishMqttInMessage
import javasabr.mqtt.network.message.in.PublishReleaseMqttInMessage
import javasabr.mqtt.network.message.in.SubscribeAckMqttInMessage
import javasabr.mqtt.network.packet.out.Disconnect311OutPacket
import javasabr.mqtt.network.packet.out.MqttWritablePacket
import javasabr.mqtt.network.utils.MqttDataUtils
import javasabr.rlib.common.util.NumberUtils

import java.nio.ByteBuffer

class MqttMockClient {

  private final ByteBuffer received = ByteBuffer.allocate(1024).clear()

  private final String brokerHost
  private final int brokerPort
  private final MqttConnection connection

  private Socket socket

  MqttMockClient(String brokerHost, int brokerPort, MqttConnection connection) {
    this.brokerHost = brokerHost
    this.brokerPort = brokerPort
    this.connection = connection
  }

  void connect() {
    if (socket != null) {
      return
    }
    socket = new Socket(brokerHost, brokerPort)
  }

  void send(MqttWritablePacket packet) {

    def dataBuffer = ByteBuffer.allocate(1024)
    packet.write(connection, dataBuffer)
    dataBuffer.flip()

    def finalBuffer = ByteBuffer.allocate(1024)
    finalBuffer.put((byte) packet.packetTypeAndFlags())

    MqttDataUtils.writeMbi(dataBuffer.remaining(), finalBuffer)

    finalBuffer.put(dataBuffer).flip()

    def out = socket.getOutputStream()
    out.write(finalBuffer.array(), 0, finalBuffer.remaining())

    Thread.sleep(50)
  }

  MqttInMessage readNext() {

    if (received.position() == 0) {
      def input = socket.getInputStream()
      def readBytes = input.read(received.array(), received.position(), received.capacity() - received.position())
      if (readBytes > 0) {
        received.position(received.position() + readBytes)
      }
    }

    received.flip()

    if (!received.hasRemaining()) {
      throw new IllegalStateException("No received bytes.")
    }

    def startByte = Byte.toUnsignedInt(received.get())
    def type = NumberUtils.getHighByteBits(startByte)
    def info = NumberUtils.getLowByteBits(startByte)
    def dataSize = MqttDataUtils.readMbi(received)

    MqttInMessage packet

    switch (MqttMessageType.fromByte(type)) {
      case MqttMessageType.CONNECT_ACK:
        packet = new ConnectAckMqttInMessage(info)
        break
      case MqttMessageType.SUBSCRIBE_ACK:
        packet = new SubscribeAckMqttInMessage(info)
        break
      case MqttMessageType.PUBLISH:
        packet = new PublishMqttInMessage(info)
        break
      case MqttMessageType.PUBLISH_RELEASED:
        packet = new PublishReleaseMqttInMessage(info)
        break
      default:
        throw new IllegalStateException("Unknown packet of type: $type")
    }

    packet.read(connection, received, dataSize)

    if (received.hasRemaining()) {
      received.compact()
    } else {
      received.clear()
    }

    return packet
  }

  def disconnect() {
    send(new Disconnect311OutPacket())
    close()
  }

  def close() {
    if (socket != null) {
      socket.close()
      socket = null
      received.clear()
      Thread.sleep(50)
    }
  }
}
