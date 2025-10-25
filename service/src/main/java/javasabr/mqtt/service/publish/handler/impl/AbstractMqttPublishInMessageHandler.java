package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.message.in.PublishMqttInMessage;
import javasabr.mqtt.service.PublishDeliveringService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.publish.handler.MqttPublishInMessageHandler;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@CustomLog
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class AbstractMqttPublishInMessageHandler<C extends MqttClient>
    implements MqttPublishInMessageHandler {

  Class<C> expectedClient;
  SubscriptionService subscriptionService;
  PublishDeliveringService publishDeliveringService;

  @Override
  public void handle(MqttClient client, PublishMqttInMessage packet) {
    if (!expectedClient.isInstance(client)) {
      log.warning(client, "Not expected client:[%s]"::formatted);
      return;
    }
    handleImpl(expectedClient.cast(client), packet);
  }

  protected void handleImpl(C client, PublishMqttInMessage packet) {
    TopicName topicName = packet.topicName();
    if (!subscriptionService.isValid(topicName)) {
      handleInvalidTopic(client, packet.messageId(), topicName);
      return;
    }

    Array<SingleSubscriber> subscribers = subscriptionService.findSubscribers(topicName);
    if (subscribers.isEmpty()) {
      handleEmptySubscriptions(client, packet.messageId(), topicName);
      return;
    }

    for (SingleSubscriber subscriber : subscribers) {
      PublishHandlingResult checkResult = checkSubscriber(client, packet, subscriber);
      if (checkResult.error()) {
        handleError(client, packet.messageId(), checkResult);
        return;
      }
    }

    int count = 0;
    PublishHandlingResult errorResult = null;
    for (SingleSubscriber subscriber : subscribers) {
      PublishHandlingResult result = startDelivering(client, packet, subscriber);
      if (result.error()) {
        errorResult = result;
      } else if(result == PublishHandlingResult.SUCCESS) {
        count++;
      }
    }

    if (errorResult != null) {
      handleError(client, packet.messageId(), errorResult);
    } else {
      handleSuccessfulResult(client, packet, count);
    }
  }

  protected void handleInvalidTopic(C client, int messageId, TopicName topicName) {}

  protected void handleEmptySubscriptions(C client, int messageId, TopicName topicName) {}

  protected void handleError(C client, int messageId, PublishHandlingResult handlingResult) {}

  protected void handleSuccessfulResult(C client, PublishMqttInMessage packet, int subscribers) {}

  protected PublishHandlingResult checkSubscriber(
      C client,
      PublishMqttInMessage packet,
      SingleSubscriber subscriber) {
    return PublishHandlingResult.SUCCESS;
  }

  protected PublishHandlingResult startDelivering(C client, PublishMqttInMessage packet, SingleSubscriber subscriber) {
    return publishDeliveringService.startDelivering(packet, subscriber);
  }
}
