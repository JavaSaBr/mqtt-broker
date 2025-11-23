package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.MqttSession;
import javasabr.mqtt.service.MessageOutFactoryService;
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
  MessageOutFactoryService messageOutFactoryService;

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
    if (validateImpl(expectedClient, session, publish)) {
      handleImpl(expectedClient, session, publish);
    }
  }

  protected boolean validateImpl(C client, MqttSession session, Publish publish) {
    return true;
  }

  protected void handleImpl(C client, MqttSession session, Publish publish) {
    TopicName topicName = publish.topicName();
    Array<SingleSubscriber> subscribers = subscriptionService.findSubscribers(topicName);
    if (subscribers.isEmpty()) {
      log.debug(client.clientId(), publish, "[%s] Not found any subscriber for publish: [%s]"::formatted);
      handleNoMatchedSubscribers(client, session, publish);
      return;
    }

    int count = 0;
    for (SingleSubscriber subscriber : subscribers) {
      PublishHandlingResult checkResult = checkSubscriber(client, publish, subscriber);
      if (checkResult.error()) {
        log.debug(client.clientId(), checkResult, subscriber,
            "[%s] Found error:[%s] for subscriber:[%s] during checking"::formatted);
        handleError(client, session, publish, checkResult);
        return;
      } else if(checkResult == PublishHandlingResult.SUCCESS) {
        count++;
      }
    }

    log.debug(client.clientId(), count,
        "[%s] Started delivering publish to [%s] subscribers"::formatted);
    handleSuccess(client, session, publish, count);

    for (SingleSubscriber subscriber : subscribers) {
      startDelivering(client, session, publish, subscriber);
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

  protected void sendFeedback(C client, MqttOutMessage response) {
    client.send(response);
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
