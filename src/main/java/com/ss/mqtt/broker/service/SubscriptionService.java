package com.ss.mqtt.broker.service;

import com.ss.mqtt.broker.model.ActionResult;
import com.ss.mqtt.broker.model.MqttSession;
import com.ss.mqtt.broker.model.SingleSubscriber;
import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.model.reason.code.SubscribeAckReasonCode;
import com.ss.mqtt.broker.model.reason.code.UnsubscribeAckReasonCode;
import com.ss.mqtt.broker.model.topic.TopicFilter;
import com.ss.mqtt.broker.model.topic.TopicName;
import com.ss.mqtt.broker.network.client.MqttClient;
import java.util.function.BiFunction;
import javasabr.rlib.collections.array.Array;

/**
 * Subscription service
 */
public interface SubscriptionService {

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
