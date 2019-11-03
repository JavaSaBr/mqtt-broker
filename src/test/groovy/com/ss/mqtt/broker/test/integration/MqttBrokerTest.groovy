package com.ss.mqtt.broker.test.integration

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.ss.mqtt.broker.test.integration.config.MqttBrokerTestConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.nio.charset.StandardCharsets

@ContextConfiguration(classes = MqttBrokerTestConfig)
class MqttBrokerTest extends Specification {
    
    public static final ENCODING = StandardCharsets.UTF_8
    public static final topicFilter = "topic/Filter"
    public static final publishPayload = "publishPayload".getBytes(ENCODING)
    
    @Autowired
    Mqtt5AsyncClient mqttSubscriber
    
    @Autowired
    Mqtt5AsyncClient mqttPublisher
}
