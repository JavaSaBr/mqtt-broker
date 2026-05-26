package javasabr.mqtt.service.impl;

import static javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode.NO_SUBSCRIPTION_EXISTED;
import static javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode.SUCCESS;

import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode;
import javasabr.mqtt.model.session.ActiveSubscriptions;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.subscriber.tree.ConcurrentSubscriberTree;
import javasabr.mqtt.model.subscription.Subscription;
import javasabr.mqtt.model.subscription.SubscriptionResult;
import javasabr.mqtt.model.topic.SharedTopicFilter;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.service.AuthorizationService;
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

  private static final SubscriptionResult INVALID_TOPIC_FILTER_RESULT =
      new SubscriptionResult(SubscribeAckReasonCode.TOPIC_FILTER_INVALID);
  private static final SubscriptionResult SHARED_SUBSCRIPTION_NOT_SUPPORTED_RESULT =
      new SubscriptionResult(SubscribeAckReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED);
  private static final SubscriptionResult WILDCARD_SUBSCRIPTION_NOT_SUPPORTED_RESULT =
      new SubscriptionResult(SubscribeAckReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED);

  ConcurrentSubscriberTree subscriberTree;
  AuthorizationService authorizationService;

  public InMemorySubscriptionService(AuthorizationService authorizationService) {
    this.authorizationService = authorizationService;
    this.subscriberTree = new ConcurrentSubscriberTree();
  }

  @Override
  public Array<SingleSubscriber> findSubscribersTo(MutableArray<SingleSubscriber> container, TopicName topicName) {
    Array<SingleSubscriber> matched = subscriberTree.matches(topicName);
    container.addAll(matched);
    return container;
  }

  @Override
  public Array<SubscriptionResult> subscribe(
      MqttUser user,
      MqttSession session,
      Array<Subscription> subscriptions) {

    MutableArray<SubscriptionResult> subscribeResults = ArrayFactory.mutableArray(
        SubscriptionResult.class,
        subscriptions.size());

    for (Subscription subscription : subscriptions) {
      subscribeResults.add(addSubscription(user, session, subscription));
    }

    return subscribeResults;
  }

  private SubscriptionResult addSubscription(MqttUser user, MqttSession session, Subscription newSubscription) {
    MqttClientConnectionConfig connectionConfig = user.connectionConfig();
    TopicFilter topicFilter = newSubscription.topicFilter();
    if (topicFilter.isInvalid()) {
      return INVALID_TOPIC_FILTER_RESULT;
    } else if (!connectionConfig.sharedSubscriptionAvailable() && topicFilter instanceof SharedTopicFilter) {
      return SHARED_SUBSCRIPTION_NOT_SUPPORTED_RESULT;
    } else if (!connectionConfig.wildcardSubscriptionAvailable() && topicFilter.wildcard()) {
      return WILDCARD_SUBSCRIPTION_NOT_SUPPORTED_RESULT;
    } else if (!authorizationService.authorizeSubscribe(user, topicFilter)) {
      log.warn(user.clientId(), topicFilter, "[%s] Not authorized for subscribing to:[%s]"::formatted);
      return new SubscriptionResult(SubscribeAckReasonCode.NOT_AUTHORIZED, null, null);
    }
    ActiveSubscriptions activeSubscriptions = session.activeSubscriptions();
    SingleSubscriber previousSubscriber = subscriberTree.subscribe(user, newSubscription);
    Subscription previousSubscription = null;
    if (previousSubscriber != null) {
      previousSubscription = previousSubscriber.subscription();
      activeSubscriptions.remove(previousSubscription);
    }
    activeSubscriptions.add(newSubscription);
    return new SubscriptionResult(newSubscription, previousSubscription);
  }

  @Override
  public Array<UnsubscribeAckReasonCode> unsubscribe(
      MqttUser user,
      MqttSession session,
      Array<TopicFilter> topicFilters) {

    MutableArray<UnsubscribeAckReasonCode> unsubscribeResults = ArrayFactory.mutableArray(
        UnsubscribeAckReasonCode.class,
        topicFilters.size());

    for (TopicFilter topicFilter : topicFilters) {
      unsubscribeResults.add(removeSubscription(user, session, topicFilter));
    }

    return unsubscribeResults;
  }

  private UnsubscribeAckReasonCode removeSubscription(MqttUser user, MqttSession session, TopicFilter topicFilter) {
    if (topicFilter.isInvalid()) {
      return UnsubscribeAckReasonCode.TOPIC_FILTER_INVALID;
    } else if (subscriberTree.unsubscribe(user, topicFilter)) {
      session
          .activeSubscriptions()
          .removeByTopicFilter(topicFilter);
      return SUCCESS;
    } else {
      return NO_SUBSCRIPTION_EXISTED;
    }
  }

  @Override
  public void cleanSubscriptions(MqttUser user, MqttSession session) {
    Array<Subscription> subscriptions = session
        .activeSubscriptions()
        .subscriptions();
    for (Subscription subscription : subscriptions) {
      subscriberTree.unsubscribe(user, subscription.topicFilter());
    }
  }

  @Override
  public void restoreSubscriptions(MqttUser user, MqttSession session) {
    Array<Subscription> subscriptions = session
        .activeSubscriptions()
        .subscriptions();
    for (Subscription subscription : subscriptions) {
      subscriberTree.subscribe(user, subscription);
    }
  }
}
