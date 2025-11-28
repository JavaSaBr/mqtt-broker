package javasabr.mqtt.service.impl;

import static javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode.NO_SUBSCRIPTION_EXISTED;
import static javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode.SUCCESS;

import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode;
import javasabr.mqtt.model.session.ActiveSubscriptions;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.subscriber.Subscriber;
import javasabr.mqtt.model.subscriber.tree.ConcurrentSubscriberTree;
import javasabr.mqtt.model.subscription.Subscription;
import javasabr.mqtt.model.topic.SharedTopicFilter;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.session.MqttNetworkSession;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;

/**
 * In memory subscription service based on {@link ConcurrentSubscriberTree}
 */
@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InMemorySubscriptionService implements SubscriptionService {

  ConcurrentSubscriberTree subscriberTree;

  public InMemorySubscriptionService() {
    this.subscriberTree = new ConcurrentSubscriberTree();
  }

  @Override
  public MqttClient resolveClient(Subscriber subscriber) {
    if (subscriber instanceof SingleSubscriber single) {
      return (MqttClient) single.user();
    }
    throw new IllegalArgumentException("Unexpected subscriber: " + subscriber);
  }

  @Override
  public Array<SingleSubscriber> findSubscribersTo(MutableArray<SingleSubscriber> container, TopicName topicName) {
    Array<SingleSubscriber> matched = subscriberTree.matches(topicName);
    container.addAll(matched);
    return container;
  }

  @Override
  public Array<SubscribeAckReasonCode> subscribe(
      MqttClient client,
      MqttNetworkSession session,
      Array<Subscription> subscriptions) {

    MutableArray<SubscribeAckReasonCode> subscribeResults = ArrayFactory.mutableArray(
        SubscribeAckReasonCode.class,
        subscriptions.size());

    for (Subscription subscription : subscriptions) {
      subscribeResults.add(addSubscription(client, session, subscription));
    }

    return subscribeResults;
  }

  private SubscribeAckReasonCode addSubscription(MqttClient client, MqttNetworkSession session, Subscription subscription) {
    MqttClientConnectionConfig connectionConfig = client.connectionConfig();
    TopicFilter topicFilter = subscription.topicFilter();
    if (topicFilter.isInvalid()) {
      return SubscribeAckReasonCode.TOPIC_FILTER_INVALID;
    } else if (!connectionConfig.sharedSubscriptionAvailable() && topicFilter instanceof SharedTopicFilter) {
      return SubscribeAckReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED;
    } else if (!connectionConfig.wildcardSubscriptionAvailable() && topicFilter.wildcard()) {
      return SubscribeAckReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED;
    }
    ActiveSubscriptions activeSubscriptions = session.activeSubscriptions();
    SingleSubscriber previous = subscriberTree.subscribe(client, subscription);
    if (previous != null) {
      activeSubscriptions.remove(previous.subscription());
    }
    activeSubscriptions.add(subscription);
    return subscription.qos().subscribeAckReasonCode();
  }

  @Override
  public Array<UnsubscribeAckReasonCode> unsubscribe(
      MqttClient client,
      MqttNetworkSession session,
      Array<TopicFilter> topicFilters) {

    MutableArray<UnsubscribeAckReasonCode> unsubscribeResults = ArrayFactory.mutableArray(
        UnsubscribeAckReasonCode.class,
        topicFilters.size());

    for (TopicFilter topicFilter : topicFilters) {
      unsubscribeResults.add(removeSubscription(client, session, topicFilter));
    }

    return unsubscribeResults;
  }

  private UnsubscribeAckReasonCode removeSubscription(MqttClient client, MqttNetworkSession session, TopicFilter topicFilter) {
    if (topicFilter.isInvalid()) {
      return UnsubscribeAckReasonCode.TOPIC_FILTER_INVALID;
    } else if (subscriberTree.unsubscribe(client, topicFilter)) {
      session
          .activeSubscriptions()
          .removeByTopicFilter(topicFilter);
      return SUCCESS;
    } else {
      return NO_SUBSCRIPTION_EXISTED;
    }
  }

  @Override
  public void cleanSubscriptions(MqttClient client, MqttNetworkSession session) {
    Array<Subscription> subscriptions = session
        .activeSubscriptions()
        .subscriptions();
    for (Subscription subscription : subscriptions) {
      subscriberTree.unsubscribe(client, subscription.topicFilter());
    }
  }

  @Override
  public void restoreSubscriptions(MqttClient client, MqttNetworkSession session) {
    Array<Subscription> subscriptions = session
        .activeSubscriptions()
        .subscriptions();
    for (Subscription subscription : subscriptions) {
      subscriberTree.subscribe(client, subscription);
    }
  }
}
