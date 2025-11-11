package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.model.PayloadFormat;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.reason.code.PublishAckReasonCode;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.model.topic.TopicValidator;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.message.in.PublishMqttInMessage;
import javasabr.mqtt.network.session.MqttSession;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.PublishReceivingService;
import javasabr.mqtt.service.TopicService;
import javasabr.rlib.collections.array.IntArray;
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
  protected void processValidMessage(
      MqttConnection connection,
      ExternalMqttClient client,
      MqttSession session,
      PublishMqttInMessage message) {

    int messageId = message.messageId();
    if (messageId > 0 && session.hasInPending(messageId)) {
      client.send(messageOutFactoryService
          .resolveFactory(client)
          .newPublishAck(messageId, PublishAckReasonCode.PACKET_IDENTIFIER_IN_USE));
      log.warning(client.clientId(), messageId, "[%s] Client provided already in use messageId:[%s]..."::formatted);
      return;
    }

    String rawTopicName = message.rawTopicName();

    if (!TopicValidator.validateTopicName(rawTopicName)) {
      client.send(messageOutFactoryService
          .resolveFactory(client)
          .newPublishAck(messageId, PublishAckReasonCode.TOPIC_NAME_INVALID));
      log.warning(client.clientId(), rawTopicName, "[%s] Client provided invalid topic name:[%s]..."::formatted);
      return;
    }

    TopicName topicName = topicService.createTopicName(client, rawTopicName);

    // TODO
    byte[] payload = message.payload();
    int topicAlias = message.topicAlias();
    IntArray subscriptionIds = message.subscriptionIds();
    String rawResponseTopicName = message.rawResponseTopicName();
    PayloadFormat payloadFormat = message.payloadFormat();

    publishReceivingService.processReceivedPublish(client, new Publish(
        messageId,
        message.qos(),
        topicName,
        null,
        payload,
        message.duplicate(),
        message.retained(),
        message.contentType(),
        message.subscriptionIds(),
        message.correlationData(),
        message.messageExpiryInterval(),
        topicAlias,
        payloadFormat,
        message.userProperties()));
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH;
  }
}
