package javasabr.mqtt.service.message.handler.impl;

import java.util.List;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttProtocolErrors;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.model.session.TopicNameMapping;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.model.topic.TopicValidator;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.in.PublishMqttInMessage;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.service.AuthorizationService;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.PublishReceivingService;
import javasabr.mqtt.service.TopicService;
import javasabr.mqtt.service.message.validator.MqttInMessageFieldValidator;
import javasabr.rlib.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublishMqttInMessageHandler
    extends FieldsValidatedMqttInMessageHandler<ExternalNetworkMqttUser, PublishMqttInMessage> {

  PublishReceivingService publishReceivingService;
  TopicService topicService;
  AuthorizationService authorizationService;

  public PublishMqttInMessageHandler(
      PublishReceivingService publishReceivingService,
      MessageOutFactoryService messageOutFactoryService,
      TopicService topicService, 
      AuthorizationService authorizationService,
      List<? extends MqttInMessageFieldValidator<? super ExternalNetworkMqttUser, PublishMqttInMessage>> fieldValidators) {
    super(ExternalNetworkMqttUser.class, PublishMqttInMessage.class, messageOutFactoryService, fieldValidators);
    this.publishReceivingService = publishReceivingService;
    this.topicService = topicService;
    this.authorizationService = authorizationService;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH;
  }

  @Override
  protected void processMessageWithValidFields(
      MqttConnection connection,
      ExternalNetworkMqttUser user,
      NetworkMqttSession session,
      PublishMqttInMessage publishMessage) {
    
    TopicName finalTopicName = resolveFinalTopicName(user, session, publishMessage);
    if (finalTopicName == null) {
      return;
    } else if (!authorizationService.authorizePublish(user, finalTopicName)) {
      handleNotAuthorize(user);
      return;
    }

    byte[] payload = publishMessage.payload();
    TopicName responseTopicName = resolveResponseTopic(user, publishMessage);

    //noinspection DataFlowIssue everything is already validated
    Publish publish = new Publish(
        publishMessage.messageId(),
        publishMessage.qos(),
        finalTopicName,
        responseTopicName,
        payload,
        publishMessage.duplicate(),
        publishMessage.retain(),
        publishMessage.contentType(),
        publishMessage.subscriptionIds(),
        publishMessage.correlationData(),
        publishMessage.messageExpiryInterval(),
        publishMessage.topicAlias(),
        publishMessage.payloadFormat(),
        publishMessage.userProperties());

    publishReceivingService.processPublish(user, publish);
  }
  
  @Nullable
  private TopicName resolveFinalTopicName(
      ExternalNetworkMqttUser user,
      NetworkMqttSession session,
      PublishMqttInMessage publishMessage) {

    TopicNameMapping topicNameMapping = session.topicNameMapping();
    String rawTopicName = publishMessage.rawTopicName();
    boolean providedRawTopicName = !StringUtils.isEmpty(rawTopicName);
    int topicAlias = publishMessage.topicAlias();

    TopicName topicNameByAlias;
    TopicName finalTopicName;

    if (!providedRawTopicName) {
      topicNameByAlias = topicNameMapping.resolve(topicAlias);
      if (topicNameByAlias == null) {
        log.warning(user.clientId(), topicAlias, "[%s] Unknown TopicAlias:[%d]"::formatted);
        handleNotProvidedTopicName(user);
        return null;
      }
      finalTopicName = topicNameByAlias;
    } else {
      if (!TopicValidator.validateTopicName(rawTopicName)) {
        handleInvalidTopicName(user);
        log.warning(user.clientId(), rawTopicName, "[%s] TopicName:[%s] is invalid"::formatted);
        return null;
      }
      finalTopicName = topicService.createTopicName(user, rawTopicName);
      if (topicAlias != MqttProperties.TOPIC_ALIAS_NOT_SET) {
        topicNameMapping.update(topicAlias, finalTopicName);
      }
    }
    return finalTopicName;
  }

  @Nullable
  private TopicName resolveResponseTopic(ExternalNetworkMqttUser user, PublishMqttInMessage publishMessage) {
    String rawResponseTopicName = publishMessage.rawResponseTopicName();
    if (rawResponseTopicName != null) {
      return topicService.createTopicName(user, rawResponseTopicName);
    }
    return null;
  }

  private void handleNotProvidedTopicName(ExternalNetworkMqttUser user) {
    user.closeWithReason(messageOutFactoryService
        .resolveFactory(user)
        .newDisconnect(user, DisconnectReasonCode.PROTOCOL_ERROR, MqttProtocolErrors.NO_ANY_TOPIC_NANE));
  }
  
  private void handleNotAuthorize(ExternalNetworkMqttUser user) {
    user.closeWithReason(messageOutFactoryService
        .resolveFactory(user)
        .newDisconnect(user, DisconnectReasonCode.NOT_AUTHORIZED));
  }

  private void handleInvalidTopicName(ExternalNetworkMqttUser user) {
    user.closeWithReason(messageOutFactoryService
        .resolveFactory(user)
        .newDisconnect(user, DisconnectReasonCode.TOPIC_NAME_INVALID));
  }
}
