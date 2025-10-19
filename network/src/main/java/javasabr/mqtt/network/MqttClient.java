package javasabr.mqtt.network;

import java.util.concurrent.CompletableFuture;
import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode;
import javasabr.mqtt.network.out.MqttPacketOutFactory;
import javasabr.mqtt.network.packet.in.MqttReadablePacket;
import javasabr.mqtt.network.packet.out.MqttWritablePacket;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

public interface MqttClient extends MqttUser {

  interface UnsafeMqttClient extends MqttClient {

    MqttConnection connection();

    void handle(MqttReadablePacket packet);

    void clientId(String clientId);

    void session(@Nullable MqttSession session);

    void reject(ConnectAckReasonCode reasonCode);

    Mono<?> release();
  }

  MqttPacketOutFactory packetOutFactory();

  String clientId();

  @Nullable
  MqttSession session();

  MqttClientConnectionConfig connectionConfig();

  void send(MqttWritablePacket packet);

  CompletableFuture<Boolean> sendWithFeedback(MqttWritablePacket packet);
}
