package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode;
import javasabr.mqtt.model.reason.code.PublishReleaseReasonCode;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.model.session.TrackedMessageMeta;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.in.PublishCompleteMqttInMessage;
import javasabr.mqtt.network.message.in.PublishReceivedMqttInMessage;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import lombok.CustomLog;
import org.jspecify.annotations.Nullable;

@CustomLog
public class Qos2MqttPublishOutMessageHandler extends TrackableMqttPublishOutMessageHandler {

  public Qos2MqttPublishOutMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService) {
    super(subscriptionService, messageOutFactoryService);
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

    int messageId = message.messageId();
    MessageTacker messageTacker = session.outMessageTracker();

    if (message instanceof PublishReceivedMqttInMessage publishReceived) {

      PublishReceivedReasonCode reasonCode = publishReceived.reasonCode();
      if (reasonCode != PublishReceivedReasonCode.SUCCESS) {
        log.warning(user.clientId(), reasonCode, messageId,
            "[%s] Received error response:[%s] for publish:[%s]"::formatted);
        // we can cancel the flow
        messageTacker.remove(messageId);
        return true;
      }
      messageTacker.update(messageId, MqttMessageType.PUBLISH_RECEIVED, reasonCode);
      user.sendInBackground(messageOutFactoryService
          .resolveFactory(user)
          .newPublishRelease(messageId, PublishReleaseReasonCode.SUCCESS));
      return false;
    }
    
    return false;
  }

  @Override
  protected void retryDeliveringImpl(ExternalNetworkMqttUser user, MqttSession session, Publish publish) {
    
  }

  @Override
  protected boolean handleReceivedResponse(NetworkMqttUser user, TrackableMqttMessage response) {
    if (response instanceof PublishReceivedMqttInMessage) {
      user.sendInBackground(messageOutFactoryService
          .resolveFactory(user)
          .newPublishRelease(response.messageId(), SUCCESS));
      return false;
    } else if (response instanceof PublishCompleteMqttInMessage) {
      return true;
    } else {
      throw new IllegalStateException("Unexpected response: " + response);
    }
  }
}
