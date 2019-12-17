package com.ss.mqtt.broker.test.integration.service

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5SubAckException
import com.ss.mqtt.broker.model.Subscriber
import com.ss.mqtt.broker.service.ClientIdRegistry
import com.ss.mqtt.broker.service.impl.SimpleSubscriptionService
import com.ss.mqtt.broker.test.integration.IntegrationSpecification
import org.spockframework.util.Pair
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

import java.util.concurrent.CompletionException

import static com.hivemq.client.mqtt.datatypes.MqttQos.*
import static com.ss.mqtt.broker.model.ActionResult.SUCCESS
import static com.ss.mqtt.broker.model.topic.TopicName.from
import static org.spockframework.util.Pair.of

class SubscribtionServiceTest extends IntegrationSpecification {
    
    @Autowired
    ClientIdRegistry clientIdRegistry
    
    @Autowired
    SimpleSubscriptionService subscriptionService
    
    def "should clear/restore topic subscribers after disconnect/reconnect"() {
        given:
            def clientId = clientIdRegistry.generate().block()
            def subscriber = buildMqtt5Client(clientId)
            def topicName = from(topicFilter)
            
            def matchesCount = 0
            def matchedSubscriber = null
            def action = { subs, clId ->
                matchesCount++
                matchedSubscriber = subs
                SUCCESS
            }
        when:
            subscriber.connectWith()
                .cleanStart(true)
                .send()
                .join()
            subscriber.subscribeWith()
                .topicFilter(topicFilter)
                .qos(AT_MOST_ONCE)
                .send()
                .join()
            
            def actionResult = subscriptionService.forEachTopicSubscriber(topicName, clientId, action)
        then:
            matchesCount == 1
            matchedSubscriber.mqttClient.getClientId() == clientId
            matchedSubscriber.topicFilter.getRawTopic() == topicFilter
            actionResult == SUCCESS
        when:
            subscriber.disconnect().join()
            subscriber.connectWith()
                .cleanStart(false)
                .send()
                .join()
            
            actionResult = subscriptionService.forEachTopicSubscriber(topicName, clientId, action)
        then:
            matchesCount == 2
            matchedSubscriber.mqttClient.getClientId() == clientId
            matchedSubscriber.topicFilter.getRawTopic() == topicFilter
            actionResult == SUCCESS
        cleanup:
            subscriber.disconnect().join()
    }
    
    @Unroll
    def "should match subscriber with the highest QoS"(
        String topicName,
        Pair<String, MqttQos> topicFilter1,
        Pair<String, MqttQos> topicFilter2,
        String targetTopicFilter
    ) {
        given:
            def clientId = clientIdRegistry.generate().block()
            def subscriber = buildMqtt5Client(clientId)
            
            def matchesCount = 0
            Subscriber matchedSubscriber = null
            def action = { subs, clId ->
                matchesCount++
                matchedSubscriber = subs
                SUCCESS
            }
            subscriber.connectWith()
                .cleanStart(true)
                .send()
                .join()
            subscriber.subscribeWith()
                .topicFilter(topicFilter1.first())
                .qos(topicFilter1.second())
                .send()
                .join()
            subscriber.subscribeWith()
                .topicFilter(topicFilter2.first())
                .qos(topicFilter2.second())
                .send()
                .join()
        when:
            subscriptionService.forEachTopicSubscriber(from(topicName), clientId, action)
        then:
            matchesCount == 1
            matchedSubscriber.topicFilter.getRawTopic() == targetTopicFilter
        cleanup:
            subscriber.disconnect().join()
        where:
            topicName            | topicFilter1                      | topicFilter2                 | targetTopicFilter
            "topic/Filter"       | of("topic/Filter", AT_MOST_ONCE)  | of("topic/#", AT_LEAST_ONCE) | "topic/#"
            "topic/Filter"       | of("topic/Filter", EXACTLY_ONCE)  | of("topic/#", AT_LEAST_ONCE) | "topic/Filter"
            "topic/Another"      | of("topic/Filter", EXACTLY_ONCE)  | of("topic/#", AT_LEAST_ONCE) | "topic/#"
            "topic/Filter/First" | of("topic/+/First", AT_MOST_ONCE) | of("topic/#", AT_LEAST_ONCE) | "topic/#"
            "topic/Filter/First" | of("topic/+/First", EXACTLY_ONCE) | of("topic/#", AT_LEAST_ONCE) | "topic/+/First"
    }
    
