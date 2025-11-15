package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.reason.code.PublishAckReasonCode;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.MqttSession;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.PublishDeliveringService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;

@CustomLog
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
  protected void handleNoMatchedSubscribers(
      ExternalMqttClient client,
      MqttSession session,
      Publish publish) {
    super.handleNoMatchedSubscribers(client, session, publish);
    int messageId = publish.messageId();
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newPublishAck(messageId, PublishAckReasonCode.NO_MATCHING_SUBSCRIBERS);
    sendFeedback(client, session, response, messageId);
  }

  @Override
  protected void handleError(
      ExternalMqttClient client,
      MqttSession session,
      Publish publish,
      PublishHandlingResult handlingResult) {
    super.handleError(client, session, publish, handlingResult);
    int messageId = publish.messageId();
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newPublishAck(publish.messageId(), handlingResult.ackReasonCode());
    sendFeedback(client, session, response, messageId);
  }

  @Override
  protected void handleSuccess(
      ExternalMqttClient client,
      MqttSession session,
      Publish publish,
      int matchedSubscribers) {
    super.handleSuccess(client, session, publish, matchedSubscribers);
    int messageId = publish.messageId();
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newPublishAck(messageId, PublishAckReasonCode.SUCCESS);
    sendFeedback(client, session, response, messageId);
  }
}
