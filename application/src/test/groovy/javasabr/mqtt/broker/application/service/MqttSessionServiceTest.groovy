package javasabr.mqtt.broker.application.service

import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode
import javasabr.mqtt.broker.application.IntegrationSpecification
import javasabr.mqtt.network.session.ConfigurableNetworkMqttSession
import javasabr.mqtt.service.ClientIdRegistry
import javasabr.mqtt.service.session.MqttSessionService
import org.springframework.beans.factory.annotation.Autowired

import java.time.Duration

class MqttSessionServiceTest extends IntegrationSpecification {

  @Autowired
  ClientIdRegistry clientIdRegistry

  @Autowired
  MqttSessionService mqttSessionService

  def "client should create fresh session each time"() {
    given:
        def clientId = clientIdRegistry.generate().block()
        def client = buildExternalMqtt5Client(clientId)
        def previousSession = mqttSessionService.createClean(clientId).block()
        if (previousSession instanceof ConfigurableNetworkMqttSession) {
          previousSession.expiryInterval(Duration.ofHours(1))
        }
        mqttSessionService.store(clientId, previousSession).block()
    when:
        def connectionResult = client.connectWith()
            .cleanStart(true)
            .send()
            .join()
    then:
        connectionResult.reasonCode == Mqtt5ConnAckReasonCode.SUCCESS
    when:
        client.disconnect().join()
        Thread.sleep(100)
        def restored = mqttSessionService.restore(clientId).block()
    then:
        restored != null
        restored != previousSession
  }
  
  def "client should re-use mqtt session between connections"() {
    given:
        def clientId = clientIdRegistry.generate().block()
        def client = buildExternalMqtt5Client(clientId)
    when:
        def restoredSession = mqttSessionService.restore(clientId).block()
    then: 'there no any stored session for this client'
        restoredSession == null
    when:
        def connectionResult = client.connectWith()
            .cleanStart(false)
            .send()
            .join()
    then:
        connectionResult.reasonCode == Mqtt5ConnAckReasonCode.SUCCESS
    when:
        mqttSessionService.restore(clientId).block()
    then: 'there is active session for this client'
        def exception = thrown(IllegalStateException)
        exception.message == "Client:[$clientId] already has active session"
    when:
        client.disconnect().join()
        Thread.sleep(100)
        def restored = mqttSessionService.restore(clientId).block()
    then:
        restored != null
        restored.clientId() == clientId
        restored.expiryInterval() != null
    when:
        mqttSessionService.restore(clientId).block()
    then: 'The session was already restored'
        exception = thrown(IllegalStateException)
        exception.message == "Client:[$clientId] already has active session"
    when:
        mqttSessionService.store(clientId, restored).block()
        connectionResult = client.connectWith()
            .cleanStart(false)
            .send()
            .join()
    then:
        connectionResult.reasonCode == Mqtt5ConnAckReasonCode.SUCCESS
    when:
        client.disconnect().join()
        Thread.sleep(100)
        def restored2 = mqttSessionService.restore(clientId).block()
    then: 'should be the same session instance'
        restored2 != null
        restored2 == restored
  }
}
