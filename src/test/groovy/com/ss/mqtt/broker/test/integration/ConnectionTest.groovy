package com.ss.mqtt.broker.test.integration

import org.junit.Ignore

@Ignore
class ConnectionTest extends MqttBrokerTest {
    
    def "subscriber should connect to broker"() {
        when:
            mqttSubscriber.connect().join()
        then:
            noExceptionThrown()
    }
}
