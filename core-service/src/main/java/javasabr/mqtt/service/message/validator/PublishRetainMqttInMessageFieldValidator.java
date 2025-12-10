package javasabr.mqtt.service.message.validator;

import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.in.PublishMqttInMessage;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.mqtt.service.MessageOutFactoryService;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@CustomLog
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublishRetainMqttInMessageFieldValidator extends
    MqttInMessageFieldValidator<NetworkMqttUser, PublishMqttInMessage> {

  public static final int ORDER = PublishQosMqttInMessageFieldValidator.ORDER + 1;
  
  MessageOutFactoryService messageOutFactoryService;
  
  @Override
  public boolean isNotValid(MqttConnection connection, NetworkMqttUser user, PublishMqttInMessage message) {
    MqttClientConnectionConfig connectionConfig = connection.clientConnectionConfig();
    boolean retain = message.retain();
    if (retain && !connectionConfig.retainAvailable()) {
      log.warning(user.clientId(), "[%s] 'RETAIN' option is not supported"::formatted);
      handleNotSupportedRetain(user);
      return true;
    }
    return false;
  }

  @Override
  public int order() {
    return ORDER;
  }

  private void handleNotSupportedRetain(NetworkMqttUser user) {
    user.closeWithReason(messageOutFactoryService
        .resolveFactory(user)
        .newDisconnect(user, DisconnectReasonCode.RETAIN_NOT_SUPPORTED));
  }
}
