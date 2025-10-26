package javasabr.mqtt.network;

import java.util.concurrent.CompletableFuture;
import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.network.packet.out.ConnectAck311OutPacket;
import javasabr.mqtt.network.packet.out.MqttWritablePacket;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

public interface MqttClient extends MqttUser {

  interface UnsafeMqttClient extends MqttClient {

    MqttConnection connection();

    void clientId(String clientId);

    void session(@Nullable MqttSession session);

    void reject(ConnectAck311OutPacket connectAsk);

    Mono<?> release();
  }

  String clientId();

  @Nullable
  MqttSession session();

  MqttClientConnectionConfig connectionConfig();

  void send(MqttWritablePacket packet);

  CompletableFuture<Boolean> sendWithFeedback(MqttWritablePacket packet);
}
