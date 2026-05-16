package javasabr.mqtt.service.publish.processor;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.model.reason.code.PublishAckReasonCode;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.session.TrackedMessageMeta;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.publish.IncomingPublishStorage;
import javasabr.mqtt.service.publish.PublishDispatcher;
import javasabr.mqtt.service.publish.RetainPublishService;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Qos1IncomingPublishProcessor extends TrackableIncomingPublishProcessor<ExternalNetworkMqttUser> {

  public Qos1IncomingPublishProcessor(
      SubscriptionService subscriptionService,
      PublishDispatcher publishDispatcher,
      MessageOutFactoryService messageOutFactoryService,
      RetainPublishService retainPublishService,
      IncomingPublishStorage incomingPublishStorage) {
    super(
        ExternalNetworkMqttUser.class,
        subscriptionService, 
        publishDispatcher,
        messageOutFactoryService,
        retainPublishService,
        incomingPublishStorage);
  }

  @Override
  public QoS qos() {
    return QoS.AT_LEAST_ONCE;
  }

  @Override
  protected boolean validateImpl(
      ExternalNetworkMqttUser user, 
      NetworkMqttSession session, 
      IncomingPublish publish) {
    if (!super.validateImpl(user, session, publish)) {
      return false;
    }
    int messagedId = publish.messageId();
    MessageTacker messageTacker = session.inMessageTracker();
    TrackedMessageMeta alreadyInProcess = messageTacker.stored(messagedId);
    if (alreadyInProcess != null) {
      // in the case if we already process the fist publish attempt, we can skip it
      if (publish.duplicated() && alreadyInProcess.messageType() == MqttMessageType.PUBLISH) {
        log.warning(user.clientId(), publish.id(), "[%s] Detected duplicated publish:[%s]"::formatted);
        return false;
      }
      handleMessageIdIsInUse(user, messagedId, publish);
      return false;
    }
    return true;
  }

  @Override
  protected void processImpl(
      ExternalNetworkMqttUser user, 
      NetworkMqttSession session, 
      IncomingPublish publish) {
    super.processImpl(user, session, publish);
    dispatchToSubscriber(user, session, publish);
  }

  @Override
  protected void handleNoMatchedSubscribers(
      ExternalNetworkMqttUser user,
      NetworkMqttSession session, 
      IncomingPublish publish) {
    super.handleNoMatchedSubscribers(user, session, publish);
    int messageId = publish.messageId();
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(user)
        .newPublishAck(messageId, PublishAckReasonCode.NO_MATCHING_SUBSCRIBERS);
    sendFeedback(user, session, response, messageId);
  }

  @Override
  protected void handleDispatchedToSubscribers(
      ExternalNetworkMqttUser user,
      NetworkMqttSession session,
      IncomingPublish publish,
      int matchedSubscribers) {
    super.handleDispatchedToSubscribers(user, session, publish, matchedSubscribers);
    int messageId = publish.messageId();
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(user)
        .newPublishAck(messageId, PublishAckReasonCode.SUCCESS);
    sendFeedback(user, session, response, messageId);
  }
  
  private void handleMessageIdIsInUse(
      ExternalNetworkMqttUser user,
      int messageId, 
      IncomingPublish publish) {
    log.warning(user.clientId(), messageId, publish.id(),
        "[%s] Detected conflicted messageId:[%s] from publish:[%s]"::formatted);
    sendFeedback(user, messageOutFactoryService
        .resolveFactory(user)
        .newPublishAck(messageId, PublishAckReasonCode.PACKET_IDENTIFIER_IN_USE));
  }
}
