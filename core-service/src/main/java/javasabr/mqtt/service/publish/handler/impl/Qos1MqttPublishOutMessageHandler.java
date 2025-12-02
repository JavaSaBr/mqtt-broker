package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.model.session.TrackedMessageMeta;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.in.PublishAckMqttInMessage;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import lombok.CustomLog;

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
      TrackableMqttMessage message) {
    int messageId = message.messageId();
    MessageTacker messageTacker = session.inMessageTracker();
    TrackedMessageMeta messageMeta = messageTacker.stored(messageId);
    if (messageMeta == null) {
      log.warning(user.clientId(), messageId, "[%s] No any stored information for messageId:[%d]"::formatted);
      return true;
    }
    if (messageMeta.messageType() != MqttMessageType.PUBLISH) {
      log.warning(user.clientId(), messageMeta, messageId,
          "[%s] Not expected tracked message meta:[%s] for messageId:[%d]"::formatted);
      return true;
    } else if (!(message instanceof PublishAckMqttInMessage publishAck)) {
      log.warning(user.clientId(), message, "[%s] Not expected message:%s]"::formatted);
      return true;
    }
    messageTacker.remove(messageId);
    return true;
  }

  @Override
  protected void retryDeliveringImpl(ExternalNetworkMqttUser user, MqttSession session, Publish publish) {
    
    
  }
}
