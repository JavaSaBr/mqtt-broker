package javasabr.mqtt.network.ssl;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.util.MqttDataUtils;
import javasabr.rlib.functions.ObjBoolConsumer;
import javasabr.rlib.network.packet.WritableNetworkPacket;
import javasabr.rlib.network.packet.impl.AbstractSslNetworkPacketWriter;
import javax.net.ssl.SSLEngine;

public class SslMqttMessageWriter extends AbstractSslNetworkPacketWriter<MqttOutMessage, MqttConnection> {

  private static final int MAX_MBI_SIZE = 4;
  private static final int HEADER_TYPE_SIZE = 1;
  private static final int PAYLOAD_OFFSET = MAX_MBI_SIZE + HEADER_TYPE_SIZE;

  public SslMqttMessageWriter(
      MqttConnection connection,
      Runnable updateActivityFunction,
      Supplier<WritableNetworkPacket<MqttConnection>> packetProvider,
      Consumer<WritableNetworkPacket<MqttConnection>> serializedToChannelPacketHandler,
      ObjBoolConsumer<WritableNetworkPacket<MqttConnection>> sentPacketHandler,
      SSLEngine sslEngine,
      Consumer<WritableNetworkPacket<MqttConnection>> queueAtFirst) {
    super(
        connection,
        updateActivityFunction,
        packetProvider,
        serializedToChannelPacketHandler,
        sentPacketHandler,
        sslEngine,
        queueAtFirst);
  }

  @Override
  protected int totalSize(WritableNetworkPacket<MqttConnection> packet, int expectedLength) {
    return PAYLOAD_OFFSET + expectedLength;
  }

  @Override
  protected boolean onBeforeSerialize(
      MqttOutMessage packet,
      int expectedLength,
      int totalSize,
      ByteBuffer writeBuffer) {
    writeBuffer.clear().position(PAYLOAD_OFFSET);
    return true;
  }

  @Override
  protected boolean onAfterSerialize(
      MqttOutMessage packet,
      int expectedLength,
      int totalSize,
      ByteBuffer writeBuffer) {
    int maxBufferPosition = writeBuffer.position();
    int payloadSize = maxBufferPosition - PAYLOAD_OFFSET;
    int messageTypeAndFlagsOffset = MAX_MBI_SIZE - MqttDataUtils.sizeOfMbi(payloadSize);
    writeBuffer
        .position(messageTypeAndFlagsOffset)
        .put((byte) packet.messageTypeAndFlags());
    MqttDataUtils
        .writeMbi(payloadSize, writeBuffer)
        .position(messageTypeAndFlagsOffset)
        .limit(maxBufferPosition);
    return true;
  }
}
