package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.publishing.Publish;
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
public abstract class AbstractMqttPublishOutMessageHandler<C extends NetworkMqttUser>
    implements MqttPublishOutMessageHandler {

  Class<C> expectedClient;
  SubscriptionService subscriptionService;
  MessageOutFactoryService messageOutFactoryService;

  @Override
  public PublishHandlingResult handle(Publish publish, SingleSubscriber subscriber) {
    NetworkMqttUser user = subscriptionService.resolveClient(subscriber);
    if (!expectedClient.isInstance(user)) {
      log.warning(user, "Accepted not expected client:[%s]"::formatted);
      return PublishHandlingResult.NOT_EXPECTED_CLIENT;
    }
    publish = reconstruct(user, publish);
    if (publish == null) {
      return PublishHandlingResult.SKIPPED;
    }
    return handleImpl(publish, expectedClient.cast(user));
  }

  @Nullable
  protected Publish reconstruct(NetworkMqttUser client, Publish original) {
    return original.with(
        MqttProperties.MESSAGE_ID_IS_NOT_SET,
        qos(),
        false,
        MqttProperties.TOPIC_ALIAS_NOT_SET);
  }

  protected abstract PublishHandlingResult handleImpl(Publish publish, C client) ;

  protected void startDelivering(NetworkMqttUser client, Publish publish) {
    MqttOutMessage outMessage = messageOutFactoryService
        .resolveFactory(client)
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
    client.send(outMessage);
  }
}
