package javasabr.mqtt.broker.application.service

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5SubAckException
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode
import javasabr.mqtt.broker.application.IntegrationSpecification
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.mqtt.model.topic.TopicName
import javasabr.mqtt.network.user.NetworkMqttUser
import javasabr.mqtt.service.ClientIdRegistry
import javasabr.mqtt.service.impl.InMemorySubscriptionService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

import java.util.concurrent.CompletionException

import static javasabr.mqtt.broker.application.MqttClientFactory.generateClientId

class SubscriptionServiceTest extends IntegrationSpecification {

  @Autowired
  ClientIdRegistry clientIdRegistry

  @Autowired
  InMemorySubscriptionService subscriptionService

  def "should clear/restore topic subscribers after disconnect/reconnect"() {
    given:
        def serviceId = generateClientId("service")
        def serviceName = "SubscriptionServiceTest_1"
        def subscriber = buildExternalMqtt5Client(serviceId)
        def topicName = TopicName.valueOf("service/$serviceName/device/device1")
        def topicFilter = TopicFilter.valueOf("service/$serviceName/device/+")
    when:
        fromAsync(subscriber.connectWith()
            .cleanStart(true)
            .sessionExpiryInterval(120)
            .send())
        fromAsync(subscriber.subscribeWith()
            .topicFilter(topicFilter.rawTopic())
            .qos(MqttQos.AT_MOST_ONCE)
            .send())
        def subscribers = subscriptionService
            .findSubscribers(topicName)
    then: "should find the subscriber"
        subscribers.size() == 1
        def matchedSubscriber = subscribers.get(0)
        matchedSubscriber.user() in NetworkMqttUser
    when:
        def subscription = matchedSubscriber.subscription()
        def owner = matchedSubscriber.user() as NetworkMqttUser
    then:
        owner.clientId() == serviceId
        subscription.topicFilter() == topicFilter
    when:
        fromAsync(subscriber.disconnect())
        def subscribers2 = subscriptionService
            .findSubscribers(topicName)
    then: "shot not find anything after disconnection"
        subscribers2.size() == 0
    when:
        fromAsync(subscriber.connectWith()
            .cleanStart(false)
            .send())
        def subscribers3 = subscriptionService
            .findSubscribers(topicName)
    then: "should find the reconnected subscriber"
        subscribers3.size() == 1
        def matchedSubscriber2 = subscribers3.get(0)
        matchedSubscriber2.user() in NetworkMqttUser
    when:
        def subscription2 = matchedSubscriber2.subscription()
        def owner2 = matchedSubscriber2.user() as NetworkMqttUser
    then:
        owner2.clientId() == serviceId
        subscription2.topicFilter() == topicFilter
    cleanup:
        fromAsync(subscriber.disconnect())
  }

