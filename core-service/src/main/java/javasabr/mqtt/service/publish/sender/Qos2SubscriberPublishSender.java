package javasabr.mqtt.service.publish.sender;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.model.publish.Publish;
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
public class Qos2SubscriberPublishSender extends TrackableSubscriberPublishSender {

  public Qos2SubscriberPublishSender(MessageOutFactoryService messageOutFactoryService) {
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
      @Nullable TrackedMessageMeta trackedMessageMeta,
      Publish publish) {
    if (message instanceof PublishReceivedMqttInMessage publishReceived) {
      return handlePublishReceive(user, session, message, trackedMessageMeta, publishReceived);
    } else if (message instanceof PublishCompleteMqttInMessage publishComplete) {
      handlePublishComplete(user, session, message, trackedMessageMeta, publishComplete);
      return true;
    } else {
      log.warning(user.clientId(), message.messageType(), message.messageId(),
          "[%s] Not expected message type:[%s] for messageId:[%d]"::formatted);
      handleNotExpectedResponseMessage(user, message, calculateExpectedMessageType(trackedMessageMeta));
      return true;
    }
  }

  /**
   * @return true if need to cancel the flow
   */
  private boolean handlePublishReceive(
      ExternalNetworkMqttUser user,
      MqttSession session,
      TrackableMqttMessage message,
      @Nullable TrackedMessageMeta trackedMessageMeta,
      PublishReceivedMqttInMessage publishReceived) {

    int messageId = message.messageId();
    String clientId = user.clientId();
    PublishReceivedReasonCode reasonCode = publishReceived.reasonCode();

    // if we unknown this flow
    if (trackedMessageMeta == null) {
      log.warning(clientId, messageId, "[%s] No any stored information for messageId:[%d]"::formatted);
      // for success reason code we should answer that we don't know what this flow
      if (reasonCode == PublishReceivedReasonCode.SUCCESS) {
        user.sendInBackground(messageOutFactoryService
            .resolveFactory(user)
            .newPublishRelease(messageId, PublishReleaseReasonCode.PACKET_IDENTIFIER_NOT_FOUND));
      }
      return true;
    }
    
    MqttMessageType trackedMessageType = trackedMessageMeta.messageType();
    if (trackedMessageType != MqttMessageType.PUBLISH) {
      log.warning(clientId, trackedMessageMeta, messageId,
          "[%s] No expected message meta:[%s] for messageId:[%d]"::formatted);
      handleNotExpectedFlowState(user, trackedMessageType, MqttMessageType.PUBLISH);
      return true;
    }

    MessageTacker messageTacker = session.outMessageTracker();
    if (reasonCode != PublishReceivedReasonCode.SUCCESS) {
      log.warning(clientId, reasonCode, messageId,
          "[%s] Received error response:[%s] for publish:[%s]"::formatted);
      // we can cancel the flow
      messageTacker.remove(messageId);
      return true;
    }

    // switch flow from publish to release phase
    messageTacker.update(messageId, MqttMessageType.PUBLISH_RELEASE, reasonCode);
    
    // completed this phase
    user.sendInBackground(messageOutFactoryService
        .resolveFactory(user)
        .newPublishRelease(messageId, PublishReleaseReasonCode.SUCCESS));
    return false;
  }

  private void handlePublishComplete(
      ExternalNetworkMqttUser user,
      MqttSession session,
      TrackableMqttMessage message,
      @Nullable TrackedMessageMeta trackedMessageMeta,
      PublishCompleteMqttInMessage publishComplete) {

    int messageId = message.messageId();
    String clientId = user.clientId();

    // if we unknown this flow
    if (trackedMessageMeta == null) {
      log.warning(clientId, messageId, "[%s] No any stored information for messageId:[%d]"::formatted);
      return;
    }

    MqttMessageType trackedMessageType = trackedMessageMeta.messageType();
    if (trackedMessageType != MqttMessageType.PUBLISH_RELEASE) {
      log.warning(clientId, trackedMessageMeta, messageId,
          "[%s] No expected message meta:[%s] for messageId:[%d]"::formatted);
      handleNotExpectedFlowState(user, trackedMessageType, MqttMessageType.PUBLISH_RELEASE);
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
  
  private static MqttMessageType calculateExpectedMessageType(@Nullable TrackedMessageMeta trackedMessageMeta) {
    if (trackedMessageMeta != null && trackedMessageMeta.messageType() == MqttMessageType.PUBLISH_RELEASE) {
      return MqttMessageType.PUBLISH_COMPLETE;
    }
    // by default, we expect 'PUBLISH_RECEIVED'
    return MqttMessageType.PUBLISH_RECEIVED;
  }
}
