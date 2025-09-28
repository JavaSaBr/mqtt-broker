package javasabr.mqtt.legacy.network.client;

import javasabr.mqtt.legacy.out.MqttPacketOutFactories;
import javasabr.mqtt.model.MqttConnectionConfig;
import javasabr.mqtt.legacy.out.MqttPacketOutFactory;
import javasabr.mqtt.legacy.handler.client.MqttClientReleaseHandler;
import javasabr.mqtt.legacy.handler.packet.in.PacketInHandler;
import javasabr.mqtt.legacy.network.MqttSession;
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode;
import javasabr.mqtt.legacy.network.MqttConnection;
import javasabr.mqtt.legacy.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.legacy.network.packet.in.MqttReadablePacket;
import javasabr.mqtt.legacy.network.packet.out.MqttWritablePacket;
import javasabr.mqtt.base.utils.DebugUtils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import javasabr.rlib.common.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

@Getter
@Log4j2
public abstract class AbstractMqttClient implements UnsafeMqttClient {

  static {
    DebugUtils.registerIncludedFields("clientId");
  }

  protected final MqttConnection connection;
  protected final MqttClientReleaseHandler releaseHandler;
  protected final AtomicBoolean released;

  @Setter
  private volatile String clientId;

  @Setter
  @Getter
  @Nullable
  private volatile MqttSession session;

  private volatile long sessionExpiryInterval;
  private volatile int receiveMax;
  private volatile int maximumPacketSize;
  private volatile int topicAliasMaximum;
  private volatile int keepAlive;

  private volatile boolean requestResponseInformation = false;
  private volatile boolean requestProblemInformation = false;

  public AbstractMqttClient(MqttConnection connection, MqttClientReleaseHandler releaseHandler) {
    this.connection = connection;
    this.releaseHandler = releaseHandler;
    this.released = new AtomicBoolean(false);
    this.clientId = StringUtils.EMPTY;
    var config = connection.getConfig();
    this.sessionExpiryInterval = config.getDefaultSessionExpiryInterval();
    this.receiveMax = config.getReceiveMaximum();
    this.maximumPacketSize = config.getMaximumPacketSize();
    this.topicAliasMaximum = config.getTopicAliasMaximum();
    this.keepAlive = config.getMinKeepAliveTime();
  }

  @Override
  public void handle(MqttReadablePacket packet) {
    log.debug("Client [{}] received packet: {} : {}", clientId, packet.getName(), packet);
    PacketInHandler packetHandler = connection.getPacketHandlers()[packet.getPacketType()];
    if (packetHandler != null) {
      packetHandler.handle(this, packet);
    } else {
      log.warn("No packet handler in client {} for packet {}", this, packet);
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
    log.debug("Send to client [{}] packet: {} : {}", clientId, packet.getName(), packet);
    connection.send(packet);
  }

  @Override
  public CompletableFuture<Boolean> sendWithFeedback(MqttWritablePacket packet) {
    log.debug("Send to client [{}] packet: {} : {}", clientId, packet.getName(), packet);
    return connection.sendWithFeedback(packet);
  }

  public void reject(ConnectAckReasonCode reasonCode) {
    connection
        .sendWithFeedback(getPacketOutFactory().newConnectAck(this, reasonCode))
        .thenAccept(sent -> connection.close());
  }

  @Override
  public MqttPacketOutFactory getPacketOutFactory() {
    return MqttPacketOutFactories.of(connection.getMqttVersion());
  }

  @Override
  public MqttConnectionConfig getConnectionConfig() {
    return connection.getConfig();
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
