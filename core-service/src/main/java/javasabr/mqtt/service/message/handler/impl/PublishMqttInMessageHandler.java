package javasabr.mqtt.service.message.handler.impl;

import java.util.List;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttProtocolErrors;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.publish.PublishData;
import javasabr.mqtt.model.publish.ReceivedPublish;
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
import javasabr.mqtt.service.PublishDataStorage;
import javasabr.mqtt.service.IncomingPublishRouter;
import javasabr.mqtt.service.TopicService;
import javasabr.mqtt.service.message.validator.PublishMessageExpiryIntervalMqttInMessageFieldValidator;
import javasabr.mqtt.service.message.validator.PublishPayloadMqttInMessageFieldValidator;
import javasabr.mqtt.service.message.validator.PublishQosMqttInMessageFieldValidator;
import javasabr.mqtt.service.message.validator.PublishResponseTopicMqttInMessageFieldValidator;
import javasabr.mqtt.service.message.validator.PublishRetainMqttInMessageFieldValidator;
import javasabr.mqtt.service.message.validator.PublishTopicAliasMqttInMessageFieldValidator;
import javasabr.rlib.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublishMqttInMessageHandler
    extends FieldsValidatedMqttInMessageHandler<ExternalNetworkMqttUser, PublishMqttInMessage> {

  IncomingPublishRouter incomingPublishRouter;
  TopicService topicService;
  AuthorizationService authorizationService;
  PublishDataStorage publishDataStorage;

  public PublishMqttInMessageHandler(
      IncomingPublishRouter incomingPublishRouter,
      MessageOutFactoryService messageOutFactoryService,
      TopicService topicService,
      AuthorizationService authorizationService, 
      PublishDataStorage publishDataStorage) {
    super(
        ExternalNetworkMqttUser.class, 
        PublishMqttInMessage.class, 
        messageOutFactoryService, 
        List.of(
            new PublishMessageExpiryIntervalMqttInMessageFieldValidator(messageOutFactoryService),
            new PublishResponseTopicMqttInMessageFieldValidator(messageOutFactoryService),
            new PublishRetainMqttInMessageFieldValidator(messageOutFactoryService),
            new PublishTopicAliasMqttInMessageFieldValidator(messageOutFactoryService),
            new PublishQosMqttInMessageFieldValidator(messageOutFactoryService),
            new PublishPayloadMqttInMessageFieldValidator(messageOutFactoryService)));
    this.incomingPublishRouter = incomingPublishRouter;
    this.topicService = topicService;
    this.authorizationService = authorizationService;
    this.publishDataStorage = publishDataStorage;
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
      PublishMqttInMessage message) {
    
    TopicName finalTopicName = resolveFinalTopicName(user, session, message);
    if (finalTopicName == null) {
      return;
    } else if (!authorizationService.authorizePublish(user, finalTopicName)) {
      log.warning(user.clientId(), finalTopicName, "[%s] Not authorized for publish to:[%s]"::formatted);
      handleNotAuthorize(user);
      return;
    }

    TopicName responseTopicName = resolveResponseTopic(user, message);
    
    // already tested in message validators
    @SuppressWarnings("DataFlowIssue") 
    PublishData storedPublishData = publishDataStorage.store(
        session.generateDataId(),
        message.contentType(),
        message.payloadFormat(),
        message.payload(),
        message.correlationData());

    ReceivedPublish receivedPublish = new ReceivedPublish(
        message.messageId(),
        message.qos(),
        finalTopicName,
        responseTopicName,
        storedPublishData,
        message.duplicate(),
        message.retain(),
        message.subscriptionIds(),
        message.messageExpiryInterval(),
        message.topicAlias(),
        message.userProperties());

    incomingPublishRouter.route(user, receivedPublish);
  }
  
  @Nullable
  private TopicName resolveFinalTopicName(
      ExternalNetworkMqttUser user,
      NetworkMqttSession session,
      PublishMqttInMessage message) {

    TopicNameMapping topicNameMapping = session.topicNameMapping();
    String rawTopicName = message.rawTopicName();
    boolean providedRawTopicName = StringUtils.isNotEmpty(rawTopicName);
    int topicAlias = message.topicAlias();

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
