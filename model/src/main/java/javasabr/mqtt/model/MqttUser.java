package javasabr.mqtt.model;

import java.util.concurrent.CompletionStage;
import javasabr.mqtt.model.message.SendableMqttMessage;
import javasabr.mqtt.model.session.MqttSession;
import org.jspecify.annotations.Nullable;

public interface MqttUser {

  String clientId();
  
  @Nullable
  String userName();
  
  String ipAddress();
  
  @Nullable 
  MqttSession session();

  MqttClientConnectionConfig connectionConfig();
  
  void sendInBackground(SendableMqttMessage message);

  /**
   * @return the feature with result of delivering the message
   */
  CompletionStage<Boolean> sendAsync(SendableMqttMessage message);
}
