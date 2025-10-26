package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.reason.code.PublishAckReasonCode;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.in.PublishMqttInMessage;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.PublishDeliveringService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Qos1MqttPublishInMessageHandler extends Qos0MqttPublishInMessageHandler {

  MessageOutFactoryService messageOutFactoryService;

  public Qos1MqttPublishInMessageHandler(
      SubscriptionService subscriptionService,
      PublishDeliveringService publishDeliveringService,
      MessageOutFactoryService messageOutFactoryService) {
    super(subscriptionService, publishDeliveringService);
    this.messageOutFactoryService = messageOutFactoryService;
  }

  @Override
  public QoS qos() {
    return QoS.AT_LEAST_ONCE;
  }

  @Override
  protected void handleEmptySubscriptions(ExternalMqttClient client, int messageId, TopicName topicName) {
    super.handleEmptySubscriptions(client, messageId, topicName);
    client.send(messageOutFactoryService
        .resolveFactory(client)
        .newPublishAck(messageId, PublishAckReasonCode.NO_MATCHING_SUBSCRIBERS));
  }

  @Override
  protected void handleInvalidTopic(ExternalMqttClient client, int messageId, TopicName topicName) {
    super.handleInvalidTopic(client, messageId, topicName);
    client.send(messageOutFactoryService
        .resolveFactory(client)
        .newPublishAck(messageId, PublishAckReasonCode.TOPIC_NAME_INVALID));
  }

  @Override
  protected void handleError(ExternalMqttClient client, int messageId, PublishHandlingResult handlingResult) {
    super.handleError(client, messageId, handlingResult);
    client.send(messageOutFactoryService
        .resolveFactory(client)
        .newPublishAck(messageId, handlingResult.ackReasonCode()));
  }

  @Override
  protected void handleSuccessfulResult(ExternalMqttClient client, PublishMqttInMessage packet, int subscribers) {
    super.handleSuccessfulResult(client, packet, subscribers);
    client.send(messageOutFactoryService
        .resolveFactory(client)
        .newPublishAck(packet.messageId(), PublishAckReasonCode.SUCCESS));
  }
}
