package javasabr.mqtt.network.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.message.SendableMqttMessage;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.handler.NetworkMqttUserReleaseHandler;
import javasabr.mqtt.network.message.out.ConnectAckMqtt311OutMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.network.user.ConfigurableNetworkMqttUser;
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
public abstract class AbstractNetworkMqttUser implements ConfigurableNetworkMqttUser {

  static {
    DebugUtils.registerIncludedFields("clientId");
  }

  final MqttConnection connection;
  final NetworkMqttUserReleaseHandler releaseHandler;
  final AtomicBoolean released;

  @Setter
  volatile String clientId;
  @Setter
  volatile String userName;

  @Setter
  @Getter
  @Nullable
  volatile NetworkMqttSession session;

  public AbstractNetworkMqttUser(MqttConnection connection, NetworkMqttUserReleaseHandler releaseHandler) {
    this.connection = connection;
    this.releaseHandler = releaseHandler;
    this.released = new AtomicBoolean(false);
    this.clientId = connection.remoteAddress();
  }

  @Override
  public String ipAddress() {
    return connection.remoteAddress();
  }

  @Override
  public void sendInBackground(SendableMqttMessage message) {
    sendInBackground((MqttOutMessage) message);
  }

  @Override
  public void sendInBackground(MqttOutMessage message) {
    log.debug(clientId, message.name(), message, "[%s] Send message to user:[%s] %s"::formatted);
    connection.sendInBackground(message);
  }

  @Override
  public CompletionStage<Boolean> sendAsync(SendableMqttMessage message) {
    return sendAsync((MqttOutMessage) message);
  }

  @Override
  public CompletableFuture<Boolean> sendAsync(MqttOutMessage message) {
    log.debug(clientId, message.name(), message, "[%s] Send message to user:[%s] %s"::formatted);
    return connection.sendAsync(message);
  }

  @Override
  public CompletableFuture<Boolean> closeWithReason(MqttOutMessage message) {
    log.debug(clientId(), message, "[%s] Close connection with reason: %s"::formatted);
    return sendAsync(message)
        .thenApply(sent -> {
          connection.close();
          return sent;
        });
  }

  @Override
  public void reject(ConnectAckMqtt311OutMessage connectAsk) {
    connection
        .sendAsync(connectAsk)
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
