package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.publish.handler.MqttPublishOutMessageHandler;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@CustomLog
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class AbstractMqttPublishOutMessageHandler<U extends NetworkMqttUser>
    implements MqttPublishOutMessageHandler {

  Class<U> expectedUserType;
  SubscriptionService subscriptionService;
  MessageOutFactoryService messageOutFactoryService;

  @Override
  public PublishHandlingResult handle(Publish publish, SingleSubscriber subscriber) {
    MqttUser user = subscriber.resolveUser();
    if (!expectedUserType.isInstance(user)) {
      log.warning(user.clientId(), user.getClass(), "[%s] Not expected user of type:[%s]"::formatted);
      return PublishHandlingResult.NOT_EXPECTED_CLIENT;
    }
    U expectedUser = expectedUserType.cast(user);
    MqttSession session = expectedUser.session();
    if (session == null) {
      log.warning(user.clientId(), "[%s] Session is already closed"::formatted);
      return PublishHandlingResult.SESSION_IS_ALREADY_CLOSED;
    }
    publish = reconstruct(expectedUser, session, publish);
    if (publish == null) {
      return PublishHandlingResult.SKIPPED;
    }
    return handleImpl(expectedUser, session, publish);
  }

  @Nullable
  protected abstract Publish reconstruct(U user, MqttSession session, Publish original);

  protected PublishHandlingResult handleImpl(U user, MqttSession session, Publish publish) {
    send(user, publish);
    return PublishHandlingResult.SUCCESS;
  }

  protected void send(U user, Publish publish) {
    MqttOutMessage outMessage = messageOutFactoryService
        .resolveFactory(user)
        .newPublish(
            publish.messageId(),
            publish.qos(),
            publish.retained(),
            publish.duplicated(),
            publish.topicName(),
            publish.topicAlias(),
            publish.payload(),
            publish.payloadFormat(),
            publish.responseTopicName(),
            publish.correlationData(),
            publish.userProperties());
    user.sendInBackground(outMessage);
  }
}
