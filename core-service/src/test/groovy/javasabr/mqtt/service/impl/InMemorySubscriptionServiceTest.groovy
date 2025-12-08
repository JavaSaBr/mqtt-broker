package javasabr.mqtt.service.impl

import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.SubscribeRetainHandling
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode
import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode
import javasabr.mqtt.model.subscriber.SingleSubscriber
import javasabr.mqtt.model.subscription.Subscription
import javasabr.mqtt.model.subscription.TestPublishFactory
import javasabr.mqtt.network.message.out.PublishMqtt5OutMessage
import javasabr.mqtt.service.IntegrationServiceSpecification
import javasabr.mqtt.service.TestExternalNetworkMqttUser
import javasabr.rlib.collections.array.Array

class InMemorySubscriptionServiceTest extends IntegrationServiceSpecification {

  def "should subscribe with expected results in default settings"() {
    given:
        def serverConfig = defaultExternalServerConnectionConfig
        def mqttConnection = mockedExternalConnection(serverConfig, MqttVersion.MQTT_5)
        def mqttUser = mqttConnection.user()
        def subscriptions = Array.of(
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "topic/filter/1"),
                30,
                QoS.AT_MOST_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "topic/filter/2"),
                30,
                QoS.AT_LEAST_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "topic/filter/3"),
                30,
                QoS.EXACTLY_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "topic/invalid/##"),
                30,
                QoS.EXACTLY_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true))
    when:
        def result = defaultSubscriptionService
            .subscribe(mqttUser, mqttUser.session(), subscriptions)
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
        def mqttUser = mqttConnection.user()
        def subscriptions = Array.of(
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "topic/filter/+"),
                5,
                QoS.AT_MOST_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "topic/filter/#"),
                5,
                QoS.AT_LEAST_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "\$share/group1/topic/filter/3"),
                5,
                QoS.EXACTLY_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "\$share/group1/topic/filter/#"),
                5,
                QoS.EXACTLY_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "\$share/group1/topic/filter/+"),
                5,
                QoS.EXACTLY_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true))
    when:
        def result = defaultSubscriptionService
            .subscribe(mqttUser, mqttUser.session(), subscriptions)
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
        def mqttUser = mqttConnection.user()
        def sub1 = new Subscription(
            defaultTopicService.createTopicFilter(mqttUser, "topic/filter/1"),
            15,
            QoS.AT_MOST_ONCE,
            SubscribeRetainHandling.SEND,
            true,
            true)
        def sub2 = new Subscription(
            defaultTopicService.createTopicFilter(mqttUser, "topic/filter/2"),
            15,
            QoS.AT_LEAST_ONCE,
            SubscribeRetainHandling.SEND,
            true,
            true)

        def sub3 = new Subscription(
            defaultTopicService.createTopicFilter(mqttUser, "topic/filter/3"),
            30,
            QoS.EXACTLY_ONCE,
            SubscribeRetainHandling.SEND,
            true,
            true)
        def sub4 = new Subscription(
            defaultTopicService.createTopicFilter(mqttUser, "topic/invalid/4"),
            30,
            QoS.EXACTLY_ONCE,
            SubscribeRetainHandling.SEND,
            true,
            true)
        def subscriptions = Array.of(sub1, sub2, sub3, sub4)
    when:
        def result = defaultSubscriptionService
            .subscribe(mqttUser, mqttUser.session(), subscriptions)
    then:
        result.size() == 4
        result == Array.of(
            SubscribeAckReasonCode.GRANTED_QOS_0,
            SubscribeAckReasonCode.GRANTED_QOS_1,
            SubscribeAckReasonCode.GRANTED_QOS_2,
            SubscribeAckReasonCode.GRANTED_QOS_2)
    when:
        def mqttSession = mqttUser.session()
        def activeSubscriptions = mqttSession.activeSubscriptions()
        def subsWithId15 = activeSubscriptions.findBySubscriptionId(15)
        def subsWithId30 = activeSubscriptions.findBySubscriptionId(30)
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
        def mqttUser = mqttConnection.user()
        def subscriptions = Array.of(
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "topic/filter/1"),
                30,
                QoS.AT_MOST_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "topic/filter/2"),
                30,
                QoS.AT_LEAST_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "topic/filter/3"),
                30,
                QoS.EXACTLY_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true))
        defaultSubscriptionService.subscribe(mqttUser, mqttUser.session(), subscriptions)
        def topicsToUnsubscribe = Array.of(
            defaultTopicService.createTopicFilter(mqttUser, "topic/filter/1"),
            defaultTopicService.createTopicFilter(mqttUser, "topic/filter/3"),
            defaultTopicService.createTopicFilter(mqttUser, "topic/filter/notexist"),
            defaultTopicService.createTopicFilter(mqttUser, "topic/filter/invalid##"))
    when:
        def result = defaultSubscriptionService
            .unsubscribe(mqttUser, mqttUser.session(), topicsToUnsubscribe)
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
        def mqttUser = mqttConnection.user()
        def mqttSession = mqttUser.session()
        def activeSubscriptions = mqttSession.activeSubscriptions()
        def subscriptions = Array.of(
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "topic/filter/1"),
                30,
                QoS.AT_MOST_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "topic/filter/2"),
                30,
                QoS.AT_LEAST_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "topic/filter/3"),
                30,
                QoS.EXACTLY_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true))
        def topicsToUnsubscribe = Array.of(
            defaultTopicService.createTopicFilter(mqttUser, "topic/filter/1"),
            defaultTopicService.createTopicFilter(mqttUser, "topic/filter/3"))
    when:
        defaultSubscriptionService.subscribe(mqttUser, mqttUser.session(), subscriptions)
        def storedSubscriptions = activeSubscriptions.subscriptions()
    then:
        storedSubscriptions.size() == 3
        storedSubscriptions == subscriptions
    when:
        defaultSubscriptionService.unsubscribe(mqttUser, mqttUser.session(), topicsToUnsubscribe)
        storedSubscriptions = activeSubscriptions.subscriptions()
    then:
        storedSubscriptions.size() == 1
        storedSubscriptions.get(0) == subscriptions.get(1)
  }

  def "should replace already existed subscriptions"() {
    given:
        def serverConfig = defaultExternalServerConnectionConfig
        def mqttConnection = mockedExternalConnection(serverConfig, MqttVersion.MQTT_5)
        def mqttUser = mqttConnection.user()
        def mqttSession = mqttUser.session()
        def activeSubscriptions = mqttSession.activeSubscriptions()
        def subscriptions = Array.of(
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "topic/filter/1"),
                30,
                QoS.AT_MOST_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "topic/filter/2"),
                30,
                QoS.AT_LEAST_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "topic/filter/3"),
                30,
                QoS.EXACTLY_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true))
        def subscriptions2 = Array.typed(Subscription,
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "topic/filter/1"),
                55,
                QoS.EXACTLY_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "topic/filter/3"),
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
        defaultSubscriptionService.subscribe(mqttUser, mqttUser.session(), subscriptions)
        def storedSubscriptions = activeSubscriptions.subscriptions()
    then:
        storedSubscriptions.size() == 3
        storedSubscriptions == subscriptions
    when:
        defaultSubscriptionService.subscribe(mqttUser, mqttUser.session(), subscriptions2)
        storedSubscriptions = activeSubscriptions.subscriptions()
    then:
        storedSubscriptions.size() == 3
        storedSubscriptions ==~ resultSubscriptions
  }

  def "should only deliver 'send-if-subscription-does-not-exist' Subscribe Retain Handling once"() {
    given:
        def serverConfig = defaultExternalServerConnectionConfig
        def mqttConnection = mockedExternalConnection(serverConfig, MqttVersion.MQTT_5)
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
        def subscription = new Subscription(
            defaultTopicService.createTopicFilter(mqttUser, "topic/filter/1"),
            30,
            QoS.AT_MOST_ONCE,
            SubscribeRetainHandling.SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST,
            true,
            true)
        def subscriptions = Array.of(
            subscription,
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "topic/filter/2"),
                30,
                QoS.AT_LEAST_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "topic/filter/3"),
                30,
                QoS.EXACTLY_ONCE,
                SubscribeRetainHandling.DO_NOT_SEND,
                true,
                true))
    and:
        def publishWithRetain = TestPublishFactory.makePublishWithRetain("topic/filter/1", "payload1")
        def publishWithoutRetain = TestPublishFactory.makePublishWithoutRetain("topic/filter/1", "payload2")
        defaultPublishDeliveringService.startDelivering(publishWithRetain, new SingleSubscriber(mqttUser, subscription))
        defaultPublishDeliveringService.startDelivering(publishWithoutRetain, new SingleSubscriber(mqttUser, subscription))
        defaultSubscriptionService.subscribe(mqttUser, mqttUser.session(), subscriptions)
    when:
        defaultSubscriptionService.subscribe(mqttUser, mqttUser.session(), subscriptions)
    then:
        def firstPublishMessage = mqttUser.nextSentMessage(PublishMqtt5OutMessage)
        firstPublishMessage.payload() == publishWithRetain.payload()
    and:
        def secondPublishMessage = mqttUser.nextSentMessage(PublishMqtt5OutMessage)
        secondPublishMessage.payload() == publishWithoutRetain.payload()
    and:
        def thirdPublishMessage = mqttUser.nextSentMessage(PublishMqtt5OutMessage)
        thirdPublishMessage.payload() == publishWithRetain.payload()
    and:
        mqttUser.isEmpty()
  }

  def "should always deliver 'send' Subscribe Retain Handling"() {
    given:
        def serverConfig = defaultExternalServerConnectionConfig
        def mqttConnection = mockedExternalConnection(serverConfig, MqttVersion.MQTT_5)
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
        def subscription = new Subscription(
            defaultTopicService.createTopicFilter(mqttUser, "topic/filter/1"),
            30,
            QoS.AT_MOST_ONCE,
            SubscribeRetainHandling.SEND,
            true,
            true)
        def subscriptions = Array.of(subscription)
    and:
        def publishWithRetain = TestPublishFactory.makePublishWithRetain("topic/filter/1", "payload1")
        defaultPublishDeliveringService.startDelivering(publishWithRetain, new SingleSubscriber(mqttUser, subscription))
        defaultSubscriptionService.subscribe(mqttUser, mqttUser.session(), subscriptions)
    when:
        defaultSubscriptionService.subscribe(mqttUser, mqttUser.session(), subscriptions)
    then:
        def firstSentMessage = mqttUser.nextSentMessage(PublishMqtt5OutMessage)
        firstSentMessage.payload() == publishWithRetain.payload()
    and:
        def thirdSentMessage = mqttUser.nextSentMessage(PublishMqtt5OutMessage)
        thirdSentMessage.payload() == publishWithRetain.payload()
    and:
        def fourthSentMessage = mqttUser.nextSentMessage(PublishMqtt5OutMessage)
        fourthSentMessage.payload() == publishWithRetain.payload()
    and:
        mqttUser.isEmpty()
  }

  def "should not deliver 'do-not-send' Subscribe Retain Handling"() {
    given:
        def serverConfig = defaultExternalServerConnectionConfig
        def mqttConnection = mockedExternalConnection(serverConfig, MqttVersion.MQTT_5)
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
        def subscription = new Subscription(
            defaultTopicService.createTopicFilter(mqttUser, "topic/filter/1"),
            30,
            QoS.AT_MOST_ONCE,
            SubscribeRetainHandling.DO_NOT_SEND,
            true,
            true)
        def subscriptions = Array.of(
            subscription,
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "topic/filter/2"),
                30,
                QoS.AT_LEAST_ONCE,
                SubscribeRetainHandling.SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST,
                true,
                true),
            new Subscription(
                defaultTopicService.createTopicFilter(mqttUser, "topic/filter/3"),
                30,
                QoS.EXACTLY_ONCE,
                SubscribeRetainHandling.SEND,
                true,
                true))
    and:
        def publishWithRetain = TestPublishFactory.makePublishWithRetain("topic/filter/1", "payload1")
        def publishWithoutRetain = TestPublishFactory.makePublishWithoutRetain("topic/filter/1", "payload2")
        defaultPublishDeliveringService.startDelivering(publishWithRetain, new SingleSubscriber(mqttUser, subscription))
        defaultPublishDeliveringService.startDelivering(publishWithoutRetain, new SingleSubscriber(mqttUser, subscription))
        defaultSubscriptionService.subscribe(mqttUser, mqttUser.session(), subscriptions)
    when:
        defaultSubscriptionService.subscribe(mqttUser, mqttUser.session(), subscriptions)
    then:
        def firstPublishMessage = mqttUser.nextSentMessage(PublishMqtt5OutMessage)
        firstPublishMessage.payload() == publishWithRetain.payload()
    and:
        def secondPublishMessage = mqttUser.nextSentMessage(PublishMqtt5OutMessage)
        secondPublishMessage.payload() == publishWithoutRetain.payload()
    and:
        mqttUser.isEmpty()
  }
}
