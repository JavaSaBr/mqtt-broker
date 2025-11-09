package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.TrackableMessage;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode;
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.in.PublishReleaseMqttInMessage;
import javasabr.mqtt.network.session.MqttSession;
import javasabr.mqtt.network.session.MqttSession.PendingMessageHandler;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.PublishDeliveringService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Qos2MqttPublishInMessageHandler extends Qos0MqttPublishInMessageHandler {

  MessageOutFactoryService messageOutFactoryService;
  PendingMessageHandler pendingMessageHandler;

  public Qos2MqttPublishInMessageHandler(
      SubscriptionService subscriptionService,
      PublishDeliveringService publishDeliveringService,
      MessageOutFactoryService messageOutFactoryService) {
    super(subscriptionService, publishDeliveringService);
    this.messageOutFactoryService = messageOutFactoryService;
    this.pendingMessageHandler = this::processPublishRelease;
  }

  @Override
  public QoS qos() {
    return QoS.EXACTLY_ONCE;
  }

  @Override
  protected void handleImpl(ExternalMqttClient client, Publish publish) {
    MqttSession session = client.session();
    if (session == null) {
      return;
    }
    // if this packet is re-try from client
    if (publish.duplicated()) {
      // if this packet was accepted before then we can skip it
      if (session.hasInPending(publish.messageId())) {
        return;
      }
    }
    super.handleImpl(client, publish);
  }

  @Override
  protected void handleEmptySubscriptions(ExternalMqttClient client, Publish publish) {
    super.handleEmptySubscriptions(client, publish);
    client.send(messageOutFactoryService
        .resolveFactory(client)
        .newPublishReceived(publish.messageId(), PublishReceivedReasonCode.NO_MATCHING_SUBSCRIBERS));
  }

  @Override
  protected void handleError(ExternalMqttClient client, Publish publish, PublishHandlingResult handlingResult) {
    super.handleError(client, publish, handlingResult);
    client.send(messageOutFactoryService
        .resolveFactory(client)
        .newPublishReceived(publish.messageId(), handlingResult.receivedReasonCode()));
  }

  @Override
  protected void handleSuccessfulResult(ExternalMqttClient client, Publish publish, int subscribers) {
    super.handleSuccessfulResult(client, publish, subscribers);
    MqttSession session = client.session();
    if (session == null) {
      return;
    }
    session.registerInPublish(publish, pendingMessageHandler);
    client.send(messageOutFactoryService
        .resolveFactory(client)
        .newPublishReceived(publish.messageId(), PublishReceivedReasonCode.SUCCESS));
  }

  private boolean processPublishRelease(MqttClient client, TrackableMessage response) {
    if (!(response instanceof PublishReleaseMqttInMessage)) {
      throw new IllegalStateException("Unexpected response " + response);
    }
    client.send(messageOutFactoryService
        .resolveFactory(client)
        .newPublishCompleted(response.messageId(), PublishCompletedReasonCode.SUCCESS));
    return true;
  }
}
