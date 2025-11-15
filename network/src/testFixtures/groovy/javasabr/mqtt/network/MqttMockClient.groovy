package javasabr.mqtt.network

import javasabr.mqtt.network.message.MqttMessageType
import javasabr.mqtt.network.message.in.ConnectAckMqttInMessage
import javasabr.mqtt.network.message.in.MqttInMessage
import javasabr.mqtt.network.message.in.PublishMqttInMessage
import javasabr.mqtt.network.message.in.PublishReleaseMqttInMessage
import javasabr.mqtt.network.message.in.SubscribeAckMqttInMessage
import javasabr.mqtt.network.message.out.DisconnectMqtt311OutMessage
import javasabr.mqtt.network.message.out.MqttOutMessage
import javasabr.mqtt.network.util.MqttDataUtils
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

  void send(MqttOutMessage packet) {

    def dataBuffer = ByteBuffer.allocate(1024)
    packet.write(connection, dataBuffer)
    dataBuffer.flip()

    def finalBuffer = ByteBuffer.allocate(1024)
    finalBuffer.put((byte) packet.messageTypeAndFlags())

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

    def messageType = MqttMessageType.fromByte(type)

    MqttInMessage inMessage = switch (messageType) {
      case MqttMessageType.CONNECT_ACK -> new ConnectAckMqttInMessage(info)
      case MqttMessageType.SUBSCRIBE_ACK -> new SubscribeAckMqttInMessage(info)
      case MqttMessageType.PUBLISH -> new PublishMqttInMessage(info)
      case MqttMessageType.PUBLISH_RELEASED -> new PublishReleaseMqttInMessage(info)
      default -> {
        throw new IllegalStateException("Unknown packet of type:$messageType")
      }
    }

    inMessage.read(connection, received, dataSize)

    if (received.hasRemaining()) {
      received.compact()
    } else {
      received.clear()
    }

    return inMessage
  }

  def disconnect() {
    send(new DisconnectMqtt311OutMessage())
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
