package javasabr.mqtt.service.message.validator;

import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttProtocolErrors;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.in.PublishMqttInMessage;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.rlib.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@CustomLog
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublishTopicAliasMqttInMessageFieldValidator extends
    MqttInMessageFieldValidator<NetworkMqttUser, PublishMqttInMessage> {

  public static final int ORDER = PublishResponseTopicMqttInMessageFieldValidator.ORDER + 1;
  
  MessageOutFactoryService messageOutFactoryService;

  @Override
  public boolean isNotValid(MqttConnection connection, NetworkMqttUser user, PublishMqttInMessage message) {
    boolean providedRawTopicName = !StringUtils.isEmpty(message.rawTopicName());
    if (!providedRawTopicName) {
      int topicAlias = message.topicAlias();
      if (topicAlias == MqttProperties.TOPIC_ALIAS_NOT_SET) {
        log.warning(user.clientId(), "[%s] Not provided any information about TopicName"::formatted);
        handleNotProvidedTopicName(user);
        return true;
      }
      MqttClientConnectionConfig connectionConfig = connection.clientConnectionConfig();
      int topicAliasMaxValue = connectionConfig.topicAliasMaxValue();
      if (topicAlias < MqttProperties.TOPIC_ALIAS_MIN || topicAlias > topicAliasMaxValue) {
        log.warning(user.clientId(), topicAlias, "[%s] Provided invalid TopicAlias:[%d]"::formatted);
        handleInvalidTopicAlias(user);
        return true;
      }
    }
    return false;
  }

  @Override
  public int order() {
    return ORDER;
  }

  private void handleNotProvidedTopicName(NetworkMqttUser user) {
    user.closeWithReason(messageOutFactoryService
        .resolveFactory(user)
        .newDisconnect(user, DisconnectReasonCode.PROTOCOL_ERROR, MqttProtocolErrors.NO_ANY_TOPIC_NANE));
  }

  private void handleInvalidTopicAlias(NetworkMqttUser user) {
    user.closeWithReason(messageOutFactoryService
        .resolveFactory(user)
        .newDisconnect(user, DisconnectReasonCode.TOPIC_ALIAS_INVALID));
  }
}
