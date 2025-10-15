package javasabr.mqtt.network;

import javasabr.mqtt.model.MqttConnectionConfig;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.network.handler.packet.in.PacketInHandler;
import javasabr.mqtt.network.packet.MqttPacketReader;
import javasabr.mqtt.network.packet.MqttPacketWriter;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.function.Function;
import javasabr.rlib.network.BufferAllocator;
import javasabr.rlib.network.Network;
import javasabr.rlib.network.impl.AbstractConnection;
import javasabr.rlib.network.packet.NetworkPacketReader;
import javasabr.rlib.network.packet.NetworkPacketWriter;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@CustomLog
@Accessors(fluent = true, chain = false)
public class MqttConnection extends AbstractConnection<MqttConnection> {

  @Getter(AccessLevel.PROTECTED)
  private final NetworkPacketReader packetReader;

  @Getter(AccessLevel.PROTECTED)
  private final NetworkPacketWriter packetWriter;

  @Getter
  private final PacketInHandler[] packetHandlers;

  @Getter
  private final UnsafeMqttClient client;
  @Getter
  private final MqttConnectionConfig config;

  @Getter
  @Setter
  private volatile MqttVersion mqttVersion;

  @Getter
  @Setter
  private volatile MqttSession session;

  public MqttConnection(
      Network<MqttConnection> network,
      AsynchronousSocketChannel channel,
      BufferAllocator bufferAllocator,
      int maxPacketsByRead,
      PacketInHandler[] packetHandlers,
      MqttConnectionConfig config,
      Function<MqttConnection, UnsafeMqttClient> clientFactory) {
    super(network, channel, bufferAllocator, maxPacketsByRead);
    this.packetHandlers = packetHandlers;
    this.config = config;
    this.mqttVersion = MqttVersion.MQTT_3_1_1;
    this.packetReader = createPacketReader();
    this.packetWriter = createPacketWriter();
    this.client = clientFactory.apply(this);
  }

  public boolean isSupported(MqttVersion mqttVersion) {
    return this.mqttVersion.ordinal() >= mqttVersion.ordinal();
  }

  private NetworkPacketReader createPacketReader() {
    return new MqttPacketReader(
        this,
        channel,
        bufferAllocator,
        this::updateLastActivity,
        this::handleReceivedPacket,
        maxPacketsByRead);
  }

  private NetworkPacketWriter createPacketWriter() {
    return new MqttPacketWriter(
        this,
        channel,
        bufferAllocator,
        this::updateLastActivity,
        this::nextPacketToWrite,
        this::serializedPacket,
        this::handleSentPacket);
  }

  @Override
  public String toString() {
    return remoteAddress;
  }

  @Override
  protected void doClose() {
    client.release().subscribe();
    super.doClose();
  }
}
