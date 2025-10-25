package javasabr.mqtt.network.message;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.packet.out.MqttWritablePacket;
import javasabr.mqtt.network.utils.MqttDataUtils;
import javasabr.rlib.functions.ObjBoolConsumer;
import javasabr.rlib.network.packet.WritableNetworkPacket;
import javasabr.rlib.network.packet.impl.AbstractNetworkPacketWriter;
import org.jspecify.annotations.Nullable;

public class MqttMessageWriter extends AbstractNetworkPacketWriter<MqttWritablePacket, MqttConnection> {

  public static final int DATA_OFFSET = 5;

  public MqttMessageWriter(
      MqttConnection connection,
      Runnable updateActivityFunction,
      Supplier<@Nullable WritableNetworkPacket<MqttConnection>> packetProvider,
      Consumer<WritableNetworkPacket<MqttConnection>> serializedToChannelPacketHandler,
      ObjBoolConsumer<WritableNetworkPacket<MqttConnection>> sentPacketHandler) {
    super(
        connection,
        updateActivityFunction,
        packetProvider,
        serializedToChannelPacketHandler,
        sentPacketHandler);
  }

  @Override
  protected int totalSize(WritableNetworkPacket<MqttConnection> packet, int expectedLength) {
    return DATA_OFFSET + expectedLength;
  }

  @Override
  protected boolean onBeforeSerialize(
      MqttWritablePacket packet,
      int expectedLength,
      int totalSize,
      ByteBuffer writeBuffer) {
    writeBuffer.clear().position(DATA_OFFSET);
    return true;
  }

  @Override
  protected boolean onAfterSerialize(
      MqttWritablePacket packet,
      int expectedLength,
      int totalSize,
      ByteBuffer writeBuffer) {

    int lastDataByteIndex = writeBuffer.position();
    int dataLength = lastDataByteIndex - DATA_OFFSET;
    int offset = 4 - MqttDataUtils.sizeOfMbi(dataLength);

    writeBuffer
        .position(offset)
        .put((byte) packet.packetTypeAndFlags());

    MqttDataUtils
        .writeMbi(dataLength, writeBuffer)
        .position(offset)
        .limit(lastDataByteIndex);

    return true;
  }
}
