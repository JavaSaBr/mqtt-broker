package com.ss.mqtt.broker.test.integration.service

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.ss.mqtt.broker.model.topic.TopicName
import com.ss.mqtt.broker.model.topic.TopicSubscribers
import com.ss.mqtt.broker.service.ClientIdRegistry
import com.ss.mqtt.broker.service.MqttSessionService
import com.ss.mqtt.broker.test.integration.IntegrationSpecification
import org.springframework.beans.factory.annotation.Autowired

class SubscribtionServiceTest extends IntegrationSpecification {
    
    @Autowired
    ClientIdRegistry clientIdRegistry
    
    @Autowired
    MqttSessionService mqttSessionService
    
    @Autowired
    TopicSubscribers topicSubscribers
    
    def "should clear/restore topic subscribers after disconnect/reconnect"() {
        given:
            def clientId = clientIdRegistry.generate().block()
            def subscriber = buildClient(clientId)
        when:
            connectAndSubscribe(subscriber, true, topicFilter)
            def matches = topicSubscribers.matches(TopicName.from(topicFilter))
        then:
            matches.size() == 1
            matches.get(0).mqttClient.clientId == clientId
        when:
            subscriber.disconnect().join()
            Thread.sleep(100)
            def session = mqttSessionService.restore(clientId).block()
            matches = topicSubscribers.matches(TopicName.from(topicFilter))
        then:
            session.getTopicFilters().size() == 1
            session.getTopicFilters().get(0).topicFilter.rawTopic == topicFilter
            matches.size() == 0
        when:
            mqttSessionService.store(clientId, session, 5).block()
            connectAndSubscribe(subscriber, false, "topic/#")
            Thread.sleep(100)
            matches = topicSubscribers.matches(TopicName.from(topicFilter))
        then:
            TopicSubscribers firstLevelTs = topicSubscribers.topicSubscribers.get("topic")
            firstLevelTs != null
            TopicSubscribers secondLevelTs = firstLevelTs.topicSubscribers.get("Filter")
            secondLevelTs != null
            secondLevelTs.subscribers != null
            secondLevelTs.subscribers.size() == 1
            TopicSubscribers multiLevelTs = firstLevelTs.topicSubscribers.get("#")
            multiLevelTs != null
            multiLevelTs.subscribers != null
            multiLevelTs.subscribers.size() == 1
            matches.size() == 1
        cleanup:
            subscriber.disconnect().join()
    }
    
    def connectAndSubscribe(Mqtt5AsyncClient client, boolean cleanStart, String topic) {
        client.connectWith()
            .cleanStart(cleanStart)
            .send()
            .join()
        client.subscribeWith()
            .topicFilter(topic)
            .qos(MqttQos.AT_MOST_ONCE)
            .send()
            .join()
    }
}
