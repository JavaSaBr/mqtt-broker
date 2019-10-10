package com.ss.mqtt.broker.test.integration

class PublishTest extends MqttBrokerSpecification {
    
    def "publisher should publish message to broker"() {
        when:
            mqttPublisher.connect()
                .thenCompose({ mqttPublisher.publishWith()
                        .topic("test/out")
                        .payload("payload".getBytes())
                        .send() }).join()
        then:
            noExceptionThrown()
    }
}
