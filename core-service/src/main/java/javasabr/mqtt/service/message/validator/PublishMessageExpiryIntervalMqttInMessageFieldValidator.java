package javasabr.mqtt.service.message.validator;

import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttProtocolErrors;
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
public class PublishMessageExpiryIntervalMqttInMessageFieldValidator extends
    MqttInMessageFieldValidator<NetworkMqttUser, PublishMqttInMessage> {

  public static final int ORDER = PublishRetainMqttInMessageFieldValidator.ORDER + 1;
  
  MessageOutFactoryService messageOutFactoryService;
  
  @Override
  public boolean isNotValid(MqttConnection connection, NetworkMqttUser user, PublishMqttInMessage message) {
    long messageExpiryInterval = message.messageExpiryInterval();
    if (messageExpiryInterval != MqttProperties.MESSAGE_EXPIRY_INTERVAL_IS_NOT_SET
        && messageExpiryInterval < MqttProperties.MESSAGE_EXPIRY_INTERVAL_MIN) {
      log.warn(user.clientId(), "[%s] Provided invalid MessageExpiryInterval"::formatted);
      handleInvalidMessageExpiryInterval(user);
      return true;
    }
    return false;
  }

  @Override
  public int order() {
    return ORDER;
  }

  private void handleInvalidMessageExpiryInterval(NetworkMqttUser user) {
    user.closeWithReason(messageOutFactoryService
        .resolveFactory(user)
        .newDisconnect(
            user,
            DisconnectReasonCode.PROTOCOL_ERROR,
            MqttProtocolErrors.PROVIDED_INVALID_MESSAGE_EXPIRY_INTERVAL));
  }
}
