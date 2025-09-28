package javasabr.mqtt.legacy.service.impl;

import static javasabr.mqtt.model.ActionResult.EMPTY;
import static javasabr.mqtt.model.ActionResult.FAILED;
import static javasabr.mqtt.model.reason.code.SubscribeAckReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED;
import static javasabr.mqtt.model.reason.code.SubscribeAckReasonCode.UNSPECIFIED_ERROR;
import static javasabr.mqtt.model.reason.code.SubscribeAckReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED;
import static javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode.NO_SUBSCRIPTION_EXISTED;
import static javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode.SUCCESS;
import static javasabr.mqtt.model.utils.TopicUtils.hasWildcard;
import static javasabr.mqtt.model.utils.TopicUtils.isInvalid;
import static javasabr.mqtt.model.utils.TopicUtils.isShared;

import javasabr.mqtt.model.MqttConnectionConfig;
import javasabr.mqtt.model.ActionResult;
import javasabr.mqtt.network.MqttSession;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.subscriber.SubscribeTopicFilter;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.model.topic.TopicSubscribers;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.legacy.service.SubscriptionService;
import java.util.function.BiFunction;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayCollectors;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

/**
 * Simple subscription service
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SimpleSubscriptionService implements SubscriptionService {

  TopicSubscribers topicSubscribers = new TopicSubscribers();

  @Override
  public <A> ActionResult forEachTopicSubscriber(
      TopicName topicName,
      A arg1,
      BiFunction<SingleSubscriber, A, ActionResult> action) {
    if (isInvalid(topicName)) {
      return FAILED;
    }
    ActionResult result = EMPTY;
    for (var subscriber : topicSubscribers.matches(topicName)) {
      result = result.and(action.apply(subscriber, arg1));
    }
    return result;
  }

  @Override
  public Array<SubscribeAckReasonCode> subscribe(MqttClient mqttClient, Array<SubscribeTopicFilter> topicFilters) {
    return topicFilters
        .stream()
        .map(topicFilter -> addSubscription(topicFilter, mqttClient))
        .collect(ArrayCollectors.toArray(SubscribeAckReasonCode.class));
  }

  @Nullable
  private SubscribeAckReasonCode addSubscription(SubscribeTopicFilter subscribe, MqttClient client) {
    MqttSession session = client.getSession();
    if (session == null) {
      return null;
    }

    MqttConnectionConfig config = client.getConnectionConfig();
    TopicFilter topic = subscribe.getTopicFilter();

    if (!config.isSharedSubscriptionAvailable() && isShared(topic)) {
      return SHARED_SUBSCRIPTIONS_NOT_SUPPORTED;
    } else if (!config.isWildcardSubscriptionAvailable() && hasWildcard(topic)) {
      return WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED;
    } else if (isInvalid(topic)) {
      return UNSPECIFIED_ERROR;
    } else {
      session.addSubscriber(subscribe);
      topicSubscribers.addSubscriber(client, subscribe);
      return subscribe
          .getQos()
          .getSubscribeAckReasonCode();
    }
  }

  @Override
  public Array<UnsubscribeAckReasonCode> unsubscribe(MqttClient mqttClient, Array<TopicFilter> topicFilters) {
    return topicFilters
        .stream()
        .map(topicFilter -> removeSubscription(topicFilter, mqttClient))
        .collect(ArrayCollectors.toArray(UnsubscribeAckReasonCode.class));
  }

  @Nullable
  private UnsubscribeAckReasonCode removeSubscription(TopicFilter topic, MqttClient client) {
    var session = client.getSession();
    if (session == null) {
      return null;
    } else if (isInvalid(topic)) {
      return UnsubscribeAckReasonCode.UNSPECIFIED_ERROR;
    } else if (topicSubscribers.removeSubscriber(client, topic)) {
      session.removeSubscriber(topic);
      return SUCCESS;
    } else {
      return NO_SUBSCRIPTION_EXISTED;
    }
  }

  public void cleanSubscriptions(MqttClient mqttClient, MqttSession mqttSession) {
    mqttSession.forEachTopicFilter(topicSubscribers, mqttClient, TopicSubscribers::removeSubscriber);
  }

  public void restoreSubscriptions(MqttClient mqttClient, MqttSession mqttSession) {
    mqttSession.forEachTopicFilter(topicSubscribers, mqttClient, TopicSubscribers::addSubscriber);
  }
}
