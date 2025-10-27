package javasabr.mqtt.model

import javasabr.mqtt.model.subscriber.SubscribeTopicFilter
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.mqtt.model.topic.TopicName
import javasabr.mqtt.test.support.UnitSpecification
import spock.lang.Shared
import spock.lang.Unroll

import static javasabr.mqtt.model.QoS.*
import static javasabr.mqtt.model.util.TopicUtils.buildTopicFilter
import static javasabr.mqtt.model.util.TopicUtils.buildTopicName

class TopicSubscriberTest extends UnitSpecification {

  @Shared
  MqttUser defaultUser = Mock(MqttUser)
  @Shared
  MqttUser newUser1 = Mock(MqttUser)
  @Shared
  MqttUser newUser2 = Mock(MqttUser)
  @Shared
  MqttUser newUser3 = Mock(MqttUser)

  @Unroll
  def "should choose #matchedQos from #subscriberQos"(
      TopicFilter[] topicFilters,
      TopicName topicNames,
      QoS[] subscriberQos,
      QoS[] matchedQos,
      MqttUser[] users) {
    given:
        SubscribeTopicFilter[] subscribeFilters = new SubscribeTopicFilter[users.length]
        users.eachWithIndex { MqttUser entry, int i ->
          subscribeFilters[i] = new SubscribeTopicFilter(topicFilters[i], subscriberQos[i])
        }
        def topicSubscriber = new TopicSubscribers()
    when:
        topicSubscriber.addSubscriber(users[0], subscribeFilters[0])
        topicSubscriber.addSubscriber(users[1], subscribeFilters[1])
        topicSubscriber.addSubscriber(users[2], subscribeFilters[2])
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
        users << [
            [defaultUser, defaultUser, defaultUser],
            [defaultUser, defaultUser, defaultUser],
            [defaultUser, defaultUser, defaultUser],
            [newUser1, newUser2, newUser3]
        ]
  }
}
