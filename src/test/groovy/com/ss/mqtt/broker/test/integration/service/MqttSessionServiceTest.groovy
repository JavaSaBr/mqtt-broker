package com.ss.mqtt.broker.test.integration.service

import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode
import com.ss.mqtt.broker.service.ClientIdRegistry
import com.ss.mqtt.broker.service.MqttSessionService
import com.ss.mqtt.broker.test.integration.IntegrationSpecification
import org.springframework.beans.factory.annotation.Autowired

class MqttSessionServiceTest extends IntegrationSpecification {
    
    @Autowired
    ClientIdRegistry clientIdRegistry
    
    @Autowired
    MqttSessionService mqttSessionService
    
    def "subscriber should create and re-use mqtt session"() {
        given:
            def clientId = clientIdRegistry.generate().block()
            def client = buildExternalMqtt5Client(clientId)
        when:
            def shouldNoSession = mqttSessionService.restore(clientId).block()
            def result = client.connect().join()
        then:
            result.reasonCode == Mqtt5ConnAckReasonCode.SUCCESS
            shouldNoSession == null
            mqttSessionService.restore(clientId).block() == null
        when:
            client.disconnect().join()
            Thread.sleep(100)
            def restored = mqttSessionService.restore(clientId).block()
        then:
            restored != null
        when:
            mqttSessionService.store(clientId, restored, externalConnectionConfig.getDefaultSessionExpiryInterval()).block()
            client.connect().join()
            shouldNoSession = mqttSessionService.restore(clientId).block()
        then:
            shouldNoSession == null
        when:
            client.disconnect().join()
            Thread.sleep(100)
            restored = mqttSessionService.restore(clientId).block()
        then:
            restored != null
    }
}
