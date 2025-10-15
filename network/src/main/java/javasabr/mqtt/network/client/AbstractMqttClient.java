package javasabr.mqtt.network.client;

import javasabr.mqtt.model.MqttConnectionConfig;
import javasabr.mqtt.network.MqttSession;
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.network.handler.client.MqttClientReleaseHandler;
import javasabr.mqtt.network.handler.packet.in.PacketInHandler;
import javasabr.mqtt.network.out.MqttPacketOutFactories;
import javasabr.mqtt.network.out.MqttPacketOutFactory;
import javasabr.mqtt.network.packet.in.MqttReadablePacket;
import javasabr.mqtt.network.packet.out.MqttWritablePacket;
import javasabr.mqtt.base.utils.DebugUtils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import javasabr.rlib.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

@Getter
@CustomLog
@Accessors(fluent = true, chain = false)
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class AbstractMqttClient implements UnsafeMqttClient {

  static {
    DebugUtils.registerIncludedFields("clientId");
  }

  final MqttConnection connection;
  final MqttPacketOutFactory packetOutFactory;
  final MqttClientReleaseHandler releaseHandler;
  final AtomicBoolean released;

  @Setter
  volatile String clientId;

  @Setter
  @Getter
  @Nullable
  volatile MqttSession session;

  volatile long sessionExpiryInterval;
  volatile int receiveMax;
  volatile int maximumPacketSize;
  volatile int topicAliasMaximum;
  volatile int keepAlive;

  volatile boolean requestResponseInformation = false;
  volatile boolean requestProblemInformation = false;

  public AbstractMqttClient(MqttConnection connection, MqttClientReleaseHandler releaseHandler) {
    this.connection = connection;
    this.releaseHandler = releaseHandler;
    this.released = new AtomicBoolean(false);
    this.clientId = StringUtils.EMPTY;
    var config = connection.config();
    this.sessionExpiryInterval = config.getDefaultSessionExpiryInterval();
    this.receiveMax = config.getReceiveMaximum();
    this.maximumPacketSize = config.getMaximumPacketSize();
    this.topicAliasMaximum = config.getTopicAliasMaximum();
    this.keepAlive = config.getMinKeepAliveTime();
    this.packetOutFactory = MqttPacketOutFactories.of(connection.mqttVersion());
  }

  @Override
  public void handle(MqttReadablePacket packet) {
    log.debug(clientId, packet.name(), packet, "Client:[%s] received packet:[%s]:[%s]"::formatted);
    PacketInHandler packetHandler = connection.packetHandlers()[packet.getPacketType()];
    if (packetHandler != null) {
      packetHandler.handle(this, packet);
    } else {
      log.warning(this, packet, "No packet handler in client:[%s] for packet:[%s]"::formatted);
    }
  }

  @Override
  public void configure(
      long sessionExpiryInterval,
      int receiveMax,
      int maximumPacketSize,
      int topicAliasMaximum,
      int keepAlive,
      boolean requestResponseInformation,
      boolean requestProblemInformation) {
    this.sessionExpiryInterval = sessionExpiryInterval;
    this.receiveMax = receiveMax;
    this.maximumPacketSize = maximumPacketSize;
    this.topicAliasMaximum = topicAliasMaximum;
    this.keepAlive = keepAlive;
    this.requestProblemInformation = requestProblemInformation;
    this.requestResponseInformation = requestResponseInformation;
  }

  @Override
  public void send(MqttWritablePacket packet) {
    log.debug(clientId, packet.name(), packet, "Send to client:[%s] packet:[%s]:[%s]"::formatted);
    connection.send(packet);
  }

  @Override
  public CompletableFuture<Boolean> sendWithFeedback(MqttWritablePacket packet) {
    log.debug(clientId, packet.name(), packet, "Send to client:[%s] packet:[%s]:[%s]"::formatted);
    return connection.sendWithFeedback(packet);
  }

  public void reject(ConnectAckReasonCode reasonCode) {
    connection
        .sendWithFeedback(packetOutFactory().newConnectAck(this, reasonCode))
        .thenAccept(_ -> connection.close());
  }

  @Override
  public MqttPacketOutFactory packetOutFactory() {
    return packetOutFactory;
  }

  @Override
  public MqttConnectionConfig connectionConfig() {
    return connection.config();
  }

  @Override
  public Mono<?> release() {
    if (released.compareAndSet(false, true)) {
      return releaseHandler.release(this);
    } else {
      return Mono.empty();
    }
  }

  @Override
  public String toString() {
    return DebugUtils.toJsonString(this);
  }
}
