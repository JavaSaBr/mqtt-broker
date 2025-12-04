package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttProtocolErrors;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.model.session.ProcessingPublishes;
import javasabr.mqtt.model.session.PublishRetryer;
import javasabr.mqtt.model.session.TrackableMessageCallback;
import javasabr.mqtt.model.session.TrackedMessageMeta;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.message.out.factory.MqttMessageOutFactory;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@CustomLog
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class TrackableMqttPublishOutMessageHandler extends
    AbstractMqttPublishOutMessageHandler<ExternalNetworkMqttUser> {

  TrackableMessageCallback trackableMessageCallback;
  PublishRetryer publishRetryer;

  protected TrackableMqttPublishOutMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService) {
    super(ExternalNetworkMqttUser.class, subscriptionService, messageOutFactoryService);
    this.trackableMessageCallback = this::handleReceivedTrackableMessage;
    this.publishRetryer = this::retryDelivering;
  }

  @Nullable
  @Override
  protected Publish reconstruct(ExternalNetworkMqttUser user, MqttSession session, Publish original) {
    return original.with(
        // generate new uniq message id for specific user
        session.generateMessageId(),
        qos(),
        false,
        MqttProperties.TOPIC_ALIAS_NOT_SET);
  }

  @Override
  protected PublishHandlingResult handleImpl(ExternalNetworkMqttUser user, MqttSession session, Publish publish) {
    // register message id
    MessageTacker messageTacker = session.outMessageTracker();
    messageTacker.add(publish.messageId(), MqttMessageType.PUBLISH);
    // register callback and retrier
    ProcessingPublishes processingPublishes = session.outProcessingPublishes();
    processingPublishes.register(publish, trackableMessageCallback, publishRetryer);
    return super.handleImpl(user, session, publish);
  }

  protected boolean handleReceivedTrackableMessage(
      MqttUser user, 
      MqttSession session,
      TrackableMqttMessage message) {

    int messageId = message.messageId();
    MessageTacker messageTacker = session.outMessageTracker();
    TrackedMessageMeta trackedMessageMeta = messageTacker.stored(messageId);
 
    return handleReceivedTrackableMessageImpl(
        expectedUserType.cast(user), 
        session,
        message,
        trackedMessageMeta);
  }

  protected abstract boolean handleReceivedTrackableMessageImpl(
      ExternalNetworkMqttUser user,
      MqttSession session,
      TrackableMqttMessage message,
      @Nullable TrackedMessageMeta trackedMessageMeta);

  protected void retryDelivering(MqttUser user, MqttSession session, Publish publish) {
    retryDeliveringImpl(expectedUserType.cast(user), session, publish);
  }

  protected void retryDeliveringImpl(ExternalNetworkMqttUser user, MqttSession session, Publish publish) {
    int messageId = publish.messageId();
    MessageTacker messageTacker = session.outMessageTracker();
    TrackedMessageMeta messageMeta = messageTacker.stored(messageId);
    if (messageMeta == null) {
      log.warning(user.clientId(), messageId, "[%s] No any stored information for messageId:[%d]"::formatted);
      return;
    } else if(messageMeta.messageType() != MqttMessageType.PUBLISH) {
      log.warning(user.clientId(), messageMeta, messageId,
          "[%s] Not expected tracked message meta:[%s] for messageId:[%d]"::formatted);
      return;
    }
    log.debug(user.clientId(), messageId, "[%s] Retry to deliver publish:[%s]"::formatted);
    send(user, publish.withDuplicated());
  }

  protected void handleNotExpectedFlowState(
      ExternalNetworkMqttUser user,
      MqttMessageType trackedMessageType,
      MqttMessageType expectedTrackedMessageType) {
    MqttMessageOutFactory messageOutFactory = messageOutFactoryService.resolveFactory(user);
    String reason = MqttProtocolErrors.UNEXPECTED_FLOW_STATE.formatted(
        trackedMessageType,
        expectedTrackedMessageType);
    user.closeWithReason(messageOutFactory.newDisconnect(user, DisconnectReasonCode.PROTOCOL_ERROR, reason));
  }

  protected void handleNotExpectedResponseMessage(
      ExternalNetworkMqttUser user,
      TrackableMqttMessage receivedMessage,
      MqttMessageType expectedMessageType) {
    MqttMessageOutFactory messageOutFactory = messageOutFactoryService.resolveFactory(user);
    String reason = MqttProtocolErrors.UNEXPECTED_RESPONSE_MESSAGE.formatted(
        receivedMessage.messageType(),
        expectedMessageType);
    user.closeWithReason(messageOutFactory.newDisconnect(user, DisconnectReasonCode.PROTOCOL_ERROR, reason));
  }
}
