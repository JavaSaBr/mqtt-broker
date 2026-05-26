package javasabr.mqtt.network.message.ssl;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.MqttPacketCodec;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.rlib.functions.ObjBoolConsumer;
import javasabr.rlib.network.packet.WritableNetworkPacket;
import javasabr.rlib.network.packet.impl.AbstractSslNetworkPacketWriter;
import javax.net.ssl.SSLEngine;

public class TlsMqttMessageWriter extends AbstractSslNetworkPacketWriter<MqttOutMessage, MqttConnection> {

  private final MqttPacketCodec mqttPacketCodec;

  public TlsMqttMessageWriter(
      MqttConnection connection,
      Runnable updateActivityFunction,
      Supplier<WritableNetworkPacket<MqttConnection>> packetProvider,
      Consumer<WritableNetworkPacket<MqttConnection>> serializedToChannelPacketHandler,
      ObjBoolConsumer<WritableNetworkPacket<MqttConnection>> sentPacketHandler,
      SSLEngine sslEngine,
      Consumer<WritableNetworkPacket<MqttConnection>> queueAtFirst,
      MqttPacketCodec mqttPacketCodec) {
    super(
        connection,
        updateActivityFunction,
        packetProvider,
        serializedToChannelPacketHandler,
        sentPacketHandler,
        sslEngine,
        queueAtFirst);
    this.mqttPacketCodec = mqttPacketCodec;
  }

  @Override
  protected int totalSize(WritableNetworkPacket<MqttConnection> packet, int expectedLength) {
    return mqttPacketCodec.calculateTotalSize(expectedLength);
  }

  @Override
  protected boolean onBeforeSerialize(
      MqttOutMessage packet,
      int expectedLength,
      int totalSize,
      ByteBuffer writeBuffer) {
    mqttPacketCodec.prepareBuffer(writeBuffer);
    return true;
  }

  @Override
  protected boolean onAfterSerialize(
      MqttOutMessage packet,
      int expectedLength,
      int totalSize,
      ByteBuffer writeBuffer) {
    mqttPacketCodec.finalizeHeader(packet, writeBuffer);
    return true;
  }
}
