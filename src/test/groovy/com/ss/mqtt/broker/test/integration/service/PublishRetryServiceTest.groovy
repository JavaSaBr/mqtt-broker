package com.ss.mqtt.broker.test.integration.service

import com.ss.mqtt.broker.model.MqttSession
import com.ss.mqtt.broker.network.client.MqttClient
import com.ss.mqtt.broker.network.packet.in.PublishInPacket
import com.ss.mqtt.broker.service.MqttSessionService
import com.ss.mqtt.broker.service.PublishRetryService
import com.ss.mqtt.broker.test.integration.IntegrationSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

import java.util.concurrent.atomic.AtomicInteger

class PublishRetryServiceTest extends IntegrationSpecification {
    
    @Autowired
    PublishRetryService publishRetryService
    
    @Autowired
    MqttSessionService mqttSessionService
    
    @Value('${publish.pending.check.interval}')
    int checkInterval
    
    @Value('${publish.retry.interval}')
    int retryInterval
    
    def "client should be presented in re-try service after connecting"() {
        given:
            def client = buildClient()
            def config = client.getConfig()
            def identifier = config.getClientIdentifier()
        when:
            client.connect().join()
        then:
            noExceptionThrown()
            publishRetryService.exist(identifier.get().toString())
        when:
            client.disconnect().join()
            Thread.sleep(500)
        then:
            noExceptionThrown()
            !publishRetryService.exist(identifier.get().toString())
    }
    
    def "service should check expired and pending messages from time to time"() {
        given:
            
            def session = Mock(MqttSession)
            def client = Stub(MqttClient) {
                getSession() >> session
            }
        
        when:
            publishRetryService.register(client)
            Thread.sleep(checkInterval)
        then:
            1 * session.removeExpiredPackets()
            1 * session.resendPendingPacketsAsync(client, retryInterval)
        cleanup:
            publishRetryService.unregister(client)
    }
    
    def "service should remove expired and retry pending messages from time to time"() {
        given:
    
            def session = mqttSessionService.create(UUID.randomUUID().toString()).block()
            def retryAttempts = new AtomicInteger()
        
            def handler = Stub(MqttSession.PendingPacketHandler) {
                retryAsync(_,_,_) >> {
                    retryAttempts.incrementAndGet()
                }
            }
            def client = Stub(MqttClient) {
                getSession() >> session
            }
    
            publishRetryService.register(client)
        when:
            def publish = Stub(PublishInPacket) {
                getMessageExpiryInterval() >> 1L
            }
        
            session.registerOutPublish(publish, handler, 1)
        
            Thread.sleep(1000 + checkInterval)
        then:
            retryAttempts.get() == 0
            !session.hasOutPending()
        when:
            publish = Stub(PublishInPacket) {
                getMessageExpiryInterval() >> 1000L
            }
        
            session.registerOutPublish(publish, handler, 2)
    
            Thread.sleep(retryInterval + checkInterval)
        then:
            retryAttempts.get() == 1
            session.hasOutPending()
        cleanup:
            publishRetryService.unregister(client)
    }
}