    @Unroll
    def "should match all subscribers with shared and single topic"(
        String topicName,
        Pair<String, MqttQos> topicFilter1,
        Pair<String, MqttQos> topicFilter2,
        String targetTopicFilter,
        int targetCount
    ) {
        given:
            def clientId1 = clientIdRegistry.generate().block()
            def clientId2 = clientIdRegistry.generate().block()
            def subscriber1 = buildMqtt5Client(clientId1)
            def subscriber2 = buildMqtt5Client(clientId2)
            
            def matchesCount = 0
            def matchedSubscribers = new LinkedHashSet<String>();
            def action = { Subscriber subscriber, String clientId ->
                matchesCount++
                matchedSubscribers.add(subscriber.getMqttClient().getClientId())
                SUCCESS
            }
            
            subscriber1.connectWith()
                .cleanStart(true)
                .send()
                .join()
            subscriber2.connectWith()
                .cleanStart(true)
                .send()
                .join()
            
            
            subscriber1.subscribeWith()
                .topicFilter(topicFilter1.first())
                .qos(topicFilter1.second())
                .send()
                .join()
            subscriber2.subscribeWith()
                .topicFilter(topicFilter2.first())
                .qos(topicFilter2.second())
                .send()
                .join()
        when:
            subscriptionService.forEachTopicSubscriber(from(topicName), clientId, action)
        then:
            matchesCount == targetCount
            matchedSubscribers[0] == clientId1
            matchedSubscribers[1] == clientId2
        cleanup:
            subscriber1.disconnect().join()
            subscriber2.disconnect().join()
        where:
            topicName            | topicFilter1                                     | topicFilter2                                 | targetTopicFilter | targetCount
            "topic/Filter"       | of("\$shared/group1/topic/Filter", AT_MOST_ONCE) | of("\$shared/group2/topic/#", AT_LEAST_ONCE) | "topic/#"         | 2
            "topic/Filter"       | of("\$shared/group1/topic/Filter", EXACTLY_ONCE) | of("topic/#", AT_LEAST_ONCE)                 | "topic/Filter"    | 2
            "topic/Filter/First" | of("topic/+/First", AT_MOST_ONCE)                | of("\$shared/group2/topic/#", AT_LEAST_ONCE) | "topic/#"         | 2
            "topic/Filter/First" | of("topic/+/First", EXACTLY_ONCE)                | of("topic/#", AT_LEAST_ONCE)                 | "topic/+/First"   | 2
    }
    
    @Unroll
    def "should reject subscribe with wrong topic filter"(
        String wrongTopicFilter,
        Class<Throwable> exception,
        Class<Throwable> cause,
        String message
    ) {
        given:
            def clientId = clientIdRegistry.generate().block()
            def subscriber = buildMqtt5Client(clientId)
        when:
            subscriber.connectWith()
                .cleanStart(true)
                .send()
                .join()
            subscriber.subscribeWith()
                .topicFilter(wrongTopicFilter)
                .qos(AT_MOST_ONCE)
                .send()
                .join()
        then:
            def ex = thrown exception
            if (ex.cause != null) {
                ex.cause.class == cause
                ex.cause.message == "SUBACK contains only Error Codes"
            }
        where:
            wrongTopicFilter  | exception                | cause                | message
            "topic/"          | CompletionException      | Mqtt5SubAckException | "SUBACK contains only Error Codes"
            "topic//Filter"   | CompletionException      | Mqtt5SubAckException | "SUBACK contains only Error Codes"
            "/topic/Another"  | CompletionException      | Mqtt5SubAckException | "SUBACK contains only Error Codes"
            "topic/##"        | IllegalArgumentException | null                 | null
            "++/Filter/First" | IllegalArgumentException | null                 | null
    }
}
