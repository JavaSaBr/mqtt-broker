package javasabr.mqtt.network;

import java.util.concurrent.CompletableFuture;
import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.network.message.out.ConnectAckMqtt311OutMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.MqttNetworkSession;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

public interface MqttClient extends MqttUser {

  interface UnsafeMqttClient extends MqttClient {

    MqttConnection connection();

    void clientId(String clientId);

    void session(@Nullable MqttNetworkSession session);

    void reject(ConnectAckMqtt311OutMessage connectAsk);

    Mono<?> release();
  }

  @Nullable 
  @Override
  MqttNetworkSession session();

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
