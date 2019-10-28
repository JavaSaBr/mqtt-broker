package com.ss.mqtt.broker.test.integration

class ConnectSubscribePublishTest extends MqttBrokerSpecification {
    
    def "publisher should publish message to broker"() {
        when:
            
            mqttSubscriber.connect()
                .thenCompose({
                    mqttSubscriber.subscribeWith()
                        .topicFilter("test/out")
                        .callback({
                            publish ->
                                publish.getPayload().ifPresent({
                                    payload -> System.out.println(payload)
                                })
                        })
                        .send()
                }).join()
            
            mqttPublisher.connect()
                .thenCompose({
                    mqttPublisher.publishWith()
                        .topic("test/out")
                        .payload("payload".getBytes())
                        .send()
                }).join()
        then:
            noExceptionThrown()
    }
}
