package javasabr.mqtt.network;

import javasabr.mqtt.network.out.MqttPacketOutFactory;
import javasabr.mqtt.network.packet.out.MqttWritablePacket;
import javasabr.mqtt.model.MqttConnectionConfig;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode;
import javasabr.mqtt.network.packet.in.MqttReadablePacket;
import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

public interface MqttClient extends MqttUser {

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
