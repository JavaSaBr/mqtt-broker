package javasabr.mqtt.service.impl;

import static javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode.NO_SUBSCRIPTION_EXISTED;
import static javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode.SUCCESS;

import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.subscriber.Subscriber;
import javasabr.mqtt.model.subscribtion.Subscription;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.model.topic.tree.TopicTree;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.MqttSession;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.TopicService;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;

/**
 * Simple subscription service
 */
@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SimpleSubscriptionService implements SubscriptionService {

  TopicService topicService;
  TopicTree topicTree;

  public SimpleSubscriptionService(TopicService topicService) {
    this.topicService = topicService;
    this.topicTree = new TopicTree();
  }

  @Override
  public MqttClient resolveClient(Subscriber subscriber) {
    if (subscriber instanceof SingleSubscriber single) {
      return (MqttClient) single.owner();
    }
    throw new IllegalArgumentException("Unexpected subscriber: " + subscriber);
  }

  @Override
  public Array<SingleSubscriber> findSubscribersTo(MutableArray<SingleSubscriber> container, TopicName topicName) {
    Array<SingleSubscriber> matched = topicTree.matches(topicName);
    container.addAll(matched);
    return container;
  }

  @Override
  public Array<SubscribeAckReasonCode> subscribe(
      MqttClient client,
      Array<Subscription> subscriptions) {

    MutableArray<SubscribeAckReasonCode> result = ArrayFactory.mutableArray(
        SubscribeAckReasonCode.class,
        subscriptions.size());

    MqttSession session = client.session();
    if (session == null) {
      // without session just fill error for each topic filter
      log.warning(client.clientId(), "[%s] Cannot add subscription for client without session"::formatted);
      for (int i = 0, length = subscriptions.size(); i < length; i++) {
        result.add(SubscribeAckReasonCode.UNSPECIFIED_ERROR);
      }
      return result;
    }

    for (Subscription subscription : subscriptions) {
      result.add(addSubscription(client, session, subscription));
    }

    return result;
  }

  private SubscribeAckReasonCode addSubscription(MqttClient client, MqttSession session, Subscription subscription) {
    MqttClientConnectionConfig connectionConfig = client.connectionConfig();
    TopicFilter topicFilter = subscription.topicFilter();
    if (topicService.isInvalid(topicFilter)) {
      return SubscribeAckReasonCode.TOPIC_FILTER_INVALID;
    } else if (!connectionConfig.sharedSubscriptionAvailable() && topicService.isShared(topicFilter)) {
      return SubscribeAckReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED;
    } else if (!connectionConfig.wildcardSubscriptionAvailable() && topicService.hasWildcard(topicFilter)) {
      return SubscribeAckReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED;
    }
    session.storeSubscription(subscription);
    topicTree.subscribe(client, subscription);
    return subscription.qos().subscribeAckReasonCode();
  }

  @Override
  public Array<UnsubscribeAckReasonCode> unsubscribe(MqttClient client, Array<TopicFilter> topicFilters) {

    MutableArray<UnsubscribeAckReasonCode> result = ArrayFactory.mutableArray(
        UnsubscribeAckReasonCode.class,
        topicFilters.size());

    MqttSession session = client.session();
    if (session == null) {
      // without session just fill error for each topic filter
      log.warning(client.clientId(), "[%s] Cannot add subscription for client without session"::formatted);
      for (int i = 0, length = topicFilters.size(); i < length; i++) {
        result.add(UnsubscribeAckReasonCode.UNSPECIFIED_ERROR);
      }
      return result;
    }

    for (TopicFilter topicFilter : topicFilters) {
      result.add(removeSubscription(client, session, topicFilter));
    }

    return result;
  }

  private UnsubscribeAckReasonCode removeSubscription(MqttClient client, MqttSession session, TopicFilter topicFilter) {
    if (topicService.isInvalid(topicFilter)) {
      return UnsubscribeAckReasonCode.TOPIC_FILTER_INVALID;
    } else if (topicTree.unsubscribe(client, topicFilter)) {
      session.removeSubscription(topicFilter);
      return SUCCESS;
    } else {
      return NO_SUBSCRIPTION_EXISTED;
    }
  }

  @Override
  public void cleanSubscriptions(MqttClient client, MqttSession session) {
    Array<Subscription> subscriptions = session.storedSubscriptions();
    for (Subscription subscription : subscriptions) {
      topicTree.unsubscribe(client, subscription.topicFilter());
    }
  }

  @Override
  public void restoreSubscriptions(MqttClient client, MqttSession session) {
    Array<Subscription> subscriptions = session.storedSubscriptions();
    for (Subscription subscription : subscriptions) {
      topicTree.subscribe(client, subscription);
    }
  }
}
