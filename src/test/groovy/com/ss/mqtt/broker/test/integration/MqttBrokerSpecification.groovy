package com.ss.mqtt.broker.test.integration

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.ss.mqtt.broker.test.integration.config.MqttBrokerTestConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes = MqttBrokerTestConfig)
class MqttBrokerSpecification extends Specification {
    
    @Autowired
    protected Mqtt5AsyncClient mqttSubscriber
    
    @Autowired
    protected Mqtt5AsyncClient mqttPublisher
}
