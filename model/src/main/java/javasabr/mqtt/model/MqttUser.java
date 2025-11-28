package javasabr.mqtt.model;

import javasabr.mqtt.model.session.MqttSession;
import org.jspecify.annotations.Nullable;

public interface MqttUser {

  String clientId();
  
  @Nullable
  String userName();
  
  String ipAddress();
  
  @Nullable 
  MqttSession session();
}
