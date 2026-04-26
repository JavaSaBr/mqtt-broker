package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.publish.Publish;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.PublishDispatcher;
import javasabr.mqtt.service.RetainPublishService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.publish.handler.IncomingPublishProcessor;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@CustomLog
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class AbstractIncomingPublishProcessor<U extends NetworkMqttUser>
    implements IncomingPublishProcessor {

  Class<U> expectedUserType;
  SubscriptionService subscriptionService;
  PublishDispatcher publishDispatcher;
  MessageOutFactoryService messageOutFactoryService;
  RetainPublishService retainPublishService;

  @Override
  public final void process(NetworkMqttUser user, Publish publish) {
    if (!expectedUserType.isInstance(user)) {
      log.warning(user.clientId(), user.getClass(), "[%s] Not expected user of type:[%s]"::formatted);
      return;
    }
    U expectedUser = expectedUserType.cast(user);
    NetworkMqttSession session = expectedUser.session();
    if (session == null) {
      log.warning(user.clientId(), "[%s] Session is already closed"::formatted);
      return;
    }
    if (validateImpl(expectedUser, session, publish)) {
      handleImpl(expectedUser, session, publish);
    }
  }

  protected boolean validateImpl(U user, NetworkMqttSession session, Publish publish) {
    return true;
  }

  protected void handleImpl(U user, NetworkMqttSession session, Publish publish) {
    TopicName topicName = publish.topicName();
    Array<SingleSubscriber> subscribers = subscriptionService.findSubscribers(topicName);
    if (subscribers.isEmpty()) {
      log.debug(user.clientId(), publish, "[%s] Not found any subscriber for publish: [%s]"::formatted);
      handleNoMatchedSubscribers(user, session, publish);
      return;
    }

    int count = 0;
    for (SingleSubscriber subscriber : subscribers) {
      PublishHandlingResult checkResult = checkSubscriber(user, publish, subscriber);
      if (checkResult.error()) {
        log.debug(user.clientId(), checkResult, subscriber,
            "[%s] Found error:[%s] for subscriber:[%s] during checking"::formatted);
        handleError(user, session, publish, checkResult);
        return;
      } else if(checkResult == PublishHandlingResult.SUCCESS) {
        count++;
      }
    }

    log.debug(count, "Started delivering publish to [%s] subscribers"::formatted);
    handleSuccess(user, session, publish, count);

    for (SingleSubscriber subscriber : subscribers) {
      startDelivering(publish, subscriber);
    }
  }

  protected void handleNoMatchedSubscribers(U user, NetworkMqttSession session, Publish publish) {}

  protected void handleSuccess(
      U user,
      NetworkMqttSession session,
      Publish publish,
      int matchedSubscribers) {
    if (publish.retained()) {
      retainPublishService.retain(publish);
    }
  }

  protected void handleError(
      U user,
      NetworkMqttSession session,
      Publish publish,
      PublishHandlingResult handlingResult) {}

  protected PublishHandlingResult checkSubscriber(
      U user,
      Publish publish,
      SingleSubscriber subscriber) {
    return PublishHandlingResult.SUCCESS;
  }

  protected void startDelivering(Publish publish, SingleSubscriber subscriber) {
    publishDispatcher.dispatchToSubscriber(publish, subscriber.user(), subscriber.subscription());
  }

  protected void sendFeedback(U user, MqttOutMessage response) {
    user.sendInBackground(response);
  }

  protected void sendFeedback(
      U user,
      MqttSession session,
      MqttOutMessage response,
      int messageId) {
    MessageTacker messageTacker = session.inMessageTracker();
    user.sendAsync(response)
        .thenAccept(_ -> messageTacker.remove(messageId));
  }
}
