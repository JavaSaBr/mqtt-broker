package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.TrackableMessage;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode;
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.session.ProcessingPublishes;
import javasabr.mqtt.model.session.PublishRetryer;
import javasabr.mqtt.model.session.TrackableMessageCallback;
import javasabr.mqtt.model.session.TrackedMessageMeta;
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

  TrackableMessageCallback trackableMessageCallback;

  public Qos2MqttPublishInMessageHandler(
      SubscriptionService subscriptionService,
      PublishDeliveringService publishDeliveringService,
      MessageOutFactoryService messageOutFactoryService) {
    super(ExternalMqttClient.class, subscriptionService, publishDeliveringService, messageOutFactoryService);
    this.trackableMessageCallback = this::handleReceivedTrackableMessage;
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
    var reasonCode = PublishReceivedReasonCode.NO_MATCHING_SUBSCRIBERS;
    updateSessionState(session, publish, reasonCode);
    sendFeedback(client, messageOutFactoryService
        .resolveFactory(client)
        .newPublishReceived(publish.messageId(), reasonCode));
  }

  @Override
  protected void handleSuccess(
      ExternalMqttClient client,
      MqttSession session,
      Publish publish,
      int matchedSubscribers) {
    super.handleSuccess(client, session, publish, matchedSubscribers);
    var reasonCode = PublishReceivedReasonCode.SUCCESS;
    updateSessionState(session, publish, reasonCode);
    sendFeedback(client, messageOutFactoryService
        .resolveFactory(client)
        .newPublishReceived(publish.messageId(), PublishReceivedReasonCode.SUCCESS));
  }

  private void updateSessionState(MqttSession session, Publish publish, PublishReceivedReasonCode reasonCode) {
    // store response reason code for duplicated publishes
    MessageTacker messageTacker = session.inMessageTracker();
    messageTacker.update(publish.messageId(), MqttMessageType.PUBLISH, reasonCode);
    // store callback to handle publish release
    ProcessingPublishes processingPublishes = session.inProcessingPublishes();
    processingPublishes.register(publish, trackableMessageCallback, PublishRetryer.NO_OPS);
  }

  @Override
  protected void handleError(
      ExternalMqttClient client,
      MqttSession session,
      Publish publish,
      PublishHandlingResult handlingResult) {
    super.handleError(client, session, publish, handlingResult);

    int messageId = publish.messageId();
    PublishReceivedReasonCode reasonCode = handlingResult.receivedReasonCode();

    MessageTacker messageTacker = session.inMessageTracker();
    messageTacker.update(messageId, MqttMessageType.PUBLISH, reasonCode);

    sendFeedback(client, session, messageOutFactoryService
        .resolveFactory(client)
        .newPublishReceived(messageId, reasonCode), messageId);
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

  private boolean handleReceivedTrackableMessage(MqttUser user, Object object, TrackableMessage message) {
    ExternalMqttClient client = (ExternalMqttClient) user;
    MqttSession session = (MqttSession) object;
    int messageId = message.messageId();

    MessageTacker messageTacker = session.inMessageTracker();
    TrackedMessageMeta messageMeta = messageTacker.stored(messageId);
    if (messageMeta == null) {
      log.warning(client.clientId(), messageId, "[%s] No any stored information for messageId:[%d]"::formatted);
      return true;
    }

    if (messageMeta.messageType() != MqttMessageType.PUBLISH) {
      log.warning(client.clientId(), messageMeta, messageId,
          "[%s] Not expected tracked message meta:[%s] for messageId:[%d]"::formatted);
      return true;
    } else if (!(message instanceof PublishReleaseMqttInMessage release)) {
      log.warning(client.clientId(), message, "[%s] Not expected message:%s]"::formatted);
      return true;
    }

    messageTacker.update(
        messageId,
        MqttMessageType.PUBLISH_COMPLETE,
        PublishCompletedReasonCode.SUCCESS);

    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newPublishCompleted(message.messageId(), PublishCompletedReasonCode.SUCCESS);

    sendFeedback(client, session, response, messageId);
    return true;
  }
}
