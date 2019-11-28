package com.ss.mqtt.broker.test.integration


import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5MessageType
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode

import java.util.concurrent.atomic.AtomicReference

class ConnectSubscribePublishTest extends IntegrationSpecification {
    
    def "publisher should publish message QoS 0"() {
        given:
            def received = new AtomicReference<Mqtt5Publish>()
            def subscriber = buildClient()
            def publisher = buildClient()
        when:
            subscriber.connect().join()
            publisher.connect().join()
            
            def subscribeResult = subscriber.subscribeWith()
                .topicFilter(topicFilter)
                .qos(MqttQos.AT_MOST_ONCE)
                .callback({ publish -> received.set(publish) })
                .send()
                .join()
        
            def publishResult = publisher.publishWith()
                .topic(topicFilter)
                .qos(MqttQos.AT_MOST_ONCE)
                .payload(publishPayload)
                .payloadFormatIndicator(Mqtt5PayloadFormatIndicator.UTF_8)
                .send()
                .join()
    
            Thread.sleep(500)
        then:
            noExceptionThrown()
            
            subscribeResult != null
            subscribeResult.reasonCodes.contains(Mqtt5SubAckReasonCode.GRANTED_QOS_0)
            subscribeResult.type == Mqtt5MessageType.SUBACK
    
            publishResult != null
            publishResult.publish.qos == MqttQos.AT_MOST_ONCE
            publishResult.publish.type == Mqtt5MessageType.PUBLISH
            publishResult.publish.topic.levels.join("/") == topicFilter
        
            received.get() != null
            received.get().qos == MqttQos.AT_MOST_ONCE
            received.get().type == Mqtt5MessageType.PUBLISH
            received.get().topic.levels.join("/") == topicFilter
        cleanup:
            subscriber.disconnect()
            publisher.disconnect()
    }
    
    def "publisher should publish message QoS 1"() {
        given:
            def received = new AtomicReference<Mqtt5Publish>()
            def subscriber = buildClient()
            def publisher = buildClient()
        when:
           
            subscriber.connect().join()
            publisher.connect().join()
            
            def subscribeResult = subscriber.subscribeWith()
                .topicFilter(topicFilter)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback({ publish -> received.set(publish) })
                .send()
                .join()
            
            def publishResult = publisher.publishWith()
                .topic(topicFilter)
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload(publishPayload)
                .payloadFormatIndicator(Mqtt5PayloadFormatIndicator.UTF_8)
                .send()
                .join()
            
            Thread.sleep(500)
        then:
            noExceptionThrown()
            
            subscribeResult != null
            subscribeResult.reasonCodes.contains(Mqtt5SubAckReasonCode.GRANTED_QOS_1)
            subscribeResult.type == Mqtt5MessageType.SUBACK
            
            publishResult != null
            publishResult.publish.qos == MqttQos.AT_LEAST_ONCE
            publishResult.publish.type == Mqtt5MessageType.PUBLISH
            publishResult.publish.topic.levels.join("/") == topicFilter
    
            received.get() != null
            received.get().qos == MqttQos.AT_LEAST_ONCE
            received.get().type == Mqtt5MessageType.PUBLISH
            received.get().topic.levels.join("/") == topicFilter
        cleanup:
            subscriber.disconnect().join()
            publisher.disconnect().join()
    }
}
