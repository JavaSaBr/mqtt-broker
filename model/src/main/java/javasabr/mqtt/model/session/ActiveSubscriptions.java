package javasabr.mqtt.model.session;

import javasabr.mqtt.model.subscription.Subscription;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.rlib.collections.array.Array;

public interface ActiveSubscriptions {

  void add(Subscription subscription);

  void remove(Subscription subscription);

  void removeByTopicFilter(TopicFilter topicFilter);

  Array<Subscription> subscriptions();

  Array<Subscription> findBySubscriptionId(int subscriptionId);
}
