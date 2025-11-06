package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.PayloadFormat;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.message.out.PublishMqttOutMessage;
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
public abstract class AbstractMqttPublishOutMessageHandler<C extends MqttClient>
    implements MqttPublishOutMessageHandler {

  Class<C> expectedClient;
  SubscriptionService subscriptionService;
  MessageOutFactoryService messageOutFactoryService;

  @Override
  public PublishHandlingResult handle(Publish publish, SingleSubscriber subscriber) {
    MqttClient client = subscriptionService.resolveClient(subscriber);
    if (!expectedClient.isInstance(client)) {
      log.warning(client, "Accepted not expected client:[%s]"::formatted);
      return PublishHandlingResult.NOT_EXPECTED_CLIENT;
    }
    publish = reconstruct(client, publish);
    if (publish == null) {
      return PublishHandlingResult.SKIPPED;
    }
    return handleImpl(publish, expectedClient.cast(client));
  }

  @Nullable
  protected Publish reconstruct(MqttClient client, Publish original) {
    return original.with(
        MqttProperties.MESSAGE_ID_IS_NOT_SET,
        qos(),
        false,
        MqttProperties.TOPIC_ALIAS_UNDEFINED);
  }

  protected abstract PublishHandlingResult handleImpl(Publish publish, C client) ;

  protected void startDelivering(MqttClient client, Publish publish) {
    TopicName responseTopicName = publish.responseTopicName();
    PublishMqttOutMessage outMessage = messageOutFactoryService
        .resolveFactory(client)
        .newPublish(
            publish.messageId(),
            qos(),
            publish.retained(),
            publish.duplicated(),
            publish.topicName().toString(),
            publish.topicAlias(),
            publish.payload(),
            publish.payloadFormat() == PayloadFormat.UTF8_STRING,
            responseTopicName == null ? null : responseTopicName.toString(),
            publish.correlationData(),
            publish.userProperties());
    client.send(outMessage);
  }
}
