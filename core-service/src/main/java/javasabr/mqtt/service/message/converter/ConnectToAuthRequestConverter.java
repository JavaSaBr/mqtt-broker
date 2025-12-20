package javasabr.mqtt.service.message.converter;

import javasabr.mqtt.auth.api.AuthRequest;
import javasabr.mqtt.network.message.in.ConnectMqttInMessage;

public class ConnectToAuthRequestConverter implements MessageConverter<ConnectMqttInMessage, AuthRequest> {

  @Override
  public AuthRequest convert(ConnectMqttInMessage message) {
    return new AuthRequest(
        message.username(),
        message.password(),
        message.authenticationMethod(),
        message.authenticationData());
  }
}
