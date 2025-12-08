package javasabr.mqtt.service.message.validator;

import javasabr.mqtt.model.MqttProtocolErrors;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.model.topic.TopicValidator;
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
public class PublishResponseTopicMqttInMessageFieldValidator extends
    MqttInMessageFieldValidator<NetworkMqttUser, PublishMqttInMessage> {

  public static final int ORDER = PublishMessageExpiryIntervalMqttInMessageFieldValidator.ORDER + 1;
  
  MessageOutFactoryService messageOutFactoryService;

  @Override
  public boolean isNotValid(MqttConnection connection, NetworkMqttUser user, PublishMqttInMessage message) {
    String rawResponseTopicName = message.rawResponseTopicName();
    if (rawResponseTopicName != null) {
      if (!TopicValidator.validateTopicName(rawResponseTopicName)) {
        log.warning(user.clientId(), rawResponseTopicName, "[%s] Provided invalid ResponseTopic:[%s]"::formatted);
        handleInvalidResponseTopicName(user);
        return true;
      }
    }
    return false;
  }

  @Override
  public int order() {
    return ORDER;
  }
  
  private void handleInvalidResponseTopicName(NetworkMqttUser user) {
    user.closeWithReason(messageOutFactoryService
        .resolveFactory(user)
        .newDisconnect(
            user, 
            DisconnectReasonCode.PROTOCOL_ERROR,
            MqttProtocolErrors.PROVIDED_INVALID_RESPONSE_TOPIC));
  }
}
