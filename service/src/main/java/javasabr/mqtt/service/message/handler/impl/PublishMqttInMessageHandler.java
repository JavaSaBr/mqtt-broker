package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttProtocolErrors;
import javasabr.mqtt.model.PayloadFormat;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.model.reason.code.PublishAckReasonCode;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.model.topic.TopicValidator;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.message.in.PublishMqttInMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.MessageTacker;
import javasabr.mqtt.network.session.MqttSession;
import javasabr.mqtt.network.session.TopicNameMapping;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.PublishReceivingService;
import javasabr.mqtt.service.TopicService;
import javasabr.rlib.collections.array.IntArray;
import javasabr.rlib.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublishMqttInMessageHandler
    extends AbstractMqttInMessageHandler<ExternalMqttClient, PublishMqttInMessage> {

  PublishReceivingService publishReceivingService;
  TopicService topicService;

  public PublishMqttInMessageHandler(
      PublishReceivingService publishReceivingService,
      MessageOutFactoryService messageOutFactoryService,
      TopicService topicService) {
    super(ExternalMqttClient.class, PublishMqttInMessage.class, messageOutFactoryService);
    this.publishReceivingService = publishReceivingService;
    this.topicService = topicService;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH;
  }

  @Override
  protected void processValidMessage(
      MqttConnection connection,
      ExternalMqttClient client,
      MqttSession session,
      PublishMqttInMessage publishMessage) {

    int messageId = publishMessage.messageId();
    QoS requestedQos = publishMessage.qos();

    MessageTacker messageTacker = session.inMessageTracker();
    if (messageId > 0 && messageTacker.isInUse(messageId)) {
      log.warning(client.clientId(), messageId, "[%s] MessageId:[%d] is already in use"::formatted);
      handleMessageIdIsInUse(client, publishMessage);
      return;
    } else if (messageId == MqttProperties.MESSAGE_ID_IS_NOT_SET && QoS.AT_MOST_ONCE != requestedQos) {
      log.warning(client.clientId(), messageId, "[%s] Missed MessageId"::formatted);
      handleMissedMessageId(client);
      return;
    }

    MqttClientConnectionConfig connectionConfig = connection.clientConnectionConfig();
    if (connectionConfig.maxQos().isLower(requestedQos)) {
      log.warning(client.clientId(), requestedQos, "[%s] Requested QoS:[%s] is not supported"::formatted);
      handleNotSupportedQos(client);
      return;
    }
    boolean retain = publishMessage.retain();
    if (retain && !connectionConfig.retainAvailable()) {
      log.warning(client.clientId(), "[%s] 'RETAIN' option is not supported"::formatted);
      handleNotSupportedRetain(client);
      return;
    }
    PayloadFormat payloadFormat = publishMessage.payloadFormat();
    byte[] payload = publishMessage.payload();
    long messageExpiryInterval = publishMessage.messageExpiryInterval();

    String rawResponseTopicName = publishMessage.rawResponseTopicName();
    TopicName responseTopicName = null;
    if (rawResponseTopicName != null) {
      if (!TopicValidator.validateTopicName(rawResponseTopicName)) {
        log.warning(client.clientId(), rawResponseTopicName, "[%s] Provided invalid response TopicName:[%d]"::formatted);
        handleInvalidResponseTopicName(client);
        return;
      }
      responseTopicName = topicService.createTopicName(client, rawResponseTopicName);
    }

    int topicAliasMaxValue = connectionConfig.topicAliasMaxValue();
    TopicNameMapping topicNameMapping = session.topicNameMapping();
    TopicName topicNameByAlias = null;

    String rawTopicName = publishMessage.rawTopicName();
    boolean providedRawTopicName = !StringUtils.isEmpty(rawTopicName);
    int topicAlias = publishMessage.topicAlias();

    if (!providedRawTopicName) {
      if (topicAlias == MqttProperties.TOPIC_ALIAS_NOT_SET) {
        log.warning(client.clientId(), "[%s] Not provided any information about TopicName"::formatted);
        handleNotProvidedTopicName(client);
        return;
      } else if (topicAlias < MqttProperties.TOPIC_ALIAS_MIN || topicAlias > topicAliasMaxValue) {
        log.warning(client.clientId(), topicAlias, "[%s] Provided invalid TopicAlias:[%d]"::formatted);
        handleInvalidTopicAlias(client);
        return;
      }
      topicNameByAlias = topicNameMapping.resolve(topicAlias);
      if (topicNameByAlias == null) {
        log.warning(client.clientId(), topicAlias, "[%s] Unknown TopicAlias:[%d]"::formatted);
        handleNotProvidedTopicName(client);
        return;
      }
    }

    if (messageId > 0) {
      messageTacker.add(messageId);
    }

    TopicName topicName;

    if (providedRawTopicName) {
      if (!TopicValidator.validateTopicName(rawTopicName)) {
        handleInvalidTopicName(client, session, publishMessage);
        log.warning(client.clientId(), publishMessage.rawTopicName(), "[%s] TopicName:[%s] is invalid"::formatted);
        return;
      }
      topicName = topicService.createTopicName(client, rawTopicName);
    } else {
      topicName = topicNameByAlias;
    }

    publishReceivingService.processPublish(client, new Publish(
        messageId,
        publishMessage.qos(),
        topicName,
        responseTopicName,
        payload,
        publishMessage.duplicate(),
        retain,
        publishMessage.contentType(),
        publishMessage.subscriptionIds(),
        publishMessage.correlationData(), messageExpiryInterval,
        topicAlias,
        payloadFormat,
        publishMessage.userProperties()));
  }

  private void handleMessageIdIsInUse(ExternalMqttClient client, PublishMqttInMessage publishMessage) {
    client.send(messageOutFactoryService
        .resolveFactory(client)
        .newPublishAck(publishMessage.messageId(), PublishAckReasonCode.PACKET_IDENTIFIER_IN_USE));
  }

  private void handleMissedMessageId(ExternalMqttClient client) {
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newPublishAck(
            MqttProperties.MESSAGE_ID_IS_NOT_SET,
            PublishAckReasonCode.UNSPECIFIED_ERROR,
            MqttProtocolErrors.MISSED_REQUIRED_MESSAGE_ID);
    client.send(response);
  }

  private void handleNotSupportedQos(ExternalMqttClient client) {
    client.closeWithReason(messageOutFactoryService
        .resolveFactory(client)
        .newDisconnect(client, DisconnectReasonCode.QOS_NOT_SUPPORTED));
  }

  private void handleNotSupportedRetain(ExternalMqttClient client) {
    client.closeWithReason(messageOutFactoryService
        .resolveFactory(client)
        .newDisconnect(client, DisconnectReasonCode.RETAIN_NOT_SUPPORTED));
  }

  private void handleNotProvidedTopicName(ExternalMqttClient client) {
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newDisconnect(client, DisconnectReasonCode.PROTOCOL_ERROR, MqttProtocolErrors.NO_ANY_TOPIC_NANE);
    client.closeWithReason(response);
  }

  private void handleInvalidTopicAlias(ExternalMqttClient client) {
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newDisconnect(client, DisconnectReasonCode.PROTOCOL_ERROR, MqttProtocolErrors.INVALID_TOPIC_ALIAS);
    client.closeWithReason(response);
  }

  private void handleInvalidResponseTopicName(ExternalMqttClient client) {
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newDisconnect(client, DisconnectReasonCode.PROTOCOL_ERROR, MqttProtocolErrors.INVALID_RESPONSE_TOPIC_NAME);
    client.closeWithReason(response);
  }

  private void handleInvalidTopicName(
      ExternalMqttClient client,
      MqttSession session,
      PublishMqttInMessage publishMessage) {
    int messagedId = publishMessage.messageId();
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newPublishAck(messagedId, PublishAckReasonCode.TOPIC_NAME_INVALID);
    client
        .sendWithFeedback(response)
        .thenAccept(_ -> session
            .inMessageTracker()
            .remove(messagedId));
  }

}
