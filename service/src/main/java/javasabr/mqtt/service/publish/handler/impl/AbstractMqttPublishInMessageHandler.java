package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.packet.in.PublishInPacket;
import javasabr.mqtt.service.PublishDeliveringService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.message.handler.PublishHandlingResult;
import javasabr.mqtt.service.publish.handler.MqttPublishInMessageHandler;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class AbstractMqttPublishInMessageHandler<C extends MqttClient>
    implements MqttPublishInMessageHandler {

  Class<C> expectedClient;
  SubscriptionService subscriptionService;
  PublishDeliveringService publishDeliveringService;

  @Override
  public void handle(MqttClient client, PublishInPacket packet) {
    handleImpl(expectedClient.cast(client), packet);
  }

  protected void handleImpl(C client, PublishInPacket packet) {
    TopicName topicName = packet.getTopicName();
    if (!subscriptionService.isValid(topicName)) {
      handleInvalidTopic(client, packet.getPacketId(), topicName);
      return;
    }
    Array<SingleSubscriber> subscribers = subscriptionService.findSubscribers(topicName);
    if (subscribers.isEmpty()) {
      handleEmptySubscriptions(client, packet.getPacketId(), topicName);
      return;
    }
    for (SingleSubscriber subscriber : subscribers) {
      PublishHandlingResult checkResult = checkSubscriber(client, packet, subscriber);
      if (checkResult.error()) {
        handleError(client, packet.getPacketId(), checkResult);
        return;
      }
    }
    for (SingleSubscriber subscriber : subscribers) {
      startDelivering(client, packet, subscriber);
    }
  }

  protected void handleInvalidTopic(C client, int messageId, TopicName topicName) {}

  protected void handleEmptySubscriptions(C client, int messageId, TopicName topicName) {}

  protected void handleError(C client, int messageId, PublishHandlingResult handlingResult) {}

  protected PublishHandlingResult checkSubscriber(
      C client,
      PublishInPacket packet,
      SingleSubscriber subscriber) {
    return PublishHandlingResult.SUCCESS;
  }

  protected void startDelivering(C client, PublishInPacket packet, SingleSubscriber subscriber) {
    publishDeliveringService.startDelivering(packet, subscriber);
  }

  protected void handleSuccessfulResult(C client, int messageId, int subscribers) {}
}
