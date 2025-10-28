package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.reason.code.PublishAckReasonCode;
import javasabr.mqtt.network.impl.ExternalMqttClient;
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
  protected void handleEmptySubscriptions(ExternalMqttClient client, Publish publish) {
    super.handleEmptySubscriptions(client, publish);
    client.send(messageOutFactoryService
        .resolveFactory(client)
        .newPublishAck(publish.messageId(), PublishAckReasonCode.NO_MATCHING_SUBSCRIBERS));
  }

  @Override
  protected void handleError(ExternalMqttClient client, Publish publish, PublishHandlingResult handlingResult) {
    super.handleError(client, publish, handlingResult);
    client.send(messageOutFactoryService
        .resolveFactory(client)
        .newPublishAck(publish.messageId(), handlingResult.ackReasonCode()));
  }

  @Override
  protected void handleSuccessfulResult(ExternalMqttClient client, Publish publish, int subscribers) {
    super.handleSuccessfulResult(client, publish, subscribers);
    client.send(messageOutFactoryService
        .resolveFactory(client)
        .newPublishAck(publish.messageId(), PublishAckReasonCode.SUCCESS));
  }
}
