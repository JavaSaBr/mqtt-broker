package javasabr.mqtt.service.message.validator;

import javasabr.mqtt.model.reason.code.ConnectAckReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.in.ConnectMqttInMessage;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.mqtt.service.MessageOutFactoryService;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@CustomLog
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ClientIdMqttInMessageFieldValidator extends
    MqttInMessageFieldValidator<NetworkMqttUser, ConnectMqttInMessage> {

  public static final int ORDER = 10;
  
  MessageOutFactoryService messageOutFactoryService;

  @Override
  public boolean isNotValid(MqttConnection connection, NetworkMqttUser user, ConnectMqttInMessage message) {
    String clientId = message.clientId();
    if (clientId == null || clientId.isBlank()) {
      handleNotValidClientId(user);
      return true;
    }
    return false;
  }

  @Override
  public int order() {
    return ORDER;
  }
  
  private void handleNotValidClientId(NetworkMqttUser user) {
    user.closeWithReason(messageOutFactoryService
        .resolveFactory(user)
        .newConnectAck(user, ConnectAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID));
  }
}
