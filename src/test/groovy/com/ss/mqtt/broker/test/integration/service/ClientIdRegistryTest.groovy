package com.ss.mqtt.broker.test.integration.service

import com.ss.mqtt.broker.service.ClientIdRegistry
import com.ss.mqtt.broker.test.integration.MqttBrokerTest
import com.ss.rlib.common.util.StringUtils
import org.springframework.beans.factory.annotation.Autowired

class ClientIdRegistryTest extends MqttBrokerTest {
    
    @Autowired
    ClientIdRegistry clientIdRegistry
    
    def "should register new client ids"() {
        
        given:
            def clientId1 = "testClientId1"
            def clientId2 = "testClientId2"
        when:
            def result1 = clientIdRegistry.register(clientId1).block()
            def result2 = clientIdRegistry.register(clientId2).block()
        then:
            result1 && result2
        cleanup:
            clientIdRegistry.unregister(clientId1).block()
            clientIdRegistry.unregister(clientId2).block()
    }
    
    def "should not register duplicated client ids"() {
        
        given:
            
            def clientId1 = "testClientId3"
            def clientId2 = "testClientId4"
    
            clientIdRegistry.register(clientId1).block()
            clientIdRegistry.register(clientId2).block()
        
        when:
            def result1 = clientIdRegistry.register(clientId1).block()
            def result2 = clientIdRegistry.register(clientId2).block()
        then:
            !result1 && !result2
        cleanup:
            clientIdRegistry.unregister(clientId1).block()
            clientIdRegistry.unregister(clientId2).block()
    }
    
    def "should unregister exist client ids"() {
        
        given:
            
            def clientId1 = "testClientId5"
            def clientId2 = "testClientId6"
        
            clientIdRegistry.register(clientId1).block()
            clientIdRegistry.register(clientId2).block()
        
        when:
            def result1 = clientIdRegistry.unregister(clientId1).block()
            def result2 = clientIdRegistry.unregister(clientId2).block()
        then:
            result1 && result2
    }
    
    def "should not unregister not exist client ids"() {
        
        given:
            def clientId1 = "testClientId7"
            def clientId2 = "testClientId8"
        when:
            def result1 = clientIdRegistry.unregister(clientId1).block()
            def result2 = clientIdRegistry.unregister(clientId2).block()
        then:
            !result1 && !result2
    }
    
    def "should generate and register new client ids"() {
        
        given:
            def clientId1 = clientIdRegistry.generate().block()
            def clientId2 = clientIdRegistry.generate().block()
        when:
            def result1 = clientIdRegistry.register(clientId1).block()
            def result2 = clientIdRegistry.register(clientId2).block()
        then:
            result1 && result2
            StringUtils.isNotEmpty(clientId1)
            StringUtils.isNotEmpty(clientId2)
        cleanup:
            clientIdRegistry.unregister(clientId1).block()
            clientIdRegistry.unregister(clientId2).block()
    }
    
    def "should generate invalid client ids"() {
        
        given:
            def clientId1 = "testClientId*^&%"
            def clientId2 = "testClientId{}@!"
            def clientId3 = "testClientId9"
        when:
            def result1 = clientIdRegistry.validate(clientId1)
            def result2 = clientIdRegistry.validate(clientId2)
            def result3 = clientIdRegistry.validate(clientId3)
        then:
            !result1 && !result2 && result3
    }
}
