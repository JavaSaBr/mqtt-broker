package javasabr.mqtt.network;

import java.nio.channels.AsynchronousSocketChannel;
import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttServerConnectionConfig;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.network.message.MqttMessageReader;
import javasabr.mqtt.network.message.MqttMessageWriter;
import javasabr.mqtt.network.user.ConfigurableNetworkMqttUser;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.mqtt.network.user.NetworkMqttUserFactory;
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

  final ConfigurableNetworkMqttUser user;
  @Getter
  final MqttServerConnectionConfig serverConnectionConfig;

  @Nullable
  MqttClientConnectionConfig clientConnectionConfig;

  public MqttConnection(
      Network<MqttConnection> network,
      AsynchronousSocketChannel channel,
      BufferAllocator bufferAllocator,
      int maxPacketsByRead,
      MqttServerConnectionConfig serverConnectionConfig,
      NetworkMqttUserFactory mqttUserFactory) {
    super(network, channel, bufferAllocator, maxPacketsByRead);
    this.serverConnectionConfig = serverConnectionConfig;
    this.packetReader = createPacketReader();
    this.packetWriter = createPacketWriter();
    this.user = mqttUserFactory.createNetworkUser(this);
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

  public NetworkMqttUser user() {
    return user;
  }

  private NetworkPacketReader createPacketReader() {
    return new MqttMessageReader(
        this,
        this::updateLastActivity,
        this::handleReceivedValidPacket,
        this::handleReceivedInvalidPacket,
        maxPacketsByRead);
  }

  private NetworkPacketWriter createPacketWriter() {
    return new MqttMessageWriter(
        this,
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
    user.release().subscribe();
    super.doClose();
  }
}
