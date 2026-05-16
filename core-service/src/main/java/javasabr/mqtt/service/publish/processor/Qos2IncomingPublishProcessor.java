package javasabr.mqtt.service.publish.processor;

import java.time.Duration;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode;
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.model.session.PublishRetryer;
import javasabr.mqtt.model.session.TrackableMessageCallback;
import javasabr.mqtt.model.session.TrackedMessageMeta;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.publish.IncomingPublishStorage;
import javasabr.mqtt.service.publish.PublishDispatcher;
import javasabr.mqtt.service.publish.RetainPublishService;
import javasabr.mqtt.service.publish.exception.NotScheduledForRemovalPublishStorageException;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Qos2IncomingPublishProcessor extends TrackableIncomingPublishProcessor<ExternalNetworkMqttUser> {

  /**
   * We keep publish data +5 mins from keeping message meta to avoid race conditions
   */
  private static final Duration NOT_CONFIRMED_REMOVAL_DELAY = META_EXPIRATION.plusMinutes(5);
  
  TrackableMessageCallback<IncomingPublish> trackableMessageCallback;

  public Qos2IncomingPublishProcessor(
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
    this.trackableMessageCallback = this::handleReceivedTrackableMessage;
  }

  @Override
  public QoS qos() {
    return QoS.EXACTLY_ONCE;
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
      // in the case if we already process the fist publish attempt, we should ack response
      if (publish.duplicated() && alreadyInProcess.messageType() == MqttMessageType.PUBLISH) {
        handleDuplicated(user, messagedId, alreadyInProcess, publish);
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
    var reasonCode = PublishReceivedReasonCode.SUCCESS;
    updateSessionState(user, session, publish, reasonCode);
    sendFeedback(
        user,
        messageOutFactoryService
            .resolveFactory(user)
            .newPublishReceived(publish.messageId(), reasonCode));
    incomingPublishStorage.scheduleRemoval(publish, NOT_CONFIRMED_REMOVAL_DELAY);
  }

  @Override
  protected void handleNoMatchedSubscribers(
      ExternalNetworkMqttUser user, 
      NetworkMqttSession session,
      IncomingPublish publish) {
    super.handleNoMatchedSubscribers(user, session, publish);
    sendFinalFeedback(user, session, publish, PublishCompletedReasonCode.SUCCESS);
  }

  @Override
  protected void handleDispatchedToSubscribers(
      ExternalNetworkMqttUser user,
      NetworkMqttSession session,
      IncomingPublish publish,
      int matchedSubscribers) {
    super.handleMatchedSubscribers(user, session, publish, matchedSubscribers);
    sendFinalFeedback(user, session, publish, PublishCompletedReasonCode.SUCCESS);
  }
  
  private void updateSessionState(
      ExternalNetworkMqttUser user,
      NetworkMqttSession session,
      IncomingPublish publish, 
      PublishReceivedReasonCode reasonCode) {
    int messageId = publish.messageId();
    log.debug(user.clientId(), messageId, publish.id(), 
        "[%s] Update tracking messageId:[%s] for publish:[%s] to PUBLISH"::formatted);
    // store response reason code for duplicated publishes
    MessageTacker messageTacker = session.inMessageTracker();
    messageTacker.update(messageId, MqttMessageType.PUBLISH, reasonCode);
    // store callback to handle publish release
    session
        .incomingProcessingPublishes()
        .register(publish, trackableMessageCallback, PublishRetryer.NO_OPS);
  }

  private void handleDuplicated(
      ExternalNetworkMqttUser user,
      int messageId, 
      TrackedMessageMeta alreadyInProcess,
      IncomingPublish publish) {
    PublishReceivedReasonCode reasonCode = PublishReceivedReasonCode.SUCCESS;
    if (alreadyInProcess.reasonCode() instanceof PublishReceivedReasonCode receivedReasonCode) {
      reasonCode = receivedReasonCode;
    }
    log.warning(user.clientId(), publish.id(), "[%s] Detected duplicated publish:[%s]"::formatted);
    sendFeedback(
        user,
        messageOutFactoryService
            .resolveFactory(user)
            .newPublishReceived(messageId, reasonCode));
  }

  private void handleMessageIdIsInUse(
      ExternalNetworkMqttUser user, 
      int messageId, 
      IncomingPublish publish) {
    log.warning(user.clientId(), messageId, publish.id(),
        "[%s] Detected conflicted messageId:[%s] from publish:[%s]"::formatted);
    sendFeedback(user, messageOutFactoryService
        .resolveFactory(user)
        .newPublishReceived(messageId, PublishReceivedReasonCode.PACKET_IDENTIFIER_IN_USE));
  }

  private boolean handleReceivedTrackableMessage(
      MqttUser user,
      MqttSession session, 
      TrackableMqttMessage message,
      IncomingPublish publish) {
    log.debug(user.clientId(), message.messageType(), message, 
        "[%s] Received trackable message:[%s] -> %s"::formatted);
    
    ExternalNetworkMqttUser networkMqttUser = expectedUserType.cast(user);
    String clientId = networkMqttUser.clientId();
    int messageId = message.messageId();
    MessageTacker messageTacker = session.inMessageTracker();
    TrackedMessageMeta messageMeta = messageTacker.stored(messageId);
    if (messageMeta == null) {
      log.warning(clientId, messageId, "[%s] No any stored information for messageId:[%d]"::formatted);
      incomingPublishStorage.cancelScheduledRemovalIfScheduled(publish);
      incomingPublishStorage.removeIfExist(publish);
      return true;
    } else if (messageMeta.messageType() != MqttMessageType.PUBLISH) {
      log.warning(clientId, messageMeta, messageId, 
          "[%s] Not expected tracked message meta:[%s] for messageId:[%d]"::formatted);
      incomingPublishStorage.cancelScheduledRemovalIfScheduled(publish);
      incomingPublishStorage.removeIfExist(publish);
      return true;
    } else if (message.messageType() != MqttMessageType.PUBLISH_RELEASE) {
      log.warning(clientId, message.messageType(), "[%s] Not expected message:[%s]"::formatted);
      incomingPublishStorage.cancelScheduledRemovalIfScheduled(publish);
      incomingPublishStorage.removeIfExist(publish);
      return true;
    }
    
    try {
      incomingPublishStorage.cancelScheduledRemoval(publish);
    } catch (NotScheduledForRemovalPublishStorageException e) {
      // we waited for the 'PUBLISH_RELEASE' too long and we already dropped the original publish
      log.warning(user.clientId(), messageId, publish.id(),
          "[%s] 'PUBLISH_RELEASE' message for publish:[%s] and messageId:[%s] came too late"::formatted);
      sendFinalFeedback(
          networkMqttUser, 
          session, 
          publish, 
          PublishCompletedReasonCode.PACKET_IDENTIFIER_NOT_FOUND);
      return true;
    }
    messageTacker.update(messageId, MqttMessageType.PUBLISH_COMPLETE, PublishCompletedReasonCode.SUCCESS);
    log.debug(user.clientId(), messageId, publish.id(),
        "[%s] Update tracking messageId:[%s] for publish:[%s] to PUBLISH_COMPLETE"::formatted);
    dispatchToSubscriber(networkMqttUser, (NetworkMqttSession) session, publish);
    return true;
  }
  
  private void sendFinalFeedback(
      ExternalNetworkMqttUser user,
      MqttSession session, 
      IncomingPublish publish,
      PublishCompletedReasonCode reasonCode) {
    sendFeedback(
        user,
        session,
        messageOutFactoryService
            .resolveFactory(user)
            .newPublishCompleted(publish.messageId(), reasonCode),
        publish.messageId());
  }
}
