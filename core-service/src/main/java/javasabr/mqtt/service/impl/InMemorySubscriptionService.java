package javasabr.mqtt.service.impl;

import static javasabr.mqtt.model.SubscribeRetainHandling.SEND;
import static javasabr.mqtt.model.SubscribeRetainHandling.SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST;
import static javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode.NO_SUBSCRIPTION_EXISTED;
import static javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode.SUCCESS;

import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.SubscribeRetainHandling;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode;
import javasabr.mqtt.model.session.ActiveSubscriptions;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.subscriber.Subscriber;
import javasabr.mqtt.model.subscriber.tree.ConcurrentSubscriberTree;
import javasabr.mqtt.model.subscription.Subscription;
import javasabr.mqtt.model.topic.SharedTopicFilter;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.service.RetainMessageService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

/**
 * In memory subscription service based on {@link ConcurrentSubscriberTree}
 */
@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InMemorySubscriptionService implements SubscriptionService {

  RetainMessageService retainMessageService;
  ConcurrentSubscriberTree subscriberTree;

  public InMemorySubscriptionService(RetainMessageService retainMessageService) {
    this.subscriberTree = new ConcurrentSubscriberTree();
    this.retainMessageService = retainMessageService;
  }

  @Override
  public Array<SingleSubscriber> findSubscribersTo(MutableArray<SingleSubscriber> container, TopicName topicName) {
    Array<SingleSubscriber> matched = subscriberTree.matches(topicName);
    container.addAll(matched);
    return container;
  }

  @Override
  public Array<SubscribeAckReasonCode> subscribe(
      MqttUser user,
      MqttSession session,
      Array<Subscription> subscriptions) {

    MutableArray<SubscribeAckReasonCode> subscribeResults = ArrayFactory.mutableArray(
        SubscribeAckReasonCode.class,
        subscriptions.size());

    for (Subscription subscription : subscriptions) {
      subscribeResults.add(addSubscription(user, session, subscription));
    }

    return subscribeResults;
  }

  private SubscribeAckReasonCode addSubscription(MqttUser user, MqttSession session, Subscription subscription) {
    MqttClientConnectionConfig connectionConfig = user.connectionConfig();
    TopicFilter topicFilter = subscription.topicFilter();
    if (topicFilter.isInvalid()) {
      return SubscribeAckReasonCode.TOPIC_FILTER_INVALID;
    } else if (!connectionConfig.sharedSubscriptionAvailable() && topicFilter instanceof SharedTopicFilter) {
      return SubscribeAckReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED;
    } else if (!connectionConfig.wildcardSubscriptionAvailable() && topicFilter.wildcard()) {
      return SubscribeAckReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED;
    }
    ActiveSubscriptions activeSubscriptions = session.activeSubscriptions();
    SingleSubscriber newSubscriber = new SingleSubscriber(user, subscription);
    SingleSubscriber previousSubscriber = subscriberTree.subscribe(newSubscriber);
    if (previousSubscriber != null) {
      activeSubscriptions.remove(previousSubscriber.subscription());
    }
    sendRetainedMessages(newSubscriber, previousSubscriber);
    activeSubscriptions.add(subscription);
    return subscription.qos().subscribeAckReasonCode();
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
      session.activeSubscriptions().removeByTopicFilter(topicFilter);
      return SUCCESS;
    } else {
      return NO_SUBSCRIPTION_EXISTED;
    }
  }

  @Override
  public void cleanSubscriptions(MqttUser user, MqttSession session) {
    Array<Subscription> subscriptions = session.activeSubscriptions().subscriptions();
    for (Subscription subscription : subscriptions) {
      subscriberTree.unsubscribe(user, subscription.topicFilter());
    }
  }

  @Override
  public void restoreSubscriptions(MqttUser user, MqttSession session) {
    Array<Subscription> subscriptions = session.activeSubscriptions().subscriptions();
    for (Subscription subscription : subscriptions) {
      SingleSubscriber singleSubscriber = new SingleSubscriber(user, subscription);
      subscriberTree.subscribe(singleSubscriber);
    }
  }

  private static boolean isRetainHandlingSatisfied(Subscription subscription, @Nullable Subscriber previousSubscriber) {
    SubscribeRetainHandling retainHandling = subscription.retainHandling();
    return retainHandling == SEND || (retainHandling == SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST
                                          && previousSubscriber == null);
  }

  private void sendRetainedMessages(Subscriber newSubscriber, @Nullable Subscriber previousSubscriber) {
    Subscription subscription = newSubscriber.resolveSingle().subscription();
    if (!subscription.qos().isValid() || !isRetainHandlingSatisfied(subscription, previousSubscriber)) {
      return;
    }
    int count = 0;
    String clientId = newSubscriber.resolveSingle().user().clientId();
    var results = retainMessageService.deliverRetainedMessages(newSubscriber);
    for (PublishHandlingResult result : results) {
      PublishHandlingResult errorResult = null;
      if (result.error()) {
        errorResult = result;
      } else if (result == PublishHandlingResult.SUCCESS) {
        count++;
      }
      if (errorResult != null) {
        log.debug(clientId, errorResult, "[%s] Error occurred [%s] during sending retained messages"::formatted);
      } else {
        log.debug(clientId, count, "[%s] Delivering of [%s] retained message has been started"::formatted);
      }
    }
  }
}
