package javasabr.mqtt.service

import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.SubscribeRetainHandling
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode
import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode
import javasabr.mqtt.model.subscribtion.Subscription
import javasabr.mqtt.service.impl.InMemorySubscriptionService
import javasabr.rlib.collections.array.Array

class InMemorySubscriptionServiceTest extends IntegrationServiceSpecification {

  SubscriptionService subscriptionService = new InMemorySubscriptionService()

  def "should subscribe with expected results in default settings"() {
    given:
        def serverConfig = defaultExternalServerConnectionConfig
        def mqttConnection = mockedExternalConnection(serverConfig, MqttVersion.MQTT_5)
        def mqttClient = mqttConnection.client()
        def subscriptions = Array.of(
            new Subscription(
                defaultTopicService.createTopicFilter(mqttClient, "topic/filter/1"),
                30,
                QoS.AT_MOST_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttClient, "topic/filter/2"),
                30,
                QoS.AT_LEAST_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttClient, "topic/filter/3"),
                30,
                QoS.EXACTLY_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttClient, "topic/invalid/##"),
                30,
                QoS.EXACTLY_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true))
    when:
        def result = subscriptionService.subscribe(mqttClient, subscriptions)
    then:
        result.size() == 4
        result == Array.of(
            SubscribeAckReasonCode.GRANTED_QOS_0,
            SubscribeAckReasonCode.GRANTED_QOS_1,
            SubscribeAckReasonCode.GRANTED_QOS_2,
            SubscribeAckReasonCode.TOPIC_FILTER_INVALID)
  }

  def "should not subscribe with for not supported topic filter"() {
    given:
        def serverConfig = defaultExternalServerConnectionConfig
            .withSharedSubscriptionAvailable(false)
            .withWildcardSubscriptionAvailable(false)
        def mqttConnection = mockedExternalConnection(serverConfig, MqttVersion.MQTT_5)
        def mqttClient = mqttConnection.client()
        def subscriptions = Array.of(
            new Subscription(
                defaultTopicService.createTopicFilter(mqttClient, "topic/filter/+"),
                5,
                QoS.AT_MOST_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttClient, "topic/filter/#"),
                5,
                QoS.AT_LEAST_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttClient, "\$share/group1/topic/filter/3"),
                5,
                QoS.EXACTLY_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttClient, "\$share/group1/topic/filter/#"),
                5,
                QoS.EXACTLY_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttClient, "\$share/group1/topic/filter/+"),
                5,
                QoS.EXACTLY_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true))
    when:
        def result = subscriptionService.subscribe(mqttClient, subscriptions)
    then:
        result.size() == 5
        result == Array.of(
            SubscribeAckReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED,
            SubscribeAckReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED,
            SubscribeAckReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED,
            SubscribeAckReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED,
            SubscribeAckReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED)
  }

  def "should store subscription with correct subscription id"() {
    given:
        def serverConfig = defaultExternalServerConnectionConfig
        def mqttConnection = mockedExternalConnection(serverConfig, MqttVersion.MQTT_5)
        def mqttClient = mqttConnection.client()

        def sub1 = new Subscription(
            defaultTopicService.createTopicFilter(mqttClient, "topic/filter/1"),
            15,
            QoS.AT_MOST_ONCE,
            SubscribeRetainHandling.SEND,
            true,
            true)
        def sub2 = new Subscription(
            defaultTopicService.createTopicFilter(mqttClient, "topic/filter/2"),
            15,
            QoS.AT_LEAST_ONCE,
            SubscribeRetainHandling.SEND,
            true,
            true)

        def sub3 = new Subscription(
            defaultTopicService.createTopicFilter(mqttClient, "topic/filter/3"),
            30,
            QoS.EXACTLY_ONCE,
            SubscribeRetainHandling.SEND,
            true,
            true)
        def sub4 = new Subscription(
            defaultTopicService.createTopicFilter(mqttClient, "topic/invalid/4"),
            30,
            QoS.EXACTLY_ONCE,
            SubscribeRetainHandling.SEND,
            true,
            true)
        def subscriptions = Array.of(sub1, sub2, sub3, sub4)
    when:
        def result = subscriptionService.subscribe(mqttClient, subscriptions)
    then:
        result.size() == 4
        result == Array.of(
            SubscribeAckReasonCode.GRANTED_QOS_0,
            SubscribeAckReasonCode.GRANTED_QOS_1,
            SubscribeAckReasonCode.GRANTED_QOS_2,
            SubscribeAckReasonCode.GRANTED_QOS_2)
    when:
        def session = mqttClient.session()
        def subsWithId15 = session.findStoredSubscriptionWithId(15)
        def subsWithId30 = session.findStoredSubscriptionWithId(30)
    then:
        subsWithId15.size() == 2
        subsWithId30.size() == 2
        subsWithId15 == Array.of(sub1, sub2)
        subsWithId30 == Array.of(sub3, sub4)
  }

  def "should unsubscribe with expected results"() {
    given:
        def serverConfig = defaultExternalServerConnectionConfig
        def mqttConnection = mockedExternalConnection(serverConfig, MqttVersion.MQTT_5)
        def mqttClient = mqttConnection.client()
        def subscriptions = Array.of(
            new Subscription(
                defaultTopicService.createTopicFilter(mqttClient, "topic/filter/1"),
                30,
                QoS.AT_MOST_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttClient, "topic/filter/2"),
                30,
                QoS.AT_LEAST_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttClient, "topic/filter/3"),
                30,
                QoS.EXACTLY_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true))
        subscriptionService.subscribe(mqttClient, subscriptions)
        def topicsToUnsubscribe = Array.of(
            defaultTopicService.createTopicFilter(mqttClient, "topic/filter/1"),
            defaultTopicService.createTopicFilter(mqttClient, "topic/filter/3"),
            defaultTopicService.createTopicFilter(mqttClient, "topic/filter/notexist"),
            defaultTopicService.createTopicFilter(mqttClient, "topic/filter/invalid##"))
    when:
        def result = subscriptionService.unsubscribe(mqttClient, topicsToUnsubscribe)
    then:
        result.size() == 4
        result == Array.of(
            UnsubscribeAckReasonCode.SUCCESS,
            UnsubscribeAckReasonCode.SUCCESS,
            UnsubscribeAckReasonCode.NO_SUBSCRIPTION_EXISTED,
            UnsubscribeAckReasonCode.TOPIC_FILTER_INVALID)
  }

  def "should store and clean subscriptions in MQTT session"() {
    given:
        def serverConfig = defaultExternalServerConnectionConfig
        def mqttConnection = mockedExternalConnection(serverConfig, MqttVersion.MQTT_5)
        def mqttClient = mqttConnection.client()
        def mqttSession = mqttClient.session()
        def subscriptions = Array.of(
            new Subscription(
                defaultTopicService.createTopicFilter(mqttClient, "topic/filter/1"),
                30,
                QoS.AT_MOST_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttClient, "topic/filter/2"),
                30,
                QoS.AT_LEAST_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttClient, "topic/filter/3"),
                30,
                QoS.EXACTLY_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true))
        def topicsToUnsubscribe = Array.of(
            defaultTopicService.createTopicFilter(mqttClient, "topic/filter/1"),
            defaultTopicService.createTopicFilter(mqttClient, "topic/filter/3"))
    when:
        subscriptionService.subscribe(mqttClient, subscriptions)
        def storedSubscriptions = mqttSession.storedSubscriptions()
    then:
        storedSubscriptions.size() == 3
        storedSubscriptions == subscriptions
    when:
        subscriptionService.unsubscribe(mqttClient, topicsToUnsubscribe)
        storedSubscriptions = mqttSession.storedSubscriptions()
    then:
        storedSubscriptions.size() == 1
        storedSubscriptions.get(0) == subscriptions.get(1)
  }

  def "should replace already existed subscriptions"() {
    given:
        def serverConfig = defaultExternalServerConnectionConfig
        def mqttConnection = mockedExternalConnection(serverConfig, MqttVersion.MQTT_5)
        def mqttClient = mqttConnection.client()
        def mqttSession = mqttClient.session()
        def subscriptions = Array.of(
            new Subscription(
                defaultTopicService.createTopicFilter(mqttClient, "topic/filter/1"),
                30,
                QoS.AT_MOST_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttClient, "topic/filter/2"),
                30,
                QoS.AT_LEAST_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttClient, "topic/filter/3"),
                30,
                QoS.EXACTLY_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true))
        def subscriptions2 = Array.typed(Subscription,
            new Subscription(
                defaultTopicService.createTopicFilter(mqttClient, "topic/filter/1"),
                55,
                QoS.EXACTLY_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttClient, "topic/filter/3"),
                55,
                QoS.AT_LEAST_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true))
        def resultSubscriptions = Array.of(
            subscriptions2.get(0),
            subscriptions.get(1),
            subscriptions2.get(1))
    when:
        subscriptionService.subscribe(mqttClient, subscriptions)
        def storedSubscriptions = mqttSession.storedSubscriptions()
    then:
        storedSubscriptions.size() == 3
        storedSubscriptions == subscriptions
    when:
        subscriptionService.subscribe(mqttClient, subscriptions2)
        storedSubscriptions = mqttSession.storedSubscriptions()
    then:
        storedSubscriptions.size() == 3
        storedSubscriptions ==~ resultSubscriptions
  }
}
