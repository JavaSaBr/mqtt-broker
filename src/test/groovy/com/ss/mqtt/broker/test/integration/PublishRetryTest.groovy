package com.ss.mqtt.broker.test.integration

import com.ss.mqtt.broker.model.MqttSession
import com.ss.mqtt.broker.network.client.MqttClient
import com.ss.mqtt.broker.network.packet.in.PublishInPacket
import com.ss.mqtt.broker.network.packet.out.ConnectAck5OutPacket
import com.ss.mqtt.broker.service.MqttSessionService
import org.springframework.beans.factory.annotation.Autowired

import java.util.concurrent.atomic.AtomicInteger

class PublishRetryTest extends IntegrationSpecification {
    
    @Autowired
    MqttSessionService mqttSessionService
    
  /*  def "mqtt5 client should be generate session with one pending packet"() {
        given:
            def client = buildMqtt5MockClient()
        when:
            client.connect()
            client.send(new ConnectAck5OutPacket(
            
            ))
        then:
            noExceptionThrown()
            publishRetryService.exist(identifier.get().toString())
        when:
            client.disconnect().join()
            Thread.sleep(500)
        then:
            noExceptionThrown()
            !publishRetryService.exist(identifier.get().toString())
    }*/
}
