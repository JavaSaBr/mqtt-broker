package javasabr.mqtt.network;

import java.util.concurrent.CompletableFuture;
import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.subscribtion.SubscriptionOwner;
import javasabr.mqtt.network.message.out.ConnectAckMqtt311OutMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.MqttSession;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

public interface MqttClient extends SubscriptionOwner {

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

  void send(MqttOutMessage message);

  /**
   * @return the feature with result of delivering the message
   */
  CompletableFuture<Boolean> sendWithFeedback(MqttOutMessage message);

  /**
   * @return the feature with result of delivering the reason before closing
   */
  CompletableFuture<Boolean> closeWithReason(MqttOutMessage message);
}
