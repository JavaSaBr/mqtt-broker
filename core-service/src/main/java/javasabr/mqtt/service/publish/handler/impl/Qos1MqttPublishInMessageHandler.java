package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.reason.code.PublishAckReasonCode;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.session.TrackedMessageMeta;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.PublishDeliveringService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Qos1MqttPublishInMessageHandler extends TrackableMqttPublishInMessageHandler<ExternalNetworkMqttUser> {

  public Qos1MqttPublishInMessageHandler(
      SubscriptionService subscriptionService,
      PublishDeliveringService publishDeliveringService,
      MessageOutFactoryService messageOutFactoryService) {
    super(ExternalNetworkMqttUser.class, subscriptionService, publishDeliveringService, messageOutFactoryService);
  }

  @Override
  public QoS qos() {
    return QoS.AT_LEAST_ONCE;
  }

  @Override
  protected boolean validateImpl(ExternalNetworkMqttUser user, NetworkMqttSession session, Publish publish) {
    if (!super.validateImpl(user, session, publish)) {
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
      handleMessageIdIsInUse(user, messagedId);
      return false;
    }
    return true;
  }

  @Override
  protected void handleNoMatchedSubscribers(
      ExternalNetworkMqttUser user,
      NetworkMqttSession session,
      Publish publish) {
    super.handleNoMatchedSubscribers(user, session, publish);
    int messageId = publish.messageId();
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(user)
        .newPublishAck(messageId, PublishAckReasonCode.NO_MATCHING_SUBSCRIBERS);
    sendFeedback(user, session, response, messageId);
  }

  @Override
  protected void handleSuccess(
      ExternalNetworkMqttUser user,
      NetworkMqttSession session,
      Publish publish,
      int matchedSubscribers) {
    super.handleSuccess(user, session, publish, matchedSubscribers);
    int messageId = publish.messageId();
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(user)
        .newPublishAck(messageId, PublishAckReasonCode.SUCCESS);
    sendFeedback(user, session, response, messageId);
  }

  @Override
  protected void handleError(
      ExternalNetworkMqttUser user,
      NetworkMqttSession session,
      Publish publish,
      PublishHandlingResult handlingResult) {
    super.handleError(user, session, publish, handlingResult);
    int messageId = publish.messageId();
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(user)
        .newPublishAck(publish.messageId(), handlingResult.ackReasonCode());
    sendFeedback(user, session, response, messageId);
  }

  private void handleMessageIdIsInUse(ExternalNetworkMqttUser user, int messageId) {
    user.sendInBackground(messageOutFactoryService
        .resolveFactory(user)
        .newPublishAck(messageId, PublishAckReasonCode.PACKET_IDENTIFIER_IN_USE));
  }
}
