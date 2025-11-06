package javasabr.mqtt.service;

import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.subscriber.Subscriber;
import javasabr.mqtt.model.subscribtion.Subscription;
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

  MqttClient resolveClient(Subscriber subscriber);

  default Array<SingleSubscriber> findSubscribers(TopicName topicName) {
    return findSubscribersTo(MutableArray.ofType(SingleSubscriber.class), topicName);
  }

  Array<SingleSubscriber> findSubscribersTo(MutableArray<SingleSubscriber> container, TopicName topicName);

  /**
   * Subscribes MQTT client to listen to topics.
   *
   * @param client MQTT client which requests subscriptions
   * @param subscriptions the list of request to subscribe topics
   * @return array of subscribe ack reason codes
   */
  Array<SubscribeAckReasonCode> subscribe(MqttClient client, Array<Subscription> subscriptions);

  /**
   * Removes MQTT client from listening to the topics.
   *
   * @param client MQTT client to be removed
   * @param topicFilters topic filters
   * @return array of unsubscribe ack reason codes
   */
  Array<UnsubscribeAckReasonCode> unsubscribe(MqttClient client, Array<TopicFilter> topicFilters);

  void cleanSubscriptions(MqttClient client, MqttSession session);

  void restoreSubscriptions(MqttClient client, MqttSession session);
}
