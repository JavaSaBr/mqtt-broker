package javasabr.mqtt.legacy.model

import javasabr.mqtt.legacy.network.MqttClient
import javasabr.mqtt.legacy.network.NetworkUnitSpecification
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.mqtt.model.topic.TopicName
import javasabr.mqtt.model.topic.TopicSubscribers
import spock.lang.Unroll

import static javasabr.mqtt.model.QoS.AT_LEAST_ONCE
import static javasabr.mqtt.model.QoS.AT_MOST_ONCE
import static javasabr.mqtt.model.QoS.EXACTLY_ONCE
import static javasabr.mqtt.model.utils.TopicUtils.buildTopicFilter
import static javasabr.mqtt.model.utils.TopicUtils.buildTopicName

class TopicSubscriberTest extends NetworkUnitSpecification {

  @Unroll
  def "should choose #matchedQos from #subscriberQos"(
      TopicFilter[] topicFilters,
      TopicName topicNames,
      QoS[] subscriberQos,
      QoS[] matchedQos,
      MqttClient[] mqttClients
  ) {
    given:
        def subscribeTopicFilter = Mock(SubscribeTopicFilter) {
          getQos() >>> subscriberQos
          getTopicFilter() >>> topicFilters
        }
        def topicSubscriber = new TopicSubscribers()
    when:
        topicSubscriber.addSubscriber(mqttClients[0], subscribeTopicFilter)
        topicSubscriber.addSubscriber(mqttClients[1], subscribeTopicFilter)
        topicSubscriber.addSubscriber(mqttClients[2], subscribeTopicFilter)
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
            [defaultMqttClient, defaultMqttClient, defaultMqttClient],
            [defaultMqttClient, defaultMqttClient, defaultMqttClient],
            [defaultMqttClient, defaultMqttClient, defaultMqttClient],
            [defaultMqttClient(), defaultMqttClient(), defaultMqttClient()]
        ]
  }
}