  def "should not allow to subscribe on not allowed by ACL topic"() {
    given:
        def deviceId = generateClientId("device")
        def serviceName = "SubscriptionServiceTest_2"
        def subscriber = buildExternalMqtt5Client(deviceId)
        def allowedTopic = TopicFilter.valueOf("device/$deviceId")
        def notAllowedTopic = TopicFilter.valueOf("service/$serviceName/device/+")
    when:
        fromAsync(subscriber.connectWith()
            .cleanStart(true)
            .sessionExpiryInterval(120)
            .send())
        fromAsync(subscriber.subscribeWith()
            .addSubscription()
              .topicFilter(allowedTopic.rawTopic())
              .qos(MqttQos.EXACTLY_ONCE)
            .applySubscription()
            .addSubscription()
              .topicFilter(notAllowedTopic.rawTopic())
              .qos(MqttQos.AT_LEAST_ONCE)
            .applySubscription()
            .send())
    then:
        def completionEx = thrown CompletionException
        def subAckException = completionEx.cause as Mqtt5SubAckException
        with(subAckException.getMqttMessage()) {
          getReasonCodes().get(0) == Mqtt5SubAckReasonCode.GRANTED_QOS_2
          getReasonCodes().get(1) == Mqtt5SubAckReasonCode.NOT_AUTHORIZED
        }
    cleanup:
        fromAsync(subscriber.disconnect())
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
        def systemId = generateClientId("system")
        def subscriber = buildExternalMqtt5Client(systemId)
        fromAsync(subscriber.connectWith()
            .send())
        fromAsync(subscriber.subscribeWith()
            .topicFilter(topicFilter1)
            .qos(qos1)
            .send())
        fromAsync(subscriber.subscribeWith()
            .topicFilter(topicFilter2)
            .qos(qos2)
            .send())
    when:
        def subscribers = subscriptionService
            .findSubscribers(TopicName.valueOf(topicName))
    then:
        subscribers.size() == 1
        with(subscribers.get(0)) {
          subscription().topicFilter().rawTopic() == expectedTopicFilter
        }
    cleanup:
        fromAsync(subscriber.disconnect())
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
        def systemId1 = generateClientId("system")
        def systemId2 = generateClientId("system")
        def subscriber1 = buildExternalMqtt5Client(systemId1)
        def subscriber2 = buildExternalMqtt5Client(systemId2)
        fromAsync(subscriber1.connectWith().send())
        fromAsync(subscriber2.connectWith().send())
        fromAsync(subscriber1.subscribeWith()
            .topicFilter(topicFilter1)
            .qos(qos1)
            .send())
        fromAsync(subscriber2.subscribeWith()
            .topicFilter(topicFilter2)
            .qos(qos2)
            .send())
    when:
        def subscribers = subscriptionService
            .findSubscribers(TopicName.valueOf(topicName))
    then:
        subscribers.size() == targetCount
        with(subscribers[0].user() as NetworkMqttUser) {
          clientId() == systemId1
        }
        with(subscribers[1].user() as NetworkMqttUser) {
          clientId() == systemId2
        }
    cleanup:
        fromAsync(subscriber1.disconnect())
        fromAsync(subscriber2.disconnect())
    where:
        topicName            | topicFilter1                  | qos1                 | topicFilter2             | qos2                  | targetTopicFilter | targetCount
        "topic/Filter"       | "\$share/group1/topic/Filter" | MqttQos.AT_MOST_ONCE | "\$share/group2/topic/#" | MqttQos.AT_LEAST_ONCE | "topic/#"         | 2
        "topic/Filter"       | "\$share/group1/topic/Filter" | MqttQos.EXACTLY_ONCE | "topic/#"                | MqttQos.AT_LEAST_ONCE | "topic/Filter"    | 2
        "topic/Filter/First" | "topic/+/First"               | MqttQos.AT_MOST_ONCE | "\$share/group2/topic/#" | MqttQos.AT_LEAST_ONCE | "topic/#"         | 2
        "topic/Filter/First" | "topic/+/First"               | MqttQos.EXACTLY_ONCE | "topic/#"                | MqttQos.AT_LEAST_ONCE | "topic/+/First"   | 2
  }

  @Unroll
  def "should reject subscribe with wrong topic filter"(
      String wrongTopicFilter,
      Mqtt5SubAckReasonCode reasonCode) {
    given:
        def subscriber = buildExternalMqtt5Client()
    when:
        fromAsync(subscriber.connectWith().send())
        fromAsync(subscriber.subscribeWith()
            .topicFilter(wrongTopicFilter)
            .send())
    then:
        def completionEx = thrown CompletionException
        def subAckException = completionEx.cause as Mqtt5SubAckException
        with(subAckException) {
          message == "SUBACK contains only Error Codes"
          with(getMqttMessage()) {
            getReasonCodes().get(0) == reasonCode
          }
        }
    cleanup:
        fromAsync(subscriber.disconnect())
    where:
        wrongTopicFilter | reasonCode
        "\$sys/topic/"   | Mqtt5SubAckReasonCode.TOPIC_FILTER_INVALID
        "topic//Filter"  | Mqtt5SubAckReasonCode.TOPIC_FILTER_INVALID
  }
}
