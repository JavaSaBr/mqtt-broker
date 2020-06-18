package com.ss.mqtt.broker.test.integration

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.message.Mqtt3MessageType
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAckReturnCode
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5MessageType
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode

import java.util.concurrent.atomic.AtomicReference

class ConnectSubscribePublishTest extends IntegrationSpecification {
    
    def "publisher should publish message QoS 0 using mqtt 3.1.1"() {
        given:
            def received = new AtomicReference<Mqtt3Publish>()
            def subscriber = buildExternalMqtt311Client()
            def subscriberId = subscriber.getConfig().clientIdentifier.get().toString()
            def publisher = buildExternalMqtt311Client()
        when:
            subscriber.connect().join()
            publisher.connect().join()
            
            def subscribeResult = subscribe(subscriber, subscriberId, MqttQos.AT_MOST_ONCE, received)
            def publishResult = publish(publisher, subscriberId, MqttQos.AT_MOST_ONCE)
            
            Thread.sleep(100)
        then:
            noExceptionThrown()
            
            subscribeResult != null
            subscribeResult.returnCodes.contains(Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_0)
            subscribeResult.type == Mqtt3MessageType.SUBACK
            
            publishResult != null
            publishResult.qos == MqttQos.AT_MOST_ONCE
            publishResult.type == Mqtt3MessageType.PUBLISH
            
            received.get() != null
            received.get().qos == MqttQos.AT_MOST_ONCE
            received.get().type == Mqtt3MessageType.PUBLISH
        cleanup:
            subscriber.disconnect().join()
            publisher.disconnect().join()
    }
    
    def "publisher should publish message QoS 0 using mqtt 5"() {
        given:
            def received = new AtomicReference<Mqtt5Publish>()
            def subscriber = buildExternalMqtt5Client()
            def subscriberId = subscriber.getConfig().clientIdentifier.get().toString()
            def publisher = buildExternalMqtt5Client()
        when:
            subscriber.connect().join()
            publisher.connect().join()
    
            def subscribeResult = subscribe(subscriber, subscriberId, MqttQos.AT_MOST_ONCE, received)
            def publishResult = publish(publisher, subscriberId, MqttQos.AT_MOST_ONCE)

            Thread.sleep(100)
        then:
            noExceptionThrown()
            
            subscribeResult != null
            subscribeResult.reasonCodes.contains(Mqtt5SubAckReasonCode.GRANTED_QOS_0)
            subscribeResult.type == Mqtt5MessageType.SUBACK
    
            publishResult != null
            publishResult.publish.qos == MqttQos.AT_MOST_ONCE
            publishResult.publish.type == Mqtt5MessageType.PUBLISH
        
            received.get() != null
            received.get().qos == MqttQos.AT_MOST_ONCE
            received.get().type == Mqtt5MessageType.PUBLISH
        cleanup:
            subscriber.disconnect().join()
            publisher.disconnect().join()
    }
    
    def "publisher should publish message QoS 1 using mqtt 3.1.1"() {
        given:
            def received = new AtomicReference<Mqtt3Publish>()
            def subscriber = buildExternalMqtt311Client()
            def subscriberId = subscriber.getConfig().clientIdentifier.get().toString()
            def publisher = buildExternalMqtt311Client()
        when:
            subscriber.connect().join()
            publisher.connect().join()
            
            def subscribeResult = subscribe(subscriber, subscriberId, MqttQos.AT_LEAST_ONCE, received)
            def publishResult = publish(publisher, subscriberId, MqttQos.AT_LEAST_ONCE)
            
            Thread.sleep(100)
        then:
            noExceptionThrown()
            
            subscribeResult != null
            subscribeResult.returnCodes.contains(Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_1)
            subscribeResult.type == Mqtt3MessageType.SUBACK
            
            publishResult != null
            publishResult.qos == MqttQos.AT_LEAST_ONCE
            publishResult.type == Mqtt3MessageType.PUBLISH
            
            received.get() != null
            received.get().qos == MqttQos.AT_LEAST_ONCE
            received.get().type == Mqtt3MessageType.PUBLISH
        cleanup:
            subscriber.disconnect().join()
            publisher.disconnect().join()
    }
    
