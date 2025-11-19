package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.TrackableMessage;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode;
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.session.TrackedMessageMeta;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.in.PublishReleaseMqttInMessage;
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
public class Qos2MqttPublishInMessageHandler extends TrackableMqttPublishInMessageHandler<ExternalMqttClient> {

  public Qos2MqttPublishInMessageHandler(
      SubscriptionService subscriptionService,
      PublishDeliveringService publishDeliveringService,
      MessageOutFactoryService messageOutFactoryService) {
    super(ExternalMqttClient.class, subscriptionService, publishDeliveringService, messageOutFactoryService);
  }

  @Override
  public QoS qos() {
    return QoS.EXACTLY_ONCE;
  }

  @Override
  protected boolean validateImpl(ExternalMqttClient client, MqttSession session, Publish publish) {
    if (!super.validateImpl(client, session, publish)) {
      return false;
    }

    int messagedId = publish.messageId();
    MessageTacker messageTacker = session.inMessageTracker();
    TrackedMessageMeta alreadyInProcess = messageTacker.stored(messagedId);
    if (alreadyInProcess != null) {
      // in the case if we already process the fist publish attempt, we should ack response
      if (publish.duplicated() && (alreadyInProcess.messageType() == MqttMessageType.PUBLISH)) {
        handleDuplicated(client, messagedId, alreadyInProcess);
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
      MqttSession session,
      Publish publish) {
    super.handleNoMatchedSubscribers(client, session, publish);

    int messagedId = publish.messageId();
    var reasonCode = PublishReceivedReasonCode.NO_MATCHING_SUBSCRIBERS;

    MessageTacker messageTacker = session.inMessageTracker();
    messageTacker.update(messagedId, MqttMessageType.PUBLISH, reasonCode);

    sendFeedback(client, messageOutFactoryService
        .resolveFactory(client)
        .newPublishReceived(messagedId, reasonCode));
  }

  @Override
  protected void handleError(
      ExternalMqttClient client,
      MqttSession session,
      Publish publish,
      PublishHandlingResult handlingResult) {
    super.handleError(client, session, publish, handlingResult);

    int messagedId = publish.messageId();
    PublishReceivedReasonCode reasonCode = handlingResult.receivedReasonCode();

    MessageTacker messageTacker = session.inMessageTracker();
    messageTacker.update(messagedId, MqttMessageType.PUBLISH, reasonCode);

    sendFeedback(client, messageOutFactoryService
        .resolveFactory(client)
        .newPublishReceived(messagedId, reasonCode));
  }

  @Override
  protected void handleSuccess(
      ExternalMqttClient client,
      MqttSession session,
      Publish publish,
      int matchedSubscribers) {
    super.handleSuccess(client, session, publish, matchedSubscribers);

    int messagedId = publish.messageId();
    var reasonCode = PublishReceivedReasonCode.SUCCESS;

    MessageTacker messageTacker = session.inMessageTracker();
    messageTacker.update(messagedId, MqttMessageType.PUBLISH, reasonCode);

    sendFeedback(client, messageOutFactoryService
        .resolveFactory(client)
        .newPublishReceived(messagedId, PublishReceivedReasonCode.SUCCESS));
  }

  private boolean processPublishRelease(MqttClient client, TrackableMessage response) {
    if (!(response instanceof PublishReleaseMqttInMessage)) {
      throw new IllegalStateException("Unexpected response " + response);
    }
    MqttOutMessage response1 = messageOutFactoryService
        .resolveFactory(client)
        .newPublishCompleted(response.messageId(), PublishCompletedReasonCode.SUCCESS);
    //sendFeedback(client, client.session(), response1, response.messageId());
    return true;
  }

  private void handleDuplicated(
      ExternalMqttClient client,
      int messageId,
      TrackedMessageMeta alreadyInProcess) {
    PublishReceivedReasonCode reasonCode = PublishReceivedReasonCode.SUCCESS;
    if (alreadyInProcess.reasonCode() instanceof PublishReceivedReasonCode receivedReasonCode) {
      reasonCode = receivedReasonCode;
    }
    sendFeedback(client, messageOutFactoryService
        .resolveFactory(client)
        .newPublishReceived(messageId, reasonCode));
  }

  private void handleMessageIdIsInUse(ExternalMqttClient client, int messageId) {
    client.send(messageOutFactoryService
        .resolveFactory(client)
        .newPublishReceived(messageId, PublishReceivedReasonCode.PACKET_IDENTIFIER_IN_USE));
  }
}
