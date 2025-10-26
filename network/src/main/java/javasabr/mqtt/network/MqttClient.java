package javasabr.mqtt.network;

import java.util.concurrent.CompletableFuture;
import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.network.message.out.ConnectAckMqtt311OutMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

public interface MqttClient extends MqttUser {

  interface UnsafeMqttClient extends MqttClient {

    MqttConnection connection();

    void clientId(String clientId);

    void session(@Nullable MqttSession session);

    void reject(ConnectAckMqtt311OutMessage connectAsk);

    Mono<?> release();
  }

  String clientId();

  @Nullable
  MqttSession session();

  MqttClientConnectionConfig connectionConfig();

  void send(MqttOutMessage packet);

  CompletableFuture<Boolean> sendWithFeedback(MqttOutMessage packet);
}
