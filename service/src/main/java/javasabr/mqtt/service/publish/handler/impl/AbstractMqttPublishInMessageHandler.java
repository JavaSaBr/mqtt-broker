package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.MessageTacker;
import javasabr.mqtt.network.session.MqttSession;
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

  Class<C> expectedClientType;
  SubscriptionService subscriptionService;
  PublishDeliveringService publishDeliveringService;

  @Override
  public void handle(MqttClient client, Publish publish) {
    if (!expectedClientType.isInstance(client)) {
      log.warning(client.clientId(), client.getClass(),
          "[%s] Not expected client of type:[%s]"::formatted);
      return;
    }
    C expectedClient = expectedClientType.cast(client);
    MqttSession session = expectedClient.session();
    if (session == null) {
      log.warning(client.clientId(), "[%s] Session is already closed"::formatted);
      return;
    }
    handleImpl(expectedClient, session, publish);
  }

  protected void handleImpl(C client, MqttSession session, Publish publish) {
    TopicName topicName = publish.topicName();
    Array<SingleSubscriber> subscribers = subscriptionService.findSubscribers(topicName);
    if (subscribers.isEmpty()) {
      log.debug(client.clientId(), publish, "[%s] Not found any subscriber for publish: [%s]"::formatted);
      handleNoMatchedSubscribers(client, session, publish);
      return;
    }

    for (SingleSubscriber subscriber : subscribers) {
      PublishHandlingResult checkResult = checkSubscriber(client, publish, subscriber);
      if (checkResult.error()) {
        log.debug(client.clientId(), checkResult, subscriber,
            "[%s] Found error:[%s] for subscriber:[%s] during checking"::formatted);
        handleError(client, session, publish, checkResult);
        return;
      }
    }

    int count = 0;
    PublishHandlingResult errorResult = null;
    for (SingleSubscriber subscriber : subscribers) {
      PublishHandlingResult result = startDelivering(client, session, publish, subscriber);
      if (result.error()) {
        errorResult = result;
      } else if(result == PublishHandlingResult.SUCCESS) {
        count++;
      }
    }

    if (errorResult != null) {
      log.debug(client.clientId(), errorResult,
          "[%s] Found final error:[%s] during processing publish"::formatted);
      handleError(client, session, publish, errorResult);
    } else {
      log.debug(client.clientId(), count,
          "[%s] Successfully started delivering publish to [%s] subscribers"::formatted);
      handleSuccess(client, session, publish, count);
    }
  }

  protected void handleNoMatchedSubscribers(C client, MqttSession session, Publish publish) {}

  protected void handleSuccess(
      C client,
      MqttSession session,
      Publish publish,
      int matchedSubscribers) {}

  protected void handleError(
      C client,
      MqttSession session,
      Publish publish,
      PublishHandlingResult handlingResult) {}

  protected PublishHandlingResult checkSubscriber(
      C client,
      Publish publish,
      SingleSubscriber subscriber) {
    return PublishHandlingResult.SUCCESS;
  }

  protected PublishHandlingResult startDelivering(
      C client,
      MqttSession session,
      Publish publish,
      SingleSubscriber subscriber) {
    return publishDeliveringService.startDelivering(publish, subscriber);
  }

  protected void sendFeedback(
      C client,
      MqttSession session,
      MqttOutMessage response,
      int messageId) {
    MessageTacker messageTacker = session.inMessageTracker();
    client
        .sendWithFeedback(response)
        .thenAccept(_ -> messageTacker.remove(messageId));
  }
}
