package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.reason.code.PublishAckReasonCode;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.network.client.ExternalMqttClient;
import javasabr.mqtt.service.PublishDeliveringService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.message.handler.PublishHandlingResult;

public class Qos1PublishInMessageHandler extends Qos0PublishInMessageHandler {

  public Qos1PublishInMessageHandler(
      SubscriptionService subscriptionService,
      PublishDeliveringService publishDeliveringService) {
    super(subscriptionService, publishDeliveringService);
  }

  @Override
  public QoS qos() {
    return QoS.AT_LEAST_ONCE;
  }

  @Override
  protected void handleEmptySubscriptions(ExternalMqttClient client, int messageId, TopicName topicName) {
    super.handleEmptySubscriptions(client, messageId, topicName);
    client.send(client
        .packetOutFactory()
        .newPublishAck(messageId, PublishAckReasonCode.NO_MATCHING_SUBSCRIBERS));
  }

  @Override
  protected void handleInvalidTopic(ExternalMqttClient client, int messageId, TopicName topicName) {
    super.handleInvalidTopic(client, messageId, topicName);
    client.send(client
        .packetOutFactory()
        .newPublishAck(messageId, PublishAckReasonCode.TOPIC_NAME_INVALID));
  }

  @Override
  protected void handleError(ExternalMqttClient client, int messageId, PublishHandlingResult handlingResult) {
    super.handleError(client, messageId, handlingResult);
    client.send(client
        .packetOutFactory()
        .newPublishAck(messageId, handlingResult.ackReasonCode()));
  }
}
