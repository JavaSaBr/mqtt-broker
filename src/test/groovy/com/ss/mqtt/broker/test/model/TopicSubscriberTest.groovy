package com.ss.mqtt.broker.test.model

import com.ss.mqtt.broker.model.QoS
import com.ss.mqtt.broker.model.SubscribeTopicFilter
import com.ss.mqtt.broker.model.Subscriber
import com.ss.mqtt.broker.model.topic.TopicFilter
import com.ss.mqtt.broker.model.topic.TopicName
import com.ss.mqtt.broker.model.topic.TopicSubscribers
import com.ss.mqtt.broker.network.client.MqttClient
import com.ss.mqtt.broker.test.network.BasePacketTest
import spock.lang.Shared
import spock.lang.Unroll

import static com.ss.mqtt.broker.model.QoS.*

class TopicSubscriberTest extends BasePacketTest {
    
    @Shared
    def defaultMqttClient = defaultMqttClient()
    
    @Unroll
    def "should choose #matchedQos from #subscriberQos"(
        String[] stringTopicFilter,
        String stringTopicName,
        QoS[] subscriberQos,
        QoS[] matchedQos,
        MqttClient[] mqttClients
    ) {
        given:
            def topicName = new TopicName(stringTopicName)
            def topicFilter1 = new TopicFilter(stringTopicFilter[0])
            def topicFilter2 = new TopicFilter(stringTopicFilter[1])
            def topicFilter3 = new TopicFilter(stringTopicFilter[2])
            def subscribeTopicFilter = Mock(SubscribeTopicFilter) {
                getQos() >>> subscriberQos
            }
            def subscriber1 = new Subscriber(mqttClients[0], subscribeTopicFilter)
            def subscriber2 = new Subscriber(mqttClients[1], subscribeTopicFilter)
            def subscriber3 = new Subscriber(mqttClients[2], subscribeTopicFilter)
            def topicSubscriber = new TopicSubscribers()
        when:
            topicSubscriber.addSubscriber(topicFilter1, subscriber1)
            topicSubscriber.addSubscriber(topicFilter2, subscriber2)
            topicSubscriber.addSubscriber(topicFilter3, subscriber3)
        then:
            def subscribers = topicSubscriber.matches(topicName)
            subscribers.size() == matchedQos.size()
            for (int i = 0; i < subscribers.size(); i++) {
                subscribers[i].qos == matchedQos[i]
            }
        where:
            stringTopicFilter << [
                ["topic/second/in", "topic/+/in", "topic/#"],
                ["topic/+/in", "topic/first/in", "topic/out"],
                ["topic/second/in", "topic/first/in", "topic/out"],
                ["topic/second/in", "topic/+/in", "topic/#"]
            ]
            stringTopicName << [
                "topic/second/in",
                "topic/first/in",
                "topic/second/in",
                "topic/second/in"
            ]
            subscriberQos << [
                [AT_LEAST_ONCE_DELIVERY, AT_MOST_ONCE_DELIVERY, EXACTLY_ONCE_DELIVERY],
                [AT_LEAST_ONCE_DELIVERY, AT_MOST_ONCE_DELIVERY, EXACTLY_ONCE_DELIVERY],
                [AT_LEAST_ONCE_DELIVERY, AT_MOST_ONCE_DELIVERY, EXACTLY_ONCE_DELIVERY],
                [AT_LEAST_ONCE_DELIVERY, AT_MOST_ONCE_DELIVERY, EXACTLY_ONCE_DELIVERY]
            ]
            matchedQos << [
                [EXACTLY_ONCE_DELIVERY],
                [AT_MOST_ONCE_DELIVERY],
                [AT_LEAST_ONCE_DELIVERY],
                [AT_LEAST_ONCE_DELIVERY, AT_MOST_ONCE_DELIVERY, EXACTLY_ONCE_DELIVERY]
            ]
            mqttClients << [
                [defaultMqttClient, defaultMqttClient, defaultMqttClient],
                [defaultMqttClient, defaultMqttClient, defaultMqttClient],
                [defaultMqttClient, defaultMqttClient, defaultMqttClient],
                [defaultMqttClient(), defaultMqttClient(), defaultMqttClient()]
            ]
    }
    
}
