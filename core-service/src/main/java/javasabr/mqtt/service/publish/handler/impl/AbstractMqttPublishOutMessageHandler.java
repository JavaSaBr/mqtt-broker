package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.subscriber.Subscriber;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.mqtt.service.MessageOutFactoryService;
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

  Class<U> expectedUser;
  MessageOutFactoryService messageOutFactoryService;

  private static NetworkMqttUser resolveClient(Subscriber subscriber) {
    if (subscriber instanceof SingleSubscriber single) {
      return (NetworkMqttUser) single.user();
    }
    throw new IllegalArgumentException("Unexpected subscriber: " + subscriber);
  }

  @Override
  public PublishHandlingResult handle(Publish publish, SingleSubscriber subscriber) {
    NetworkMqttUser user = resolveClient(subscriber);
    if (!expectedUser.isInstance(user)) {
      log.warning(user, "Accepted not expected client:[%s]"::formatted);
      return PublishHandlingResult.NOT_EXPECTED_CLIENT;
    }
    publish = reconstruct(user, publish);
    if (publish == null) {
      return PublishHandlingResult.SKIPPED;
    }
    return handleImpl(publish, expectedUser.cast(user));
  }

  @Nullable
  protected Publish reconstruct(NetworkMqttUser user, Publish original) {
    return original.with(
        MqttProperties.MESSAGE_ID_IS_NOT_SET,
        qos(),
        false,
        MqttProperties.TOPIC_ALIAS_NOT_SET);
  }

  protected abstract PublishHandlingResult handleImpl(Publish publish, U client) ;

  protected void startDelivering(NetworkMqttUser user, Publish publish) {
    MqttOutMessage outMessage = messageOutFactoryService
        .resolveFactory(user)
        .newPublish(
            publish.messageId(),
            qos(),
            publish.retained(),
            publish.duplicated(),
            publish.topicName(),
            publish.topicAlias(),
            publish.payload(),
            publish.payloadFormat(),
            publish.responseTopicName(),
            publish.correlationData(),
            publish.userProperties());
    user.sendAsync(outMessage);
  }
}
