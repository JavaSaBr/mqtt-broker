package com.ss.mqtt.broker.test.model

import com.ss.mqtt.broker.model.QoS
import com.ss.mqtt.broker.model.SubscribeTopicFilter
import com.ss.mqtt.broker.model.topic.TopicFilter
import com.ss.mqtt.broker.model.topic.TopicName
import com.ss.mqtt.broker.model.topic.TopicSubscribers
import com.ss.mqtt.broker.network.client.MqttClient
import com.ss.mqtt.broker.test.network.NetworkUnitSpecification
import spock.lang.Unroll

import static com.ss.mqtt.broker.model.QoS.*

class TopicSubscriberTest extends NetworkUnitSpecification {
    
    @Unroll
    def "should choose #matchedQos from #subscriberQos"(
        String[] stringTopicFilter,
        String stringTopicName,
        QoS[] subscriberQos,
        QoS[] matchedQos,
        MqttClient[] mqttClients
    ) {
        given:
            def topicName = TopicName.from(stringTopicName)
            def topicFilter1 = TopicFilter.from(stringTopicFilter[0])
            def topicFilter2 = TopicFilter.from(stringTopicFilter[1])
            def topicFilter3 = TopicFilter.from(stringTopicFilter[2])
            def subscribeTopicFilter = Mock(SubscribeTopicFilter) {
                getQos() >>> subscriberQos
                getTopicFilter() >>> [topicFilter1, topicFilter2, topicFilter3]
            }
            def topicSubscriber = new TopicSubscribers()
        when:
            topicSubscriber.addSubscriber(mqttClients[0], subscribeTopicFilter)
            topicSubscriber.addSubscriber(mqttClients[1], subscribeTopicFilter)
            topicSubscriber.addSubscriber(mqttClients[2], subscribeTopicFilter)
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
