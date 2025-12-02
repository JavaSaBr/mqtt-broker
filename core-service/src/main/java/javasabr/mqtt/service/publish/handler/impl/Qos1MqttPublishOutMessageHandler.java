package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.reason.code.PublishAckReasonCode;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.model.session.TrackedMessageMeta;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.in.PublishAckMqttInMessage;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import lombok.CustomLog;
import org.jspecify.annotations.Nullable;

@CustomLog
public class Qos1MqttPublishOutMessageHandler extends TrackableMqttPublishOutMessageHandler {

  public Qos1MqttPublishOutMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService) {
    super(subscriptionService, messageOutFactoryService);
  }

  @Override
  public QoS qos() {
    return QoS.AT_LEAST_ONCE;
  }

  @Override
  protected boolean handleReceivedTrackableMessageImpl(
      ExternalNetworkMqttUser user, 
      MqttSession session,
      TrackableMqttMessage message,
      @Nullable TrackedMessageMeta trackedMessageMeta) {
    
    int messageId = message.messageId();
    String clientId = user.clientId();
    if (trackedMessageMeta == null) {
      log.warning(clientId, messageId, "[%s] No any stored information for messageId:[%d]"::formatted);
      return true;
    } else if (trackedMessageMeta.messageType() != MqttMessageType.PUBLISH) {
      log.warning(clientId, trackedMessageMeta, messageId,
          "[%s] No expected message meta:[%s] for messageId:[%d]"::formatted);
      return true;
    }
    if (!(message instanceof PublishAckMqttInMessage publishAck)) {
      log.warning(clientId, message.messageType(), messageId, 
          "[%s] Not expected message type:[%s] for messageId:[%d]"::formatted);
      return true;
    }
    
    PublishAckReasonCode reasonCode = publishAck.reasonCode();
    if (reasonCode != PublishAckReasonCode.SUCCESS) {
      log.warning(clientId, reasonCode, messageId, "[%s] Received error response:[%s] for publish:[%s]"::formatted);
    }

    MessageTacker messageTacker = session.outMessageTracker();
    messageTacker.remove(messageId);
    
    log.debug(clientId, messageId, "[%s] Completed publish:[%s]"::formatted);
    return true;
  }
}
