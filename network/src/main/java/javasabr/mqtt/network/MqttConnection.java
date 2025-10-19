package javasabr.mqtt.network;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.function.Function;
import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttServerConnectionConfig;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.network.handler.PacketInHandler;
import javasabr.mqtt.network.packet.MqttPacketReader;
import javasabr.mqtt.network.packet.MqttPacketWriter;
import javasabr.rlib.network.BufferAllocator;
import javasabr.rlib.network.Network;
import javasabr.rlib.network.impl.AbstractConnection;
import javasabr.rlib.network.packet.NetworkPacketReader;
import javasabr.rlib.network.packet.NetworkPacketWriter;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@CustomLog
@Accessors(fluent = true, chain = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MqttConnection extends AbstractConnection<MqttConnection> {

  @Getter(AccessLevel.PROTECTED)
  final NetworkPacketReader packetReader;
  @Getter(AccessLevel.PROTECTED)
  final NetworkPacketWriter packetWriter;

  @Getter
  final PacketInHandler[] packetHandlers;

  @Getter
  final UnsafeMqttClient client;
  @Getter
  final MqttServerConnectionConfig serverConnectionConfig;

  @Nullable
  MqttClientConnectionConfig clientConnectionConfig;

  public MqttConnection(
      Network<MqttConnection> network,
      AsynchronousSocketChannel channel,
      BufferAllocator bufferAllocator,
      int maxPacketsByRead,
      PacketInHandler[] packetHandlers,
      MqttServerConnectionConfig config,
      Function<MqttConnection, UnsafeMqttClient> clientFactory) {
    super(network, channel, bufferAllocator, maxPacketsByRead);
    this.packetHandlers = packetHandlers;
    this.serverConnectionConfig = config;
    this.packetReader = createPacketReader();
    this.packetWriter = createPacketWriter();
    this.client = clientFactory.apply(this);
  }

  public boolean isSupported(MqttVersion mqttVersion) {
    return clientConnectionConfig()
        .mqttVersion()
        .include(mqttVersion);
  }

  public void configure(MqttClientConnectionConfig clientConnectionConfig) {
    synchronized (this) {
      this.clientConnectionConfig = clientConnectionConfig;
    }
  }

  public MqttClientConnectionConfig clientConnectionConfig() {
    var config = this.clientConnectionConfig;
    if (config == null) {
      synchronized (this) {
        config = this.clientConnectionConfig;
        if (config == null) {
          throw new IllegalStateException("The connection is not fully configured.");
        }
      }
    }
    return config;
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
