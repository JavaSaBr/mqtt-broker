package javasabr.mqtt.service.message.validator;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.in.PublishMqttInMessage;
import lombok.CustomLog;

@CustomLog
public class PublishPayloadMqttInMessageFieldValidator extends
    MqttInMessageFieldValidator<MqttUser, PublishMqttInMessage> {
 
  @Override
  public boolean validate(MqttConnection connection, MqttUser user, PublishMqttInMessage message) {
    byte[] payload = message.payload();
    if (payload == null) {
      log.warning(user.clientId(), "[%s] Missed payload"::formatted);
      return false;
    }
    return true;
  }
}
