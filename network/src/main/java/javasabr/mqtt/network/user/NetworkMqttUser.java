package javasabr.mqtt.network.user;

import java.util.concurrent.CompletableFuture;
import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.NetworkMqttSession;
import org.jspecify.annotations.Nullable;

public interface NetworkMqttUser extends MqttUser {

  MqttConnection connection();

  MqttClientConnectionConfig connectionConfig();
  
  @Nullable 
  @Override
  NetworkMqttSession session();

  void sendAsync(MqttOutMessage message);

  /**
   * @return the feature with result of delivering the message
   */
  CompletableFuture<Boolean> send(MqttOutMessage message);

  /**
   * @return the feature with result of delivering the reason before closing
   */
  CompletableFuture<Boolean> closeWithReason(MqttOutMessage message);
}
