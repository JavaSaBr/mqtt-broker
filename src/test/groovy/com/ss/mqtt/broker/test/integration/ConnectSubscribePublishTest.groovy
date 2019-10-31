package com.ss.mqtt.broker.test.integration

import com.hivemq.client.internal.mqtt.datatypes.MqttTopicImplBuilder
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.datatypes.MqttTopic
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator

class ConnectSubscribePublishTest extends MqttBrokerTest {
    
    def "publisher should publish message to broker"() {
        when:
            
            def subAck = mqttSubscriber.connect()
                .thenCompose({
                    mqttSubscriber.subscribeWith()
                        .topicFilter("test/out")
                        .qos(MqttQos.AT_MOST_ONCE)
                        .callback(System.out.&println)
                        .send()
                })
                .join()
            println subAck
            System.out.println("111111")
            //mqttSubscriber.publishes(MqttGlobalPublishFilter.ALL, System.out.&println);
            
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
    }
}
