package javasabr.mqtt.model.topic.tree;

import java.util.Objects;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.subscriber.SharedSubscriber;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.subscriber.Subscriber;
import javasabr.mqtt.model.subscribtion.Subscription;
import javasabr.mqtt.model.subscribtion.SubscriptionOwner;
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
abstract class TopicTreeBase {

  protected static void addSubscriber(
      LockableArray<Subscriber> subscribers,
      SubscriptionOwner owner,
      Subscription subscription,
      TopicFilter topicFilter) {
    long stamp = subscribers.writeLock();
    try {
      if (topicFilter instanceof SharedTopicFilter stf) {
        addSharedSubscriber(subscribers, owner, subscription, stf);
      } else {
        subscribers.add(new SingleSubscriber(owner, subscription));
      }
    } finally {
      subscribers.writeUnlock(stamp);
    }
  }

  private static void addSharedSubscriber(
      LockableArray<Subscriber> subscribers,
      SubscriptionOwner owner,
      Subscription subscription,
      SharedTopicFilter sharedTopicFilter) {

    String group = sharedTopicFilter.shareName();
    SharedSubscriber sharedSubscriber = (SharedSubscriber) subscribers
        .iterations()
        .findAny(group, TopicTreeBase::isSharedSubscriberWithGroup);

    if (sharedSubscriber == null) {
      sharedSubscriber = new SharedSubscriber(sharedTopicFilter);
      subscribers.add(sharedSubscriber);
    }

    sharedSubscriber.addSubscriber(new SingleSubscriber(owner, subscription));
  }

  protected static void appendSubscribersTo(MutableArray<SingleSubscriber> result, TopicNode topicNode) {
    LockableArray<Subscriber> subscribers = topicNode.subscribers();
    if (subscribers == null) {
      return;
    }
    long stamp = subscribers.readLock();
    try {
      for (Subscriber subscriber : subscribers) {
        SingleSubscriber singleSubscriber = subscriber.resolveSingle();
        if (removeDuplicateWithLowerQoS(result, singleSubscriber)) {
          result.add(singleSubscriber);
        }
      }
    } finally {
      subscribers.readUnlock(stamp);
    }
  }

  protected static boolean removeSubscriber(
      @Nullable LockableArray<Subscriber> subscribers,
      SubscriptionOwner owner,
      TopicFilter topicFilter) {
    if (subscribers == null) {
      return false;
    }
    long stamp = subscribers.writeLock();
    try {
      if (topicFilter instanceof SharedTopicFilter stf) {
        return removeSharedSubscriber(subscribers, owner, stf);
      } else {
        int index = subscribers.indexOf(Subscriber::resolveOwner, owner);
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
      SubscriptionOwner owner,
      SharedTopicFilter sharedTopicFilter) {
    String group = sharedTopicFilter.shareName();
    SharedSubscriber sharedSubscriber = (SharedSubscriber) subscribers
        .iterations()
        .findAny(group, TopicTreeBase::isSharedSubscriberWithGroup);
    if (sharedSubscriber != null) {
      boolean removed = sharedSubscriber.removeSubscriberWithOwner(owner);
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

  private static boolean removeDuplicateWithLowerQoS(
      MutableArray<SingleSubscriber> result, SingleSubscriber candidate) {

    int found = result.indexOf(SingleSubscriber::owner, candidate.owner());
    if (found == -1) {
      return true;
    }

    QoS candidateQos = candidate.qos();
    SingleSubscriber exist = result.get(found);
    QoS existeQos = exist.qos();

    if (existeQos.ordinal() < candidateQos.ordinal()) {
      result.remove(found);
      return true;
    }

    return false;
  }
}
