package javasabr.mqtt.network.packet;

import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.packet.out.MqttWritablePacket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javasabr.mqtt.network.utils.MqttDataUtils;
import javasabr.rlib.functions.ObjBoolConsumer;
import javasabr.rlib.network.BufferAllocator;
import javasabr.rlib.network.packet.WritableNetworkPacket;
import javasabr.rlib.network.packet.impl.AbstractNetworkPacketWriter;
import org.jspecify.annotations.Nullable;

public class MqttPacketWriter extends AbstractNetworkPacketWriter<MqttWritablePacket, MqttConnection> {

  public MqttPacketWriter(
      MqttConnection connection,
      AsynchronousSocketChannel channel,
      BufferAllocator bufferAllocator,
      Runnable updateActivityFunction,
      Supplier<@Nullable WritableNetworkPacket<MqttConnection>> nextWritePacketSupplier,
      Consumer<WritableNetworkPacket<MqttConnection>> writtenPacketHandler,
      ObjBoolConsumer<WritableNetworkPacket<MqttConnection>> sentPacketHandler) {
    super(
        connection,
        channel,
        bufferAllocator,
        updateActivityFunction,
        nextWritePacketSupplier,
        writtenPacketHandler,
        sentPacketHandler);
  }

  @Override
  protected int totalSize(WritableNetworkPacket<MqttConnection> packet, int expectedLength) {
    return 1 + MqttDataUtils.sizeOfMbi(expectedLength) + expectedLength;
  }

  @Override
  protected boolean onBeforeSerialize(
      MqttWritablePacket packet,
      int expectedLength,
      int totalSize,
      ByteBuffer firstBuffer,
      ByteBuffer secondBuffer) {
    firstBuffer.clear();
    secondBuffer.clear();
    return true;
  }

  @Override
  protected boolean doSerialize(
      MqttWritablePacket packet,
      int expectedLength,
      int totalSize,
      ByteBuffer firstBuffer,
      ByteBuffer secondBuffer) {
    if (!packet.write(connection, secondBuffer)) {
      return false;
    }
    secondBuffer.flip();
    return true;
  }

  @Override
  protected boolean onAfterSerialize(
      MqttWritablePacket packet,
      int expectedLength,
      int totalSize,
      ByteBuffer firstBuffer,
      ByteBuffer secondBuffer) {
    firstBuffer.put((byte) packet.getPacketTypeAndFlags());
    MqttDataUtils.writeMbi(secondBuffer.remaining(), firstBuffer);
    firstBuffer
        .put(secondBuffer)
        .flip();
    return true;
  }
}
