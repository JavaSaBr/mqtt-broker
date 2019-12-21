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
import static com.ss.mqtt.broker.util.TopicUtils.newTopicFilter
import static com.ss.mqtt.broker.util.TopicUtils.newTopicName

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
                [newTopicFilter("topic/second/in"), newTopicFilter("topic/+/in"), newTopicFilter("topic/#")],
                [newTopicFilter("topic/+/in"), newTopicFilter("topic/first/in"), newTopicFilter("topic/out")],
                [newTopicFilter("topic/second/in"), newTopicFilter("topic/first/in"), newTopicFilter("topic/out")],
                [newTopicFilter("topic/second/in"), newTopicFilter("topic/+/in"), newTopicFilter("topic/#")]
            ]
            topicNames << [
                newTopicName("topic/second/in"),
                newTopicName("topic/first/in"),
                newTopicName("topic/second/in"),
                newTopicName("topic/second/in")
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
