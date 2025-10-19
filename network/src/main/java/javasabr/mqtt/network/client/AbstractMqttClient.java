package javasabr.mqtt.network.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import javasabr.mqtt.base.utils.DebugUtils;
import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode;
import javasabr.mqtt.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.MqttSession;
import javasabr.mqtt.network.handler.MqttClientReleaseHandler;
import javasabr.mqtt.network.handler.PacketInHandler;
import javasabr.mqtt.network.out.MqttPacketOutFactories;
import javasabr.mqtt.network.out.MqttPacketOutFactory;
import javasabr.mqtt.network.packet.in.MqttReadablePacket;
import javasabr.mqtt.network.packet.out.MqttWritablePacket;
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
  final MqttClientReleaseHandler releaseHandler;
  final AtomicBoolean released;

  @Setter
  volatile String clientId;

  @Setter
  @Getter
  @Nullable
  volatile MqttSession session;

  public AbstractMqttClient(MqttConnection connection, MqttClientReleaseHandler releaseHandler) {
    this.connection = connection;
    this.releaseHandler = releaseHandler;
    this.released = new AtomicBoolean(false);
    this.clientId = connection.remoteAddress();
  }

  @Override
  public void handle(MqttReadablePacket packet) {
    log.debug(clientId, packet.name(), packet, "[%s] Received packet:[%s] %s"::formatted);
    PacketInHandler packetHandler = connection.packetHandlers()[packet.packetType()];
    if (packetHandler != null) {
      packetHandler.handle(this, packet);
    } else {
      log.warning(clientId, packet.name(), packet, "[%s] No packet handler for packet:[%s] %s"::formatted);
    }
  }

  @Override
  public void send(MqttWritablePacket packet) {
    log.debug(clientId, packet.name(), packet, "[%s] Send to client packet:[%s] %s"::formatted);
    connection.send(packet);
  }

  @Override
  public CompletableFuture<Boolean> sendWithFeedback(MqttWritablePacket packet) {
    log.debug(clientId, packet.name(), packet, "[%s] Send to client packet:[%s] %s"::formatted);
    return connection.sendWithFeedback(packet);
  }

  public void reject(ConnectAckReasonCode reasonCode) {
    connection
        .sendWithFeedback(packetOutFactory().newConnectAck(this, reasonCode))
        .thenAccept(_ -> connection.close());
  }

  @Override
  public MqttPacketOutFactory packetOutFactory() {
    return MqttPacketOutFactories.of(connection
        .clientConnectionConfig()
        .mqttVersion());
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
  public MqttClientConnectionConfig connectionConfig() {
    return connection.clientConnectionConfig();
  }

  @Override
  public String toString() {
    return DebugUtils.toJsonString(this);
  }
}
