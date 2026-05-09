package javasabr.mqtt.service.publish.processor;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.model.publish.Publish;
import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode;
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.model.session.ProcessingPublishes;
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
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Qos2IncomingPublishProcessor extends TrackableIncomingPublishProcessor<ExternalNetworkMqttUser> {

  TrackableMessageCallback trackableMessageCallback;

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
  protected void processImpl(ExternalNetworkMqttUser user, NetworkMqttSession session, IncomingPublish publish) {
    super.processImpl(user, session, publish);
    var reasonCode = PublishReceivedReasonCode.SUCCESS;
    updateSessionState(session, publish, reasonCode);
    sendFeedback(
        user,
        messageOutFactoryService
            .resolveFactory(user)
            .newPublishReceived(publish.messageId(), reasonCode));
  }

  @Override
  protected void handleNoMatchedSubscribers(
      ExternalNetworkMqttUser user, 
      NetworkMqttSession session,
      IncomingPublish publish) {
    super.handleNoMatchedSubscribers(user, session, publish);
    sendFeedback(
        user,
        session,
        messageOutFactoryService
            .resolveFactory(user)
            .newPublishCompleted(publish.messageId(), PublishCompletedReasonCode.SUCCESS),
        publish.messageId());
  }

  @Override
  protected void handleMatchedSubscribers(
      ExternalNetworkMqttUser user,
      NetworkMqttSession session,
      IncomingPublish publish,
      int matchedSubscribers) {
    super.handleMatchedSubscribers(user, session, publish, matchedSubscribers);
    sendFeedback(
        user,
        session,
        messageOutFactoryService
            .resolveFactory(user)
            .newPublishCompleted(publish.messageId(), PublishCompletedReasonCode.SUCCESS),
        publish.messageId());
  }
  
  private void updateSessionState(
      NetworkMqttSession session,
      IncomingPublish publish, 
      PublishReceivedReasonCode reasonCode) {
    // store response reason code for duplicated publishes
    MessageTacker messageTacker = session.inMessageTracker();
    messageTacker.update(publish.messageId(), MqttMessageType.PUBLISH, reasonCode);
    // store callback to handle publish release
    ProcessingPublishes processingPublishes = session.inProcessingPublishes();
    processingPublishes.register(publish, trackableMessageCallback, PublishRetryer.NO_OPS);
  }

  private void handleDuplicated(
      ExternalNetworkMqttUser user,
      int messageId, 
      TrackedMessageMeta alreadyInProcess,
      IncomingPublish incomingPublish) {
    PublishReceivedReasonCode reasonCode = PublishReceivedReasonCode.SUCCESS;
    if (alreadyInProcess.reasonCode() instanceof PublishReceivedReasonCode receivedReasonCode) {
      reasonCode = receivedReasonCode;
    }
    sendFeedback(
        user,
        messageOutFactoryService
            .resolveFactory(user)
            .newPublishReceived(messageId, reasonCode));
    // FIXME need to add check if the prev message was fully drop and not delivered
    // no any sense to keep duplicated message on our side
    incomingPublishStorage.removeIfExist(incomingPublish);
  }

  private void handleMessageIdIsInUse(
      ExternalNetworkMqttUser user, 
      int messageId, 
      IncomingPublish publish) {
    incomingPublishStorage.removeIfExist(publish);
    user.sendInBackground(messageOutFactoryService
        .resolveFactory(user)
        .newPublishReceived(messageId, PublishReceivedReasonCode.PACKET_IDENTIFIER_IN_USE));
  }

  private boolean handleReceivedTrackableMessage(
      MqttUser user,
      MqttSession session, 
      TrackableMqttMessage message,
      Publish publish) {
    ExternalNetworkMqttUser networkMqttUser = expectedUserType.cast(user);
    String clientId = networkMqttUser.clientId();
    int messageId = message.messageId();

    if (!(publish instanceof IncomingPublish incomingPublish)) {
      log.warning(clientId, publish.getClass(), messageId, 
          "[%s] Not expected publish type:[%s] for messageId:[%d]"::formatted);
      return true;
    }

    MessageTacker messageTacker = session.inMessageTracker();
    TrackedMessageMeta messageMeta = messageTacker.stored(messageId);
    if (messageMeta == null) {
      log.warning(clientId, messageId, "[%s] No any stored information for messageId:[%d]"::formatted);
      incomingPublishStorage.remove(incomingPublish);
      return true;
    }

    if (messageMeta.messageType() != MqttMessageType.PUBLISH) {
      log.warning(clientId, messageMeta, messageId, 
          "[%s] Not expected tracked message meta:[%s] for messageId:[%d]"::formatted);
      incomingPublishStorage.remove(incomingPublish);
      return true;
    } else if (message.messageType() != MqttMessageType.PUBLISH_RELEASE) {
      log.warning(clientId, message.messageType(), "[%s] Not expected message:[%s]"::formatted);
      incomingPublishStorage.remove(incomingPublish);
      return true;
    }
    
    //FIXME No cleanup path when a QoS 2 session closes before PUBREL

    messageTacker.update(messageId, MqttMessageType.PUBLISH_COMPLETE, PublishCompletedReasonCode.SUCCESS);
    dispatchToSubscriber(networkMqttUser, (NetworkMqttSession) session, incomingPublish);
    return true;
  }
}
