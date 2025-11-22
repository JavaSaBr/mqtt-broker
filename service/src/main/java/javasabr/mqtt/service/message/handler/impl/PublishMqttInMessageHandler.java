package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttProtocolErrors;
import javasabr.mqtt.model.PayloadFormat;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.model.session.TopicNameMapping;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.model.topic.TopicValidator;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.in.PublishMqttInMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.MqttSession;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.PublishReceivingService;
import javasabr.mqtt.service.TopicService;
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

    if (!validateBaseFields(connection, client, publishMessage)) {
      return;
    }

    String rawResponseTopicName = publishMessage.rawResponseTopicName();
    TopicName responseTopicName = null;
    if (rawResponseTopicName != null) {
      if (!TopicValidator.validateTopicName(rawResponseTopicName)) {
        log.warning(client.clientId(), rawResponseTopicName, "[%s] Provided invalid response TopicName:[%s]"::formatted);
        handleInvalidResponseTopicName(client);
        return;
      }
      responseTopicName = topicService.createTopicName(client, rawResponseTopicName);
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

    TopicName topicName;

    if (providedRawTopicName) {
      if (!TopicValidator.validateTopicName(rawTopicName)) {
        handleInvalidTopicName(client, session, publishMessage);
        log.warning(client.clientId(), publishMessage.rawTopicName(), "[%s] TopicName:[%s] is invalid"::formatted);
        return;
      }
      topicName = topicService.createTopicName(client, rawTopicName);
      if (topicAlias != MqttProperties.TOPIC_ALIAS_NOT_SET) {
        topicNameMapping.update(topicAlias, topicName);
      }
    } else {
      topicName = topicNameByAlias;
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

    publishReceivingService.processPublish(client, publish);
  }

  private boolean validateBaseFields(
      MqttConnection connection,
      ExternalMqttClient client,
      PublishMqttInMessage publishMessage) {
    byte[] payload = publishMessage.payload();
    if (payload == null) {
      log.warning(client.clientId(), "[%s] Unexpected missed payload"::formatted);
      return false;
    }

    QoS requestedQos = publishMessage.qos();
    MqttClientConnectionConfig connectionConfig = connection.clientConnectionConfig();
    if (connectionConfig.maxQos().isLowerThan(requestedQos)) {
      log.warning(client.clientId(), requestedQos, "[%s] Requested QoS:[%s] is not supported"::formatted);
      handleNotSupportedQos(client);
      return false;
    }

    boolean retain = publishMessage.retain();
    if (retain && !connectionConfig.retainAvailable()) {
      log.warning(client.clientId(), "[%s] 'RETAIN' option is not supported"::formatted);
      handleNotSupportedRetain(client);
      return false;
    }

    PayloadFormat payloadFormat = publishMessage.payloadFormat();
    if (payloadFormat == PayloadFormat.INVALID) {
      log.warning(client.clientId(), "[%s] Provided invalid PayloadFormat"::formatted);
      handleInvalidPayloadFormat(client);
      return false;
    }

    long messageExpiryInterval = publishMessage.messageExpiryInterval();
    if (messageExpiryInterval != MqttProperties.MESSAGE_EXPIRY_INTERVAL_IS_NOT_SET
        && messageExpiryInterval < MqttProperties.MESSAGE_EXPIRY_INTERVAL_MIN) {
      log.warning(client.clientId(), "[%s] Provided invalid MessageExpiryInterval"::formatted);
      handleInvalidMessageExpiryInterval(client);
      return false;
    }
    return true;
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
        .newDisconnect(client, DisconnectReasonCode.TOPIC_ALIAS_INVALID);
    client.closeWithReason(response);
  }

  private void handleInvalidPayloadFormat(ExternalMqttClient client) {
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newDisconnect(client, DisconnectReasonCode.PROTOCOL_ERROR, MqttProtocolErrors.INVALID_PAYLOAD_FORMAT);
    client.closeWithReason(response);
  }

  private void handleInvalidResponseTopicName(ExternalMqttClient client) {
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newDisconnect(client, DisconnectReasonCode.PROTOCOL_ERROR, MqttProtocolErrors.INVALID_RESPONSE_TOPIC_NAME);
    client.closeWithReason(response);
  }

  private void handleInvalidMessageExpiryInterval(ExternalMqttClient client) {
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newDisconnect(client, DisconnectReasonCode.PROTOCOL_ERROR, MqttProtocolErrors.INVALID_MESSAGE_EXPIRY_INTERVAL);
    client.closeWithReason(response);
  }

  private void handleInvalidTopicName(
      ExternalMqttClient client,
      MqttSession session,
      PublishMqttInMessage publishMessage) {
    int messagedId = publishMessage.messageId();
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newDisconnect(client, DisconnectReasonCode.TOPIC_NAME_INVALID);
    // without messageId we do not need to clean it
    if (messagedId == MqttProperties.MESSAGE_ID_IS_NOT_SET) {
      client.closeWithReason(response);
      return;
    }
    client
        .closeWithReason(response)
        .thenAccept(_ -> session
            .inMessageTracker()
            .remove(messagedId));
  }
}
