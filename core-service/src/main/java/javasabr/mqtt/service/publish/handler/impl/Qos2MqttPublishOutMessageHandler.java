package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode;
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode;
import javasabr.mqtt.model.reason.code.PublishReleaseReasonCode;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.model.session.TrackedMessageMeta;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.in.PublishCompleteMqttInMessage;
import javasabr.mqtt.network.message.in.PublishReceivedMqttInMessage;
import javasabr.mqtt.service.MessageOutFactoryService;
import lombok.CustomLog;
import org.jspecify.annotations.Nullable;

@CustomLog
public class Qos2MqttPublishOutMessageHandler extends TrackableMqttPublishOutMessageHandler {

  public Qos2MqttPublishOutMessageHandler(MessageOutFactoryService messageOutFactoryService) {
    super(messageOutFactoryService);
  }

  @Override
  public QoS qos() {
    return QoS.EXACTLY_ONCE;
  }

  @Override
  protected boolean handleReceivedTrackableMessageImpl(
      ExternalNetworkMqttUser user,
      MqttSession session,
      TrackableMqttMessage message,
      @Nullable TrackedMessageMeta trackedMessageMeta) {
    if (message instanceof PublishReceivedMqttInMessage publishReceived) {
      return handlePublishRelease(user, session, message, trackedMessageMeta, publishReceived);
    } else if (message instanceof PublishCompleteMqttInMessage publishComplete) {
      handlePublishComplete(user, session, message, trackedMessageMeta, publishComplete);
      return true;
    } else {
      log.warning(user.clientId(), message.messageType(), message.messageId(),
          "[%s] Not expected message type:[%s] for messageId:[%d]"::formatted);
      return true;
    }
  }

  private boolean handlePublishRelease(
      ExternalNetworkMqttUser user,
      MqttSession session,
      TrackableMqttMessage message,
      @Nullable TrackedMessageMeta trackedMessageMeta,
      PublishReceivedMqttInMessage publishReceived) {

    int messageId = message.messageId();
    String clientId = user.clientId();
    if (trackedMessageMeta != null && trackedMessageMeta.messageType() != MqttMessageType.PUBLISH) {
      log.warning(clientId, trackedMessageMeta, messageId,
          "[%s] No expected message meta:[%s] for messageId:[%d]"::formatted);
      return true;
    }

    MessageTacker messageTacker = session.outMessageTracker();
    PublishReceivedReasonCode reasonCode = publishReceived.reasonCode();
    if (reasonCode != PublishReceivedReasonCode.SUCCESS) {
      log.warning(clientId, reasonCode, messageId,
          "[%s] Received error response:[%s] for publish:[%s]"::formatted);
      // we can cancel the flow
      if (trackedMessageMeta != null) {
        messageTacker.remove(messageId);
      }
      return true;
    }

    PublishReleaseReasonCode releaseResult;
    // we unknown this flow
    if (trackedMessageMeta == null) {
      releaseResult = PublishReleaseReasonCode.PACKET_IDENTIFIER_NOT_FOUND;
    } else {
      releaseResult = PublishReleaseReasonCode.SUCCESS;
      messageTacker.update(messageId, MqttMessageType.PUBLISH_RELEASE, reasonCode);
    }

    user.sendInBackground(messageOutFactoryService
        .resolveFactory(user)
        .newPublishRelease(messageId, releaseResult));

    // cancel this flow if it's not success
    return releaseResult != PublishReleaseReasonCode.SUCCESS;
  }

  private void handlePublishComplete(
      ExternalNetworkMqttUser user,
      MqttSession session,
      TrackableMqttMessage message,
      @Nullable TrackedMessageMeta trackedMessageMeta,
      PublishCompleteMqttInMessage publishComplete) {

    int messageId = message.messageId();
    String clientId = user.clientId();
    if (trackedMessageMeta == null) {
      log.warning(clientId, messageId, "[%s] No any stored information for messageId:[%d]"::formatted);
      return;
    } else if (trackedMessageMeta.messageType() != MqttMessageType.PUBLISH_RELEASE) {
      log.warning(clientId, trackedMessageMeta, messageId,
          "[%s] No expected message meta:[%s] for messageId:[%d]"::formatted);
      return;
    }

    PublishCompletedReasonCode reasonCode = publishComplete.reasonCode();
    if (reasonCode != PublishCompletedReasonCode.SUCCESS) {
      log.warning(clientId, reasonCode, messageId,
          "[%s] Received error response:[%s] for publish:[%s]"::formatted);
    }
    
    // finish the flow
    MessageTacker messageTacker = session.outMessageTracker();
    messageTacker.remove(messageId);
  }
}