    def "publisher should publish message QoS 1 using mqtt 5"() {
        given:
            def received = new AtomicReference<Mqtt5Publish>()
            def subscriber = buildExternalMqtt5Client()
            def subscriberId = subscriber.getConfig().clientIdentifier.get().toString()
            def publisher = buildExternalMqtt5Client()
        when:
           
            subscriber.connect().join()
            publisher.connect().join()
    
            def subscribeResult = subscribe(subscriber, subscriberId, MqttQos.AT_LEAST_ONCE, received)
            def publishResult = publish(publisher, subscriberId, MqttQos.AT_LEAST_ONCE)
        
            Thread.sleep(100)
        then:
            noExceptionThrown()
            
            subscribeResult != null
            subscribeResult.reasonCodes.contains(Mqtt5SubAckReasonCode.GRANTED_QOS_1)
            subscribeResult.type == Mqtt5MessageType.SUBACK
            
            publishResult != null
            publishResult.publish.qos == MqttQos.AT_LEAST_ONCE
            publishResult.publish.type == Mqtt5MessageType.PUBLISH
    
            received.get() != null
            received.get().qos == MqttQos.AT_LEAST_ONCE
            received.get().type == Mqtt5MessageType.PUBLISH
        cleanup:
            subscriber.disconnect().join()
            publisher.disconnect().join()
    }
    
    def "publisher should publish message QoS 2 using mqtt 3.1.1"() {
        given:
            def received = new AtomicReference<Mqtt3Publish>()
            def subscriber = buildExternalMqtt311Client()
            def subscriberId = subscriber.getConfig().clientIdentifier.get().toString()
            def publisher = buildExternalMqtt311Client()
        when:
            subscriber.connect().join()
            publisher.connect().join()
            
            def subscribeResult = subscribe(subscriber, subscriberId, MqttQos.EXACTLY_ONCE, received)
            def publishResult = publish(publisher, subscriberId, MqttQos.EXACTLY_ONCE)
            
            Thread.sleep(100)
        then:
            noExceptionThrown()
            
            subscribeResult != null
            subscribeResult.returnCodes.contains(Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_2)
            subscribeResult.type == Mqtt3MessageType.SUBACK
            
            publishResult != null
            publishResult.qos == MqttQos.EXACTLY_ONCE
            publishResult.type == Mqtt3MessageType.PUBLISH
            
            received.get() != null
            received.get().qos == MqttQos.EXACTLY_ONCE
            received.get().type == Mqtt3MessageType.PUBLISH
        cleanup:
            subscriber.disconnect().join()
            publisher.disconnect().join()
    }
    
    def "publisher should publish message QoS 2 using mqtt 5"() {
        given:
            def received = new AtomicReference<Mqtt5Publish>()
            def subscriber = buildExternalMqtt5Client()
            def subscriberId = subscriber.getConfig().clientIdentifier.get().toString()
            def publisher = buildExternalMqtt5Client()
        when:
            
            subscriber.connect().join()
            publisher.connect().join()
    
            def subscribeResult = subscribe(subscriber, subscriberId, MqttQos.EXACTLY_ONCE, received)
            def publishResult = publish(publisher, subscriberId, MqttQos.EXACTLY_ONCE)
            
            Thread.sleep(100)
        then:
            noExceptionThrown()
            
            subscribeResult != null
            subscribeResult.reasonCodes.contains(Mqtt5SubAckReasonCode.GRANTED_QOS_2)
            subscribeResult.type == Mqtt5MessageType.SUBACK
            
            publishResult != null
            publishResult.publish.qos == MqttQos.EXACTLY_ONCE
            publishResult.publish.type == Mqtt5MessageType.PUBLISH
            
            received.get() != null
            received.get().qos == MqttQos.EXACTLY_ONCE
            received.get().type == Mqtt5MessageType.PUBLISH
        cleanup:
            subscriber.disconnect().join()
            publisher.disconnect().join()
    }
    
    def publish(Mqtt5AsyncClient publisher, String subscriberId, MqttQos qos) {
        return publisher.publishWith()
            .topic("test/$subscriberId")
            .qos(qos)
            .payload(publishPayload)
            .payloadFormatIndicator(Mqtt5PayloadFormatIndicator.UTF_8)
            .send()
            .join()
    }
    
    def subscribe(
        Mqtt5AsyncClient subscriber,
        String subscriberId,
        MqttQos qos,
        AtomicReference<Mqtt5Publish> received
    ) {
        return subscriber.subscribeWith()
            .topicFilter("test/$subscriberId")
            .qos(qos)
            .callback({ publish -> received.set(publish) })
            .send()
            .join()
    }
    
    def publish(Mqtt3AsyncClient publisher, String subscriberId, MqttQos qos) {
        return publisher.publishWith()
            .topic("test/$subscriberId")
            .qos(qos)
            .payload(publishPayload)
            .send()
            .join()
    }
    
    def subscribe(
        Mqtt3AsyncClient subscriber,
        String subscriberId,
        MqttQos qos,
        AtomicReference<Mqtt3Publish> received
    ) {
        return subscriber.subscribeWith()
            .topicFilter("test/$subscriberId")
            .qos(qos)
            .callback({ publish -> received.set(publish) })
            .send()
            .join()
    }
}
