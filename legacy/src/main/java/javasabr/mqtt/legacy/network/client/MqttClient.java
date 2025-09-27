package javasabr.mqtt.legacy.network.client;

import javasabr.mqtt.legacy.config.MqttConnectionConfig;
import javasabr.mqtt.legacy.out.MqttPacketOutFactory;
import javasabr.mqtt.legacy.model.MqttSession;
import javasabr.mqtt.legacy.model.reason.code.ConnectAckReasonCode;
import javasabr.mqtt.legacy.network.MqttConnection;
import javasabr.mqtt.legacy.network.packet.in.MqttReadablePacket;
import javasabr.mqtt.legacy.network.packet.out.MqttWritablePacket;
import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

public interface MqttClient {

  interface UnsafeMqttClient extends MqttClient {

    MqttConnection getConnection();

    void handle(MqttReadablePacket packet);

    void configure(
        long sessionExpiryInterval,
        int receiveMax,
        int maximumPacketSize,
        int topicAliasMaximum,
        int keepAlive,
        boolean requestResponseInformation,
        boolean requestProblemInformation);

    void setClientId(String clientId);

    void setSession(@Nullable MqttSession session);

    void reject(ConnectAckReasonCode reasonCode);

    Mono<?> release();
  }

  MqttPacketOutFactory getPacketOutFactory();

  MqttConnectionConfig getConnectionConfig();

  String getClientId();

  @Nullable
  MqttSession getSession();

  int getKeepAlive();

  int getMaximumPacketSize();

  int getReceiveMax();

  int getTopicAliasMaximum();

  long getSessionExpiryInterval();

  void send(MqttWritablePacket packet);

  CompletableFuture<Boolean> sendWithFeedback(MqttWritablePacket packet);
}
