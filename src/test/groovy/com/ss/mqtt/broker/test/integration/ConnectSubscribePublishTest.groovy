package com.ss.mqtt.broker.test.integration

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5MessageType
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode

class ConnectSubscribePublishTest extends MqttBrokerTest {
    
    def "publisher should publish message to broker"() {
        given:
            Mqtt5Publish receivedMessage = null
        when:
            
            mqttSubscriber.connect().join()
            mqttPublisher.connect().join()
        
            def subscribeResult = mqttSubscriber.subscribeWith()
                .topicFilter(topicFilter)
                .qos(MqttQos.AT_MOST_ONCE)
                .callback({ publish -> receivedMessage = publish })
                .send()
                .join()
            
            def publishResult = mqttPublisher.publishWith()
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
            
            receivedMessage != null
            receivedMessage.qos == MqttQos.AT_MOST_ONCE
            receivedMessage.type == Mqtt5MessageType.PUBLISH
            receivedMessage.topic.levels.join("/") == topicFilter
    }
}
