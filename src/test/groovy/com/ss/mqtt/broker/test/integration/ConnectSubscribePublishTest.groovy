package com.ss.mqtt.broker.test.integration

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5MessageType
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode

class ConnectSubscribePublishTest extends MqttBrokerTest {
    
    def "publisher should publish message to broker"() {
        given:
            def topic = 'test/out'
            def connectResult = null
            def publishResult = null
        when:
            def subscribeResult = mqttSubscriber.connect()
                .thenCompose({ connect ->
                    connectResult = connect
                    mqttSubscriber.subscribeWith()
                        .topicFilter(topic)
                        .qos(MqttQos.AT_MOST_ONCE)
                        .callback({ publish -> publishResult = publish })
                        .send()
                })
                .join()
            
            mqttPublisher.connect()
                .thenCompose({
                    mqttPublisher.publishWith()
                        .topic("test/out")
                        .qos(MqttQos.AT_MOST_ONCE)
                        .payload("payload".getBytes())
                        .payloadFormatIndicator(Mqtt5PayloadFormatIndicator.UTF_8)
                        .send()
                })
                .join()
            
            Thread.sleep(500)
        then:
            noExceptionThrown()
            
            subscribeResult != null
            subscribeResult.getReasonCodes().contains(Mqtt5SubAckReasonCode.GRANTED_QOS_0)
            subscribeResult.getType() == Mqtt5MessageType.SUBACK
            
            publishResult != null
            publishResult.getQos() == MqttQos.AT_MOST_ONCE
            publishResult.getType() == Mqtt5MessageType.PUBLISH
            publishResult.getTopic().getLevels().join("/") == topic
    }
}
