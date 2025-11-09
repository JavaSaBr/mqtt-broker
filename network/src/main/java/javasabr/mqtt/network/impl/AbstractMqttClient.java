package javasabr.mqtt.network.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.handler.MqttClientReleaseHandler;
import javasabr.mqtt.network.message.out.ConnectAckMqtt311OutMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.MqttSession;
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
  public void send(MqttOutMessage message) {
    log.debug(clientId, message.name(), message, "[%s] Send to client packet:[%s] %s"::formatted);
    connection.send(message);
  }

  @Override
  public CompletableFuture<Boolean> sendWithFeedback(MqttOutMessage message) {
    log.debug(clientId, message.name(), message, "[%s] Send to client packet:[%s] %s"::formatted);
    return connection.sendWithFeedback(message);
  }

  @Override
  public CompletableFuture<Boolean> closeWithReason(MqttOutMessage message) {
    return sendWithFeedback(message)
        .thenApply(sent -> {
          connection.close();
          return sent;
        });
  }

  @Override
  public void reject(ConnectAckMqtt311OutMessage connectAsk) {
    connection
        .sendWithFeedback(connectAsk)
        .thenAccept(_ -> connection.close());
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
