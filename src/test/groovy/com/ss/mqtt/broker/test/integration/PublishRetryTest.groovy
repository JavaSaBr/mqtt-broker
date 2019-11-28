package com.ss.mqtt.broker.test.integration

import com.ss.mqtt.broker.network.packet.in.ConnectAckInPacket
import com.ss.mqtt.broker.network.packet.out.Connect311OutPacket
import com.ss.mqtt.broker.service.MqttSessionService
import org.springframework.beans.factory.annotation.Autowired

class PublishRetryTest extends IntegrationSpecification {
    
    @Autowired
    MqttSessionService mqttSessionService
    
    def "mqtt5 client should be generate session with one pending packet"() {
        given:
            def client = buildMqtt311MockClient()
            def clientId = generateClientId()
        when:
            client.connect()
            client.send(new Connect311OutPacket(clientId, keepAlive))
        then:
            client.readNext() instanceof ConnectAckInPacket
    }
}
