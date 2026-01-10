package javasabr.mqtt.broker.application.service

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5SubAckException
import javasabr.mqtt.broker.application.IntegrationSpecification
import javasabr.mqtt.model.topic.TopicName
import javasabr.mqtt.network.user.NetworkMqttUser
import javasabr.mqtt.service.ClientIdRegistry
import javasabr.mqtt.service.impl.InMemorySubscriptionService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

import java.util.concurrent.CompletionException

class SubscriptionServiceTest extends IntegrationSpecification {

  @Autowired
  ClientIdRegistry clientIdRegistry

  @Autowired
  InMemorySubscriptionService subscriptionService

  def "should clear/restore topic subscribers after disconnect/reconnect"() {
    given:
        def subscriber = buildExternalMqtt5Client(clientId)
        def topicName = TopicName.valueOf(topicFilter)
    when:
        subscriber.connectWith()
            .cleanStart(true)
            .sessionExpiryInterval(120)
            .send()
            .join()
        subscriber.subscribeWith()
            .topicFilter(topicFilter)
            .qos(MqttQos.AT_MOST_ONCE)
            .send()
            .join()
        def subscribers = subscriptionService
            .findSubscribers(topicName)
    then: "should find the subscriber"
        subscribers.size() == 1
        NetworkMqttUser.isCase(subscribers.get(0).user())
    when:
        def matchedSubscriber = subscribers.get(0)
        def subscription = matchedSubscriber.subscription()
        def owner = matchedSubscriber.user() as NetworkMqttUser
    then:
        owner.clientId() == clientId
        subscription.topicFilter().rawTopic() == topicFilter
    when:
        subscriber.disconnect().join()
        def subscribers2 = subscriptionService
            .findSubscribers(topicName)
    then: "shot not find anything after disconnection"
        subscribers2.size() == 0
    when:
        subscriber.connectWith()
            .cleanStart(false)
            .send()
            .join()
        def subscribers3 = subscriptionService
            .findSubscribers(topicName)
    then: "should find the reconnected subscriber"
        subscribers3.size() == 1
        NetworkMqttUser.isCase(subscribers3.get(0).user())
    when:
        matchedSubscriber = subscribers3.get(0)
        subscription = matchedSubscriber.subscription()
        owner = matchedSubscriber.user() as NetworkMqttUser
    then:
        owner.clientId() == clientId
        subscription.topicFilter().rawTopic() == topicFilter
    cleanup:
        subscriber.disconnect().join()
  }

  @Unroll
  def "should match subscriber with the highest QoS"(
      String topicName,
      String topicFilter1,
      MqttQos qos1,
      String topicFilter2,
      MqttQos qos2,
      String expectedTopicFilter) {
    given:
        def subscriber = buildExternalMqtt5Client()
        subscriber.connectWith()
            .send()
            .join()
        subscriber.subscribeWith()
            .topicFilter(topicFilter1)
            .qos(qos1)
            .send()
            .join()
        subscriber.subscribeWith()
            .topicFilter(topicFilter2)
            .qos(qos2)
            .send()
            .join()
    when:
        def subscribers = subscriptionService
            .findSubscribers(TopicName.valueOf(topicName))
    then:
        subscribers.size() == 1
        subscribers.get(0).subscription().topicFilter().rawTopic() == expectedTopicFilter
    cleanup:
        subscriber.disconnect().join()
    where:
        topicName            | topicFilter1    | qos1                 | topicFilter2 | qos2                  | expectedTopicFilter
        "topic/Filter"       | "topic/Filter"  | MqttQos.AT_MOST_ONCE | "topic/#"    | MqttQos.AT_LEAST_ONCE | "topic/#"
        "topic/Filter"       | "topic/Filter"  | MqttQos.EXACTLY_ONCE | "topic/#"    | MqttQos.AT_LEAST_ONCE | "topic/Filter"
        "topic/Another"      | "topic/Filter"  | MqttQos.EXACTLY_ONCE | "topic/#"    | MqttQos.AT_LEAST_ONCE | "topic/#"
        "topic/Filter/First" | "topic/+/First" | MqttQos.AT_MOST_ONCE | "topic/#"    | MqttQos.AT_LEAST_ONCE | "topic/#"
        "topic/Filter/First" | "topic/+/First" | MqttQos.EXACTLY_ONCE | "topic/#"    | MqttQos.AT_LEAST_ONCE | "topic/+/First"
  }

  @Unroll
  def "should match all subscribers with shared and single topic"(
      String topicName,
      String topicFilter1,
      MqttQos qos1,
      String topicFilter2,
      MqttQos qos2,
      String targetTopicFilter,
      int targetCount) {
    given:
        def clientId1 = clientIdRegistry.generate().block()
        def clientId2 = clientIdRegistry.generate().block()
        def subscriber1 = buildExternalMqtt5Client(clientId1)
        def subscriber2 = buildExternalMqtt5Client(clientId2)
        subscriber1.connectWith()
            .send()
            .join()
        subscriber2.connectWith()
            .send()
            .join()
        subscriber1.subscribeWith()
            .topicFilter(topicFilter1)
            .qos(qos1)
            .send()
            .join()
        subscriber2.subscribeWith()
            .topicFilter(topicFilter2)
            .qos(qos2)
            .send()
            .join()
    when:
        def subscribers = subscriptionService.findSubscribers(TopicName.valueOf(topicName))
    then:
        subscribers.size() == targetCount
        (subscribers[0].user() as NetworkMqttUser).clientId() == clientId1
        (subscribers[1].user() as NetworkMqttUser).clientId() == clientId2
    cleanup:
        subscriber1.disconnect().join()
        subscriber2.disconnect().join()
    where:
        topicName            | topicFilter1                  | qos1                 | topicFilter2             | qos2                  | targetTopicFilter | targetCount
        "topic/Filter"       | "\$share/group1/topic/Filter" | MqttQos.AT_MOST_ONCE | "\$share/group2/topic/#" | MqttQos.AT_LEAST_ONCE | "topic/#"         | 2
        "topic/Filter"       | "\$share/group1/topic/Filter" | MqttQos.EXACTLY_ONCE | "topic/#"                | MqttQos.AT_LEAST_ONCE | "topic/Filter"    | 2
        "topic/Filter/First" | "topic/+/First"               | MqttQos.AT_MOST_ONCE | "\$share/group2/topic/#" | MqttQos.AT_LEAST_ONCE | "topic/#"         | 2
        "topic/Filter/First" | "topic/+/First"               | MqttQos.EXACTLY_ONCE | "topic/#"                | MqttQos.AT_LEAST_ONCE | "topic/+/First"   | 2
  }

  @Unroll
  def "should reject subscribe with wrong topic filter"(String wrongTopicFilter, Class<Throwable> exception) {
    given:
        def subscriber = buildExternalMqtt5Client()
    when:
        subscriber.connectWith()
            .send()
            .join()
        subscriber.subscribeWith()
            .topicFilter(wrongTopicFilter)
            .send()
            .join()
    then:
        def ex = thrown exception
        if (ex.cause != null) {
          ex.cause.class == Mqtt5SubAckException
          ex.cause.message == "SUBACK contains only Error Codes"
        }
    cleanup:
        subscriber.disconnect().join()
    where:
        wrongTopicFilter       | exception
        "\$sys/topic/"         | CompletionException
        "topic//Filter"        | CompletionException
        "/topic/\u0000Another" | IllegalArgumentException
        "topic/##"             | IllegalArgumentException
        "++/Filter/First"      | IllegalArgumentException
  }
}
