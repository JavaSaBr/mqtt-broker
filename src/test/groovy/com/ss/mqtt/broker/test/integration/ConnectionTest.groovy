package com.ss.mqtt.broker.test.integration

class ConnectionTest extends MqttBrokerTest {
    
    def "subscriber should connect to broker"() {
        when:
            mqttSubscriber.connect().join()
        then:
            noExceptionThrown()
    }
}
