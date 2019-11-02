package com.ss.mqtt.broker.test.integration

import org.junit.Ignore

@Ignore
class PublishTest extends MqttBrokerTest {
    
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
