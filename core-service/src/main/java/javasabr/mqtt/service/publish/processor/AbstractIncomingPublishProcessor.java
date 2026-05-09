package javasabr.mqtt.service.publish.processor;

import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.publish.IncomingPublishStorage;
import javasabr.mqtt.service.publish.PublishDispatcher;
import javasabr.mqtt.service.publish.RetainPublishService;
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
  IncomingPublishStorage incomingPublishStorage;

  @Override
  public final void process(NetworkMqttUser user, IncomingPublish publish) {
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
      processImpl(expectedUser, session, publish);
    }
  }

  protected boolean validateImpl(U user, NetworkMqttSession session, IncomingPublish publish) {
    return true;
  }

  protected void processImpl(U user, NetworkMqttSession session, IncomingPublish publish) {
  }

  protected void dispatchToSubscriber(U user, NetworkMqttSession session, IncomingPublish publish) {
    // mark + 1 during dispatching process
    incomingPublishStorage.increaseConsumerCount(publish, 1);
    
    if (publish.retained()) {
      // to avoid auto-removing retained publish we should add +1 long time consumer
      incomingPublishStorage.increaseConsumerCount(publish, 1);
      IncomingPublish prevRetainedPublish = retainPublishService.retain(publish);
      if (prevRetainedPublish != null) {
        incomingPublishStorage.decreaseConsumerCount(prevRetainedPublish, 1);
      }
    }

    TopicName topicName = publish.topicName();
    Array<SingleSubscriber> subscribers = subscriptionService.findSubscribers(topicName);
    if (subscribers.isEmpty()) {
      log.debug(user.clientId(), publish, "[%s] Not found any subscriber for publish: [%s]"::formatted);
      handleNoMatchedSubscribers(user, session, publish);
      return;
    }

    int matchedSubscribers = subscribers.size();
    log.debug(matchedSubscribers, "Starting dispatching publish to [%s] subscribers"::formatted);
    incomingPublishStorage.increaseConsumerCount(publish, matchedSubscribers);
    
    int skipped = 0;
    for (SingleSubscriber subscriber : subscribers) {
      PublishProcessingResult checkResult = checkSubscriber(user, publish, subscriber);
      if (checkResult.error()) {
        log.debug(user.clientId(), checkResult, subscriber,
            "[%s] Found error:[%s] for subscriber:[%s] during checking"::formatted);
        skipped++;
      } else if (checkResult == PublishProcessingResult.SUCCESS) {
        dispatchToSubscriber(publish, subscriber);
      }
    }
    if (skipped > 0) {
      incomingPublishStorage.decreaseConsumerCount(publish, skipped);
    }
    int result = matchedSubscribers - skipped;
    if (result > 0) {
      handleMatchedSubscribers(user, session, publish, result);
    } else {
      handleNoMatchedSubscribers(user, session, publish);
    }
  }

  protected void handleNoMatchedSubscribers(U user, NetworkMqttSession session, IncomingPublish publish) {
    // unmark + 1 from dispatching process
    incomingPublishStorage.decreaseConsumerCount(publish, 1);
  }

  protected void handleMatchedSubscribers(
      U user, 
      NetworkMqttSession session, 
      IncomingPublish publish, 
      int matchedSubscribers) {
    log.debug(matchedSubscribers, "Dispatched publish in the result to [%s] subscribers"::formatted);
    // unmark + 1 from dispatching process
    incomingPublishStorage.decreaseConsumerCount(publish, 1);
  }

  protected PublishProcessingResult checkSubscriber(
      U user,
      IncomingPublish publish,
      SingleSubscriber subscriber) {
    return PublishProcessingResult.SUCCESS;
  }

  protected void dispatchToSubscriber(IncomingPublish publish, SingleSubscriber subscriber) {
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
