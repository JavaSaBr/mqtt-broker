package javasabr.mqtt.model.subscriber.tree;

import java.util.Objects;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.subscriber.SharedSubscriber;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.subscriber.Subscriber;
import javasabr.mqtt.model.subscription.Subscription;
import javasabr.mqtt.model.topic.SharedTopicFilter;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.rlib.collections.array.LockableArray;
import javasabr.rlib.collections.array.MutableArray;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
abstract class SubscriberTreeBase {

  /**
   * @return previous subscriber with the same user
   */
  @Nullable
  protected static SingleSubscriber addSubscriber(
      LockableArray<Subscriber> subscribers,
      MqttUser user,
      Subscription subscription,
      TopicFilter topicFilter) {
    long stamp = subscribers.writeLock();
    try {
      if (topicFilter instanceof SharedTopicFilter stf) {
        addSharedSubscriber(subscribers, user, subscription, stf);
        return null;
      } else {
        SingleSubscriber previous = removePreviousIfExist(subscribers, user);
        subscribers.add(new SingleSubscriber(user, subscription));
        return previous;
      }
    } finally {
      subscribers.writeUnlock(stamp);
    }
  }

  @Nullable
  private static SingleSubscriber removePreviousIfExist(LockableArray<Subscriber> subscribers, MqttUser user) {
    int index = subscribers.indexOf(Subscriber::resolveUser, user);
    if (index < 0) {
      return null;
    }
    return subscribers
        .remove(index)
        .resolveSingle();
  }

  private static void addSharedSubscriber(
      LockableArray<Subscriber> subscribers,
      MqttUser user,
      Subscription subscription,
      SharedTopicFilter sharedTopicFilter) {

    String group = sharedTopicFilter.shareName();
    SharedSubscriber sharedSubscriber = (SharedSubscriber) subscribers
        .iterations()
        .findAny(group, SubscriberTreeBase::isSharedSubscriberWithGroup);

    if (sharedSubscriber == null) {
      sharedSubscriber = new SharedSubscriber(sharedTopicFilter);
      subscribers.add(sharedSubscriber);
    }

    sharedSubscriber.addSubscriber(new SingleSubscriber(user, subscription));
  }

  protected static void appendSubscribersTo(MutableArray<SingleSubscriber> result, SubscriberNode subscriberNode) {
    LockableArray<Subscriber> subscribers = subscriberNode.subscribers();
    if (subscribers == null) {
      return;
    }
    long stamp = subscribers.readLock();
    try {
      for (Subscriber subscriber : subscribers) {
        addOrReplaceIfLowerQos(result, subscriber);
      }
    } finally {
      subscribers.readUnlock(stamp);
    }
  }

  protected static boolean removeSubscriber(
      @Nullable LockableArray<Subscriber> subscribers,
      MqttUser user,
      TopicFilter topicFilter) {
    if (subscribers == null) {
      return false;
    }
    long stamp = subscribers.writeLock();
    try {
      if (topicFilter instanceof SharedTopicFilter stf) {
        return removeSharedSubscriber(subscribers, user, stf);
      } else {
        int index = subscribers.indexOf(Subscriber::resolveUser, user);
        if (index >= 0) {
          subscribers.remove(index);
          return true;
        }
      }
    } finally {
      subscribers.writeUnlock(stamp);
    }
    return false;
  }

  private static boolean removeSharedSubscriber(
      LockableArray<Subscriber> subscribers,
      MqttUser user,
      SharedTopicFilter sharedTopicFilter) {
    String group = sharedTopicFilter.shareName();
    SharedSubscriber sharedSubscriber = (SharedSubscriber) subscribers
        .iterations()
        .findAny(group, SubscriberTreeBase::isSharedSubscriberWithGroup);
    if (sharedSubscriber != null) {
      boolean removed = sharedSubscriber.removeSubscriberWithUser(user);
      if (sharedSubscriber.isEmpty()) {
        // if it was last member
        subscribers.remove(sharedSubscriber);
      }
      return removed;
    }
    return false;
  }

  private static boolean isSharedSubscriberWithGroup(Subscriber subscriber, String group) {
    return subscriber instanceof SharedSubscriber shared && Objects.equals(group, shared.group());
  }

  private static void addOrReplaceIfLowerQos(MutableArray<SingleSubscriber> result, Subscriber subscriber) {
    SingleSubscriber candidate = subscriber.resolveSingle();
    int found = result.indexOf(SingleSubscriber::user, candidate.user());
    if (found == -1) {
      result.add(candidate);
      return;
    }
    QoS candidateQos = candidate.qos();
    QoS existedQos = result
        .get(found)
        .qos();
    if (existedQos.ordinal() < candidateQos.ordinal()) {
      result.remove(found);
      result.add(candidate);
    }
  }
}
