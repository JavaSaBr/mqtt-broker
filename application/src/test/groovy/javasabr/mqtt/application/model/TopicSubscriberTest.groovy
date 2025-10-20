package javasabr.mqtt.application.model

import javasabr.mqtt.application.network.NetworkUnitSpecification
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.subscriber.SubscribeTopicFilter
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.mqtt.model.topic.TopicName
import javasabr.mqtt.model.topic.TopicSubscribers
import javasabr.mqtt.network.MqttClient
import spock.lang.Unroll

import static javasabr.mqtt.model.QoS.*
import static javasabr.mqtt.model.utils.TopicUtils.buildTopicFilter
import static javasabr.mqtt.model.utils.TopicUtils.buildTopicName

class TopicSubscriberTest extends NetworkUnitSpecification {

  @Unroll
  def "should choose #matchedQos from #subscriberQos"(
      TopicFilter[] topicFilters,
      TopicName topicNames,
      QoS[] subscriberQos,
      QoS[] matchedQos,
      MqttClient[] mqttClients) {
    given:
        SubscribeTopicFilter[] subscribeFilters = new SubscribeTopicFilter[mqttClients.length]
        mqttClients.eachWithIndex { MqttClient entry, int i ->
          subscribeFilters[i] = new SubscribeTopicFilter(topicFilters[i], subscriberQos[i])
        }
        def topicSubscriber = new TopicSubscribers()
    when:
        topicSubscriber.addSubscriber(mqttClients[0], subscribeFilters[0])
        topicSubscriber.addSubscriber(mqttClients[1], subscribeFilters[1])
        topicSubscriber.addSubscriber(mqttClients[2], subscribeFilters[2])
    then:
        def subscribers = topicSubscriber.matches(topicNames)
        subscribers.size() == matchedQos.size()
        for (int i = 0; i < subscribers.size(); i++) {
          subscribers[i].qos == matchedQos[i]
        }
    where:
        topicFilters << [
            [buildTopicFilter("topic/second/in"), buildTopicFilter("topic/+/in"), buildTopicFilter("topic/#")],
            [buildTopicFilter("topic/+/in"), buildTopicFilter("topic/first/in"), buildTopicFilter("topic/out")],
            [buildTopicFilter("topic/second/in"), buildTopicFilter("topic/first/in"), buildTopicFilter("topic/out")],
            [buildTopicFilter("topic/second/in"), buildTopicFilter("topic/+/in"), buildTopicFilter("topic/#")]
        ]
        topicNames << [
            buildTopicName("topic/second/in"),
            buildTopicName("topic/first/in"),
            buildTopicName("topic/second/in"),
            buildTopicName("topic/second/in")
        ]
        subscriberQos << [
            [AT_LEAST_ONCE, AT_MOST_ONCE, EXACTLY_ONCE],
            [AT_LEAST_ONCE, AT_MOST_ONCE, EXACTLY_ONCE],
            [AT_LEAST_ONCE, AT_MOST_ONCE, EXACTLY_ONCE],
            [AT_LEAST_ONCE, AT_MOST_ONCE, EXACTLY_ONCE]
        ]
        matchedQos << [
            [EXACTLY_ONCE],
            [AT_MOST_ONCE],
            [AT_LEAST_ONCE],
            [AT_LEAST_ONCE, AT_MOST_ONCE, EXACTLY_ONCE]
        ]
        mqttClients << [
            [defaultMqtt311Client, defaultMqtt311Client, defaultMqtt311Client],
            [defaultMqtt311Client, defaultMqtt311Client, defaultMqtt311Client],
            [defaultMqtt311Client, defaultMqtt311Client, defaultMqtt311Client],
            [newMqtt311Client(), newMqtt311Client(), newMqtt311Client()]
        ]
  }
}
