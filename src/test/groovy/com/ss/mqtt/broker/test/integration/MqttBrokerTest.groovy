package com.ss.mqtt.broker.test.integration

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.ss.mqtt.broker.test.integration.config.MqttBrokerTestConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.nio.charset.StandardCharsets

@ContextConfiguration(classes = MqttBrokerTestConfig)
class MqttBrokerTest extends Specification {
    
    @Autowired
    InetSocketAddress deviceNetworkAddress
    
    public static final ENCODING = StandardCharsets.UTF_8
    public static final topicFilter = "topic/Filter"
    public static final publishPayload = "publishPayload".getBytes(ENCODING)
    
    @Autowired
    Mqtt5AsyncClient mqttSubscriber
    
    @Autowired
    Mqtt5AsyncClient mqttPublisher
    
    protected buildClient(String clientId) {
        return MqttClient.builder()
            .identifier(clientId)
            .serverHost(deviceNetworkAddress.getHostName())
            .serverPort(deviceNetworkAddress.getPort())
            .useMqttVersion5()
            .build()
            .toAsync()
    }
    
    protected static connectWith(Mqtt5AsyncClient client, String user, String pass){
        return client.connectWith()
            .simpleAuth()
            .username(user)
            .password(pass.getBytes(ENCODING))
            .applySimpleAuth()
            .send()
            .join()
    }
}
