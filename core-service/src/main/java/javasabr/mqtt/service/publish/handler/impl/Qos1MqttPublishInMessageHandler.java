package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.reason.code.PublishAckReasonCode;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.session.TrackedMessageMeta;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.MqttNetworkSession;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.PublishDeliveringService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Qos1MqttPublishInMessageHandler extends TrackableMqttPublishInMessageHandler<ExternalMqttClient> {

  public Qos1MqttPublishInMessageHandler(
      SubscriptionService subscriptionService,
      PublishDeliveringService publishDeliveringService,
      MessageOutFactoryService messageOutFactoryService) {
    super(ExternalMqttClient.class, subscriptionService, publishDeliveringService, messageOutFactoryService);
  }

  @Override
  public QoS qos() {
    return QoS.AT_LEAST_ONCE;
  }

  @Override
  protected boolean validateImpl(ExternalMqttClient client, MqttNetworkSession session, Publish publish) {
    if (!super.validateImpl(client, session, publish)) {
      return false;
    }
    int messagedId = publish.messageId();
    MessageTacker messageTacker = session.inMessageTracker();
    TrackedMessageMeta alreadyInProcess = messageTacker.stored(messagedId);
    if (alreadyInProcess != null) {
      // in the case if we already process the fist publish attempt, we can skip it
      if (publish.duplicated() && alreadyInProcess.messageType() == MqttMessageType.PUBLISH) {
        return false;
      }
      handleMessageIdIsInUse(client, messagedId);
      return false;
    }
    return true;
  }

  @Override
  protected void handleNoMatchedSubscribers(
      ExternalMqttClient client,
      MqttNetworkSession session,
      Publish publish) {
    super.handleNoMatchedSubscribers(client, session, publish);
    int messageId = publish.messageId();
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newPublishAck(messageId, PublishAckReasonCode.NO_MATCHING_SUBSCRIBERS);
    sendFeedback(client, session, response, messageId);
  }

  @Override
  protected void handleSuccess(
      ExternalMqttClient client,
      MqttNetworkSession session,
      Publish publish,
      int matchedSubscribers) {
    super.handleSuccess(client, session, publish, matchedSubscribers);
    int messageId = publish.messageId();
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newPublishAck(messageId, PublishAckReasonCode.SUCCESS);
    sendFeedback(client, session, response, messageId);
  }

  @Override
  protected void handleError(
      ExternalMqttClient client,
      MqttNetworkSession session,
      Publish publish,
      PublishHandlingResult handlingResult) {
    super.handleError(client, session, publish, handlingResult);
    int messageId = publish.messageId();
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newPublishAck(publish.messageId(), handlingResult.ackReasonCode());
    sendFeedback(client, session, response, messageId);
  }

  private void handleMessageIdIsInUse(ExternalMqttClient client, int messageId) {
    client.send(messageOutFactoryService
        .resolveFactory(client)
        .newPublishAck(messageId, PublishAckReasonCode.PACKET_IDENTIFIER_IN_USE));
  }
}
