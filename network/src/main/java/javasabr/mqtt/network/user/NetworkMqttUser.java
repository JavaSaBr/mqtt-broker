package javasabr.mqtt.network.user;

import java.util.concurrent.CompletableFuture;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.NetworkMqttSession;
import org.jspecify.annotations.Nullable;

public interface NetworkMqttUser extends MqttUser {

  MqttConnection connection();
  
  @Nullable 
  @Override
  NetworkMqttSession session();

  void sendInBackground(MqttOutMessage message);

  /**
   * @return the feature with result of delivering the message
   */
  CompletableFuture<Boolean> sendAsync(MqttOutMessage message);

  /**
   * @return the feature with result of delivering the reason before closing
   */
  CompletableFuture<Boolean> closeWithReason(MqttOutMessage message);
}
