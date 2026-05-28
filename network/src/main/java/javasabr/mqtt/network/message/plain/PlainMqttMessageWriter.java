package javasabr.mqtt.network.message.plain;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.MqttPacketCodec;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.rlib.functions.ObjBoolConsumer;
import javasabr.rlib.network.packet.WritableNetworkPacket;
import javasabr.rlib.network.packet.impl.AbstractNetworkPacketWriter;
import org.jspecify.annotations.Nullable;

public class PlainMqttMessageWriter extends AbstractNetworkPacketWriter<MqttOutMessage, MqttConnection> {

  private final MqttPacketCodec mqttPacketCodec;

  public PlainMqttMessageWriter(
      MqttConnection connection,
      Runnable updateActivityFunction,
      Supplier<@Nullable WritableNetworkPacket<MqttConnection>> packetProvider,
      Consumer<WritableNetworkPacket<MqttConnection>> serializedToChannelPacketHandler,
      ObjBoolConsumer<WritableNetworkPacket<MqttConnection>> sentPacketHandler,
      MqttPacketCodec mqttPacketCodec) {
    super(
        connection,
        updateActivityFunction,
        packetProvider,
        serializedToChannelPacketHandler,
        sentPacketHandler);
    this.mqttPacketCodec = mqttPacketCodec;
  }

  @Override
  protected int totalSize(WritableNetworkPacket<MqttConnection> packet, int expectedLength) {
    return mqttPacketCodec.calculateEncodedPacketSize(expectedLength);
  }

  @Override
  protected boolean onBeforeSerialize(
      MqttOutMessage packet,
      int expectedLength,
      int totalSize,
      ByteBuffer writeBuffer) {
    mqttPacketCodec.prepareEncodingBuffer(writeBuffer);
    return true;
  }

  @Override
  protected boolean onAfterSerialize(
      MqttOutMessage packet,
      int expectedLength,
      int totalSize,
      ByteBuffer writeBuffer) {
    mqttPacketCodec.encodeHeader(packet, writeBuffer);
    return true;
  }
}
