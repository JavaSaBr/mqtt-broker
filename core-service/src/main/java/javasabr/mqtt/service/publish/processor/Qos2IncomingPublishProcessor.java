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
import javasabr.mqtt.network.message.in.PublishReleaseMqttInMessage;
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
      //FIXME need to be sure that the new duplicated publish instance is referenced to original
      if (publish.duplicated() && (alreadyInProcess.messageType() == MqttMessageType.PUBLISH)) {
        handleDuplicated(user, messagedId, alreadyInProcess);
        return false;
      }
      handleMessageIdIsInUse(user, messagedId, publish);
      return false;
    }

    return true;
  }

  @Override
  protected void handleNoMatchedSubscribers(
      ExternalNetworkMqttUser user, 
      NetworkMqttSession session,
      IncomingPublish publish) {
    super.handleNoMatchedSubscribers(user, session, publish);
    var reasonCode = PublishReceivedReasonCode.NO_MATCHING_SUBSCRIBERS;
    updateSessionState(session, publish, reasonCode);
    sendFeedback(
        user,
        messageOutFactoryService
            .resolveFactory(user)
            .newPublishReceived(publish.messageId(), reasonCode));
  }

  @Override
  protected void handleSuccess(
      ExternalNetworkMqttUser user,
      NetworkMqttSession session,
      IncomingPublish publish,
      int matchedSubscribers) {
    super.handleSuccess(user, session, publish, matchedSubscribers);
    var reasonCode = PublishReceivedReasonCode.SUCCESS;
    updateSessionState(session, publish, reasonCode);
    sendFeedback(
        user,
        messageOutFactoryService
            .resolveFactory(user)
            .newPublishReceived(publish.messageId(), PublishReceivedReasonCode.SUCCESS));
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

  @Override
  protected void handleError(
      ExternalNetworkMqttUser user,
      NetworkMqttSession session,
      IncomingPublish publish,
      PublishProcessingResult handlingResult) {
    super.handleError(user, session, publish, handlingResult);

    int messageId = publish.messageId();
    PublishReceivedReasonCode reasonCode = handlingResult.receivedReasonCode();

    MessageTacker messageTacker = session.inMessageTracker();
    messageTacker.update(messageId, MqttMessageType.PUBLISH, reasonCode);

    sendFeedback(
        user,
        session,
        messageOutFactoryService
            .resolveFactory(user)
            .newPublishReceived(messageId, reasonCode),
        messageId);
  }

  private void handleDuplicated(ExternalNetworkMqttUser user, int messageId, TrackedMessageMeta alreadyInProcess) {
    PublishReceivedReasonCode reasonCode = PublishReceivedReasonCode.SUCCESS;
    if (alreadyInProcess.reasonCode() instanceof PublishReceivedReasonCode receivedReasonCode) {
      reasonCode = receivedReasonCode;
    }
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
    incomingPublishStorage.remove(publish);
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

    MessageTacker messageTacker = session.inMessageTracker();
    TrackedMessageMeta messageMeta = messageTacker.stored(messageId);
    if (messageMeta == null) {
      log.warning(clientId, messageId, "[%s] No any stored information for messageId:[%d]"::formatted);
      if (publish instanceof IncomingPublish incomingPublish) {
        incomingPublishStorage.remove(incomingPublish);
      }
      return true;
    }

    if (messageMeta.messageType() != MqttMessageType.PUBLISH) {
      log.warning(clientId, messageMeta, messageId, 
          "[%s] Not expected tracked message meta:[%s] for messageId:[%d]"::formatted);
      if (publish instanceof IncomingPublish incomingPublish) {
        incomingPublishStorage.remove(incomingPublish);
      }
      return true;
    } else if (!(message instanceof PublishReleaseMqttInMessage release)) {
      log.warning(clientId, message.messageType(), "[%s] Not expected message:[%s]"::formatted);
      if (publish instanceof IncomingPublish incomingPublish) {
        incomingPublishStorage.remove(incomingPublish);
      }
      return true;
    }

    messageTacker.update(messageId, MqttMessageType.PUBLISH_COMPLETE, PublishCompletedReasonCode.SUCCESS);

    // for QoS 2 only when we sure that this publish is fully correctly 
    // received we can register it to retain storage
    if (publish.retained()) {
      if (publish instanceof IncomingPublish incoming) {
        retainPublishService.retain(incoming);
      } else {
        throw new IllegalStateException("Unexpected type of publish:[%s], expected 'incoming'".formatted(publish));
      }
    }

    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(networkMqttUser)
        .newPublishCompleted(message.messageId(), PublishCompletedReasonCode.SUCCESS);

    sendFeedback(networkMqttUser, session, response, messageId);
    return true;
  }
}
