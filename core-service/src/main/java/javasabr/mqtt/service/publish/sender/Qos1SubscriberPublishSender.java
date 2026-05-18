package javasabr.mqtt.service.publish.sender;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.model.publish.OutgoingPublish;
import javasabr.mqtt.model.publish.Publish;
import javasabr.mqtt.model.reason.code.PublishAckReasonCode;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.model.session.TrackedMessageMeta;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.in.PublishAckMqttInMessage;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.publish.IncomingPublishStorage;
import lombok.CustomLog;
import org.jspecify.annotations.Nullable;

@CustomLog
public class Qos1SubscriberPublishSender extends TrackableSubscriberPublishSender {

  public Qos1SubscriberPublishSender(
      MessageOutFactoryService messageOutFactoryService,
      IncomingPublishStorage incomingPublishStorage) {
    super(messageOutFactoryService, incomingPublishStorage);
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
      @Nullable TrackedMessageMeta trackedMessageMeta,
      Publish publish) {
    if (!(publish instanceof OutgoingPublish outgoingPublish)) {
      throw new IllegalArgumentException("Unexpected publish type:[%s]".formatted(publish));
    }
    int messageId = message.messageId();
    String clientId = user.clientId();
    if (trackedMessageMeta == null) {
      log.warn(clientId, messageId, "[%s] No any stored information for messageId:[%d]"::formatted);
      incomingPublishStorage.decreaseConsumerCount(outgoingPublish.source(), 1);
      return true;
    }
    
    MqttMessageType trackedMessageType = trackedMessageMeta.messageType();
    if (trackedMessageType != MqttMessageType.PUBLISH) {
      log.warn(clientId, trackedMessageMeta, messageId,
          "[%s] No expected message meta:[%s] for messageId:[%d]"::formatted);
      handleNotExpectedFlowState(user, trackedMessageType, MqttMessageType.PUBLISH, outgoingPublish);
      return true;
    }
    
    if (!(message instanceof PublishAckMqttInMessage publishAck)) {
      log.warn(clientId, message.messageType(), messageId, 
          "[%s] Not expected message type:%s for messageId:[%d]"::formatted);
      handleNotExpectedResponseMessage(user, message, MqttMessageType.PUBLISH_ACK, outgoingPublish);
      return true;
    }
    
    PublishAckReasonCode reasonCode = publishAck.reasonCode();
    if (reasonCode != PublishAckReasonCode.SUCCESS) {
      // just to note in logs, we can't do anything with this
      log.warn(clientId, reasonCode, messageId, "[%s] Received error response:[%s] for publish:[%s]"::formatted);
    }
    
    MessageTacker messageTacker = session.outMessageTracker();
    messageTacker.removeIfExist(messageId);
    
    log.debug(clientId, messageId, "[%s] Completed publish:[%s]"::formatted);
    incomingPublishStorage.decreaseConsumerCount(outgoingPublish.source(), 1);
    return true;
  }
}
