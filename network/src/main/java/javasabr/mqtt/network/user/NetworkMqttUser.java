package javasabr.mqtt.network.user;

import java.util.concurrent.CompletableFuture;
import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.network.MqttNetworkSession;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import org.jspecify.annotations.Nullable;

public interface NetworkMqttUser extends MqttUser {
  
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
