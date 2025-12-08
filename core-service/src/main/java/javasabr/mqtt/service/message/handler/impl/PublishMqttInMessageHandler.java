package javasabr.mqtt.service.message.handler.impl;

import java.util.List;
import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttProtocolErrors;
import javasabr.mqtt.model.PayloadFormat;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.model.session.TopicNameMapping;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.model.topic.TopicValidator;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.in.PublishMqttInMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.service.AclService;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.PublishReceivingService;
import javasabr.mqtt.service.TopicService;
import javasabr.mqtt.service.message.validator.MqttInMessageFieldValidator;
import javasabr.rlib.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublishMqttInMessageHandler
    extends FieldsValidatedMqttInMessageHandler<ExternalNetworkMqttUser, PublishMqttInMessage> {

  PublishReceivingService publishReceivingService;
  TopicService topicService;
  AclService aclService;

  public PublishMqttInMessageHandler(
      PublishReceivingService publishReceivingService,
      MessageOutFactoryService messageOutFactoryService,
      TopicService topicService, 
      AclService aclService,
      List<? extends MqttInMessageFieldValidator<? super ExternalNetworkMqttUser, PublishMqttInMessage>> fieldValidators) {
    super(ExternalNetworkMqttUser.class, PublishMqttInMessage.class, messageOutFactoryService, fieldValidators);
    this.publishReceivingService = publishReceivingService;
    this.topicService = topicService;
    this.aclService = aclService;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH;
  }

  @Override
  protected void processValidMessage(
      MqttConnection connection,
      ExternalNetworkMqttUser user,
      NetworkMqttSession session,
      PublishMqttInMessage publishMessage) {

    if (!validateBaseFields(connection, user, publishMessage)) {
      return;
    }

    String rawResponseTopicName = publishMessage.rawResponseTopicName();
    TopicName responseTopicName = null;
    if (rawResponseTopicName != null) {
      if (!TopicValidator.validateTopicName(rawResponseTopicName)) {
        log.warning(user.clientId(), rawResponseTopicName, "[%s] Provided invalid response TopicName:[%s]"::formatted);
        handleInvalidResponseTopicName(user);
        return;
      }
      responseTopicName = topicService.createTopicName(user, rawResponseTopicName);
    }

    MqttClientConnectionConfig connectionConfig = connection.clientConnectionConfig();
    int topicAliasMaxValue = connectionConfig.topicAliasMaxValue();
    TopicNameMapping topicNameMapping = session.topicNameMapping();
    TopicName topicNameByAlias = null;

    String rawTopicName = publishMessage.rawTopicName();
    boolean providedRawTopicName = !StringUtils.isEmpty(rawTopicName);
    int topicAlias = publishMessage.topicAlias();

    if (!providedRawTopicName) {
      if (topicAlias == MqttProperties.TOPIC_ALIAS_NOT_SET) {
        log.warning(user.clientId(), "[%s] Not provided any information about TopicName"::formatted);
        handleNotProvidedTopicName(user);
        return;
      } else if (topicAlias < MqttProperties.TOPIC_ALIAS_MIN || topicAlias > topicAliasMaxValue) {
        log.warning(user.clientId(), topicAlias, "[%s] Provided invalid TopicAlias:[%d]"::formatted);
        handleInvalidTopicAlias(user);
        return;
      }
      topicNameByAlias = topicNameMapping.resolve(topicAlias);
      if (topicNameByAlias == null) {
        log.warning(user.clientId(), topicAlias, "[%s] Unknown TopicAlias:[%d]"::formatted);
        handleNotProvidedTopicName(user);
        return;
      }
    }

    TopicName topicName;

    if (providedRawTopicName) {
      if (!TopicValidator.validateTopicName(rawTopicName)) {
        handleInvalidTopicName(user, session, publishMessage);
        log.warning(user.clientId(), publishMessage.rawTopicName(), "[%s] TopicName:[%s] is invalid"::formatted);
        return;
      }
      topicName = topicService.createTopicName(user, rawTopicName);
      if (topicAlias != MqttProperties.TOPIC_ALIAS_NOT_SET) {
        topicNameMapping.update(topicAlias, topicName);
      }
    } else {
      topicName = topicNameByAlias;
    }

    if (!aclService.authorizePublish(user, topicName)) {
      handleNotAuthorize(user);
      return;
    }

    byte[] payload = publishMessage.payload();

    //noinspection DataFlowIssue everything is already validated
    Publish publish = new Publish(
        publishMessage.messageId(),
        publishMessage.qos(),
        topicName,
        responseTopicName,
        payload,
        publishMessage.duplicate(),
        publishMessage.retain(),
        publishMessage.contentType(),
        publishMessage.subscriptionIds(),
        publishMessage.correlationData(),
        publishMessage.messageExpiryInterval(),
        topicAlias,
        publishMessage.payloadFormat(),
        publishMessage.userProperties());

    publishReceivingService.processPublish(user, publish);
  }

  private boolean validateBaseFields(
      MqttConnection connection,
      ExternalNetworkMqttUser user,
      PublishMqttInMessage publishMessage) {
 
    MqttClientConnectionConfig connectionConfig = connection.clientConnectionConfig();

    boolean retain = publishMessage.retain();
    if (retain && !connectionConfig.retainAvailable()) {
      log.warning(user.clientId(), "[%s] 'RETAIN' option is not supported"::formatted);
      handleNotSupportedRetain(user);
      return false;
    }

    PayloadFormat payloadFormat = publishMessage.payloadFormat();
    if (payloadFormat == PayloadFormat.INVALID) {
      log.warning(user.clientId(), "[%s] Provided invalid PayloadFormat"::formatted);
      handleInvalidPayloadFormat(user);
      return false;
    }

    long messageExpiryInterval = publishMessage.messageExpiryInterval();
    if (messageExpiryInterval != MqttProperties.MESSAGE_EXPIRY_INTERVAL_IS_NOT_SET
        && messageExpiryInterval < MqttProperties.MESSAGE_EXPIRY_INTERVAL_MIN) {
      log.warning(user.clientId(), "[%s] Provided invalid MessageExpiryInterval"::formatted);
      handleInvalidMessageExpiryInterval(user);
      return false;
    }
    return true;
  }


  private void handleNotSupportedRetain(ExternalNetworkMqttUser user) {
    user.closeWithReason(messageOutFactoryService
        .resolveFactory(user)
        .newDisconnect(user, DisconnectReasonCode.RETAIN_NOT_SUPPORTED));
  }

  private void handleNotProvidedTopicName(ExternalNetworkMqttUser user) {
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(user)
        .newDisconnect(user, DisconnectReasonCode.PROTOCOL_ERROR, MqttProtocolErrors.NO_ANY_TOPIC_NANE);
    user.closeWithReason(response);
  }

  private void handleInvalidTopicAlias(ExternalNetworkMqttUser user) {
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(user)
        .newDisconnect(user, DisconnectReasonCode.TOPIC_ALIAS_INVALID);
    user.closeWithReason(response);
  }

  private void handleInvalidPayloadFormat(ExternalNetworkMqttUser user) {
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(user)
        .newDisconnect(user, DisconnectReasonCode.PROTOCOL_ERROR, MqttProtocolErrors.PROVIDED_INVALID_PAYLOAD_FORMAT);
    user.closeWithReason(response);
  }

  private void handleInvalidResponseTopicName(ExternalNetworkMqttUser user) {
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(user)
        .newDisconnect(user, DisconnectReasonCode.PROTOCOL_ERROR, MqttProtocolErrors.INVALID_RESPONSE_TOPIC_NAME);
    user.closeWithReason(response);
  }

  private void handleInvalidMessageExpiryInterval(ExternalNetworkMqttUser user) {
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(user)
        .newDisconnect(user, DisconnectReasonCode.PROTOCOL_ERROR, MqttProtocolErrors.PROVIDED_INVALID_MESSAGE_EXPIRY_INTERVAL);
    user.closeWithReason(response);
  }

  private void handleNotAuthorize(ExternalNetworkMqttUser user) {
    user.closeWithReason(messageOutFactoryService
        .resolveFactory(user)
        .newDisconnect(user, DisconnectReasonCode.NOT_AUTHORIZED));
  }

  private void handleInvalidTopicName(
      ExternalNetworkMqttUser user,
      NetworkMqttSession session,
      PublishMqttInMessage publishMessage) {
    int messagedId = publishMessage.messageId();
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(user)
        .newDisconnect(user, DisconnectReasonCode.TOPIC_NAME_INVALID);
    // without messageId we do not need to clean it
    if (messagedId == MqttProperties.MESSAGE_ID_IS_NOT_SET) {
      user.closeWithReason(response);
      return;
    }
    user
        .closeWithReason(response)
        .thenAccept(_ -> session
            .inMessageTracker()
            .remove(messagedId));
  }
}
