package javasabr.mqtt.service.publish.sender;

import javasabr.mqtt.model.MqttProtocolErrors;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.model.publish.OutgoingPublish;
import javasabr.mqtt.model.publish.Publish;
import javasabr.mqtt.model.publish.TrackableSimpleOutgoingPublish;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.model.session.PublishRetryer;
import javasabr.mqtt.model.session.TrackableMessageCallback;
import javasabr.mqtt.model.session.TrackedMessageMeta;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.message.out.factory.MqttMessageOutFactory;
import javasabr.mqtt.service.publish.IncomingPublishStorage;
import javasabr.rlib.collections.array.IntArray;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@CustomLog
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class TrackableSubscriberPublishSender extends
    AbstractSubscriberPublishSender<ExternalNetworkMqttUser> {

  TrackableMessageCallback<OutgoingPublish> trackableMessageCallback;
  PublishRetryer publishRetryer;

  protected TrackableSubscriberPublishSender(
      MessageOutFactoryService messageOutFactoryService,
      IncomingPublishStorage incomingPublishStorage) {
    super(ExternalNetworkMqttUser.class, messageOutFactoryService, incomingPublishStorage);
    this.trackableMessageCallback = this::handleReceivedTrackableMessage;
    this.publishRetryer = this::retrySending;
  }

  @Nullable
  @Override
  protected OutgoingPublish buildOutgoingPublish(
      ExternalNetworkMqttUser user, 
      MqttSession session, 
      IncomingPublish incomingPublish) {
    return new TrackableSimpleOutgoingPublish(
        incomingPublish,
        // generate new uniq message id for specific user
        session.generateMessageId(),
        false,
        incomingPublish.retained(),
        qos(),
        IntArray.EMPTY);
  }

  @Override
  protected final void sendToSubscriberImpl(
      ExternalNetworkMqttUser user, 
      MqttSession session, 
      OutgoingPublish outgoingPublish) {
    // register message id
    session
        .outMessageTracker()
        .add(outgoingPublish.messageId(), MqttMessageType.PUBLISH);
    // register callback and retrier
    session
        .outgoingProcessingPublishes()
        .register(outgoingPublish, trackableMessageCallback, publishRetryer);
    super.sendToSubscriberImpl(user, session, outgoingPublish);
  }

  protected final boolean handleReceivedTrackableMessage(
      MqttUser user, 
      MqttSession session,
      TrackableMqttMessage message,
      Publish publish) {
    ExternalNetworkMqttUser networkMqttUser = expectedUserType.cast(user);
    int messageId = message.messageId();
    MessageTacker messageTacker = session.outMessageTracker();
    TrackedMessageMeta trackedMessageMeta = messageTacker.stored(messageId);
    return handleReceivedTrackableMessageImpl(networkMqttUser, session, message, trackedMessageMeta, publish);
  }

  protected abstract boolean handleReceivedTrackableMessageImpl(
      ExternalNetworkMqttUser user,
      MqttSession session,
      TrackableMqttMessage message,
      @Nullable TrackedMessageMeta trackedMessageMeta,
      Publish publish);

  protected final void retrySending(MqttUser user, MqttSession session, Publish publish) {
    if (!(publish instanceof OutgoingPublish outgoingPublish)) {
      throw new IllegalArgumentException("Unexpected publish type:[%s]".formatted(publish));
    }
    ExternalNetworkMqttUser networkMqttUser = expectedUserType.cast(user);
    String clientId = networkMqttUser.clientId();
    int messageId = publish.messageId();
    MessageTacker outMessageTracker = session.outMessageTracker();
    TrackedMessageMeta trackedMessageMeta = outMessageTracker.stored(messageId);
    if (trackedMessageMeta == null) {
      log.warn(clientId, messageId, "[%s] No any stored information for messageId:[%d]"::formatted);
    } else if (trackedMessageMeta.messageType() != MqttMessageType.PUBLISH) {
      log.warn(clientId, trackedMessageMeta, messageId,
          "[%s] Not expected tracked message meta:[%s] for messageId:[%d]"::formatted);
    } else {
      log.debug(clientId, messageId, "[%s] Retry to deliver publish:[%s]"::formatted);
      send(networkMqttUser, outgoingPublish.withDuplicated());
    }
  }

  protected void handleNotExpectedFlowState(
      ExternalNetworkMqttUser user,
      MqttMessageType trackedMessageType,
      MqttMessageType expectedTrackedMessageType,
      OutgoingPublish outgoingPublish) {
    MqttMessageOutFactory messageOutFactory = messageOutFactoryService.resolveFactory(user);
    String reason = MqttProtocolErrors.UNEXPECTED_FLOW_STATE.formatted(
        trackedMessageType,
        expectedTrackedMessageType);
    user.closeWithReason(messageOutFactory.newDisconnect(
        user,
        DisconnectReasonCode.PROTOCOL_ERROR, 
        reason));
    incomingPublishStorage.decreaseConsumerCount(outgoingPublish.source(), 1);
  }

  protected void handleNotExpectedResponseMessage(
      ExternalNetworkMqttUser user,
      TrackableMqttMessage receivedMessage,
      MqttMessageType expectedMessageType,
      OutgoingPublish outgoingPublish) {
    MqttMessageOutFactory messageOutFactory = messageOutFactoryService.resolveFactory(user);
    String reason = MqttProtocolErrors.UNEXPECTED_RESPONSE_MESSAGE.formatted(
        receivedMessage.messageType(),
        expectedMessageType);
    user.closeWithReason(messageOutFactory.newDisconnect(
        user, 
        DisconnectReasonCode.PROTOCOL_ERROR,
        reason));
    incomingPublishStorage.decreaseConsumerCount(outgoingPublish.source(), 1);
  }
}
