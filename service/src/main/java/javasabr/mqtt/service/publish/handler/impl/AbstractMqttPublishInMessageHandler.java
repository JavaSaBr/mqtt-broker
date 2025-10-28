package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.network.MqttClient;
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
  public void handle(MqttClient client, Publish packet) {
    if (!expectedClient.isInstance(client)) {
      log.warning(client, "Not expected client:[%s]"::formatted);
      return;
    }
    handleImpl(expectedClient.cast(client), packet);
  }

  protected void handleImpl(C client, Publish publish) {
    TopicName topicName = publish.topicName();
    Array<SingleSubscriber> subscribers = subscriptionService.findSubscribers(topicName);
    if (subscribers.isEmpty()) {
      handleEmptySubscriptions(client, publish);
      return;
    }

    for (SingleSubscriber subscriber : subscribers) {
      PublishHandlingResult checkResult = checkSubscriber(client, publish, subscriber);
      if (checkResult.error()) {
        handleError(client, publish, checkResult);
        return;
      }
    }

    int count = 0;
    PublishHandlingResult errorResult = null;
    for (SingleSubscriber subscriber : subscribers) {
      PublishHandlingResult result = startDelivering(client, publish, subscriber);
      if (result.error()) {
        errorResult = result;
      } else if(result == PublishHandlingResult.SUCCESS) {
        count++;
      }
    }

    if (errorResult != null) {
      handleError(client, publish, errorResult);
    } else {
      handleSuccessfulResult(client, publish, count);
    }
  }

  protected void handleEmptySubscriptions(C client, Publish publish) {}

  protected void handleError(C client, Publish publish, PublishHandlingResult handlingResult) {}

  protected void handleSuccessfulResult(C client, Publish publish, int subscribers) {}

  protected PublishHandlingResult checkSubscriber(
      C client,
      Publish publish,
      SingleSubscriber subscriber) {
    return PublishHandlingResult.SUCCESS;
  }

  protected PublishHandlingResult startDelivering(C client, Publish publish, SingleSubscriber subscriber) {
    return publishDeliveringService.startDelivering(publish, subscriber);
  }
}
