package javasabr.mqtt.service;

import java.util.function.BiFunction;
import javasabr.mqtt.model.ActionResult;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.subscriber.SubscribeTopicFilter;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.MqttSession;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.MutableArray;

/**
 * Subscription service
 */
public interface SubscriptionService {

  boolean isValid(TopicName topicName);

  default Array<SingleSubscriber> findSubscribers(TopicName topicName) {
    return findSubscribersTo(MutableArray.ofType(SingleSubscriber.class), topicName);
  }

  Array<SingleSubscriber> findSubscribersTo(MutableArray<SingleSubscriber> container, TopicName topicName);

  /**
   * Runs function for each topic subscriber
   *
   * @param topicName topic name
   * @param argument additional argument
   * @param action function to run
   * @return {@link ActionResult} of function
   */
  <A> ActionResult forEachTopicSubscriber(
      TopicName topicName,
      A argument,
      BiFunction<SingleSubscriber, A, ActionResult> action);

  /**
   * Adds MQTT client to topic filter subscribers
   *
   * @param mqttClient MQTT client to be added
   * @param topicFilters topic filters
   * @return array of subscribe ack reason codes
   */
  Array<SubscribeAckReasonCode> subscribe(
      MqttClient mqttClient,
      Array<SubscribeTopicFilter> topicFilters);

  /**
   * Removes MQTT client from subscribers by array of topic names
   *
   * @param mqttClient MQTT client to be removed
   * @param topicFilters topic filters
   * @return array of unsubscribe ack reason codes
   */
  Array<UnsubscribeAckReasonCode> unsubscribe(
      MqttClient mqttClient,
      Array<TopicFilter> topicFilters);

  void cleanSubscriptions(MqttClient mqttClient, MqttSession mqttSession);

  void restoreSubscriptions(MqttClient mqttClient, MqttSession mqttSession);
}
