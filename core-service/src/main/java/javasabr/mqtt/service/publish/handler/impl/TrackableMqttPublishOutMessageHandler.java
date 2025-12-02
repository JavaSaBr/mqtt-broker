package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.model.session.ProcessingPublishes;
import javasabr.mqtt.model.session.PublishRetryer;
import javasabr.mqtt.model.session.TrackableMessageCallback;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

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
    return handleReceivedTrackableMessageImpl(expectedUserType.cast(user), session, message);
  }

  protected abstract boolean handleReceivedTrackableMessageImpl(
      ExternalNetworkMqttUser user,
      MqttSession session,
      TrackableMqttMessage message);

  protected void retryDelivering(MqttUser user, MqttSession session, Publish publish) {
    retryDeliveringImpl(expectedUserType.cast(user), session, publish);
  }

  protected abstract void retryDeliveringImpl(
      ExternalNetworkMqttUser user,
      MqttSession session,
      Publish publish);
}
