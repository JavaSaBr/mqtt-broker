package javasabr.mqtt.service.message.validator;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.in.MqttInMessage;

public abstract class MqttInMessageFieldValidator<U extends MqttUser, M extends MqttInMessage> {
  
  public abstract boolean isNotValid(MqttConnection connection, U user, M message);
  
  public int order() {
    return 0;
  }
}
