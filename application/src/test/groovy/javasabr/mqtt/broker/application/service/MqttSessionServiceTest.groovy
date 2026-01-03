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

  def "should create fresh session if client request it"() {
    given:
        def clientId = fromAsync(clientIdRegistry.generate())
        def client = buildExternalMqtt5Client(clientId)
        def previousSession = fromAsync(mqttSessionService.createClean(clientId)) as ConfigurableNetworkMqttSession
        previousSession.expiryInterval(Duration.ofHours(1))
        waitForAsync(mqttSessionService.store(clientId, previousSession))
    when:
        def connectionResult = fromAsync(client.connectWith()
            .cleanStart(true)
            .sessionExpiryInterval(120)
            .send())
    then:
        with(connectionResult) {
          !isSessionPresent()
          getReasonCode() == Mqtt5ConnAckReasonCode.SUCCESS
        }
    when:
        waitForAsync(client.disconnect())
        Thread.sleep(100)
        def restored = fromAsync(mqttSessionService.restore(clientId))
    then:
        restored != null
        restored != previousSession
  }

  def "should not store session for client which doesn't require it"() {
    given:
        def clientId = fromAsync(clientIdRegistry.generate())
        def client = buildExternalMqtt5Client(clientId)
    when:
        def connectionResult = fromAsync(client.connectWith().send())
    then:
        with(connectionResult) {
          !isSessionPresent()
          getReasonCode() == Mqtt5ConnAckReasonCode.SUCCESS
        }
    when:
        waitForAsync(client.disconnect())
        Thread.sleep(100)
        def restored = fromAsync(mqttSessionService.restore(clientId))
    then:
        restored == null
  }

  def "should always store session for < MQTT 5.0 clients"() {
    given:
        def clientId = fromAsync(clientIdRegistry.generate())
        def client = buildExternalMqtt311Client(clientId)
    when:
        def connectionResult = fromAsync(client.connect())
    then:
        !connectionResult.isSessionPresent()
    when:
        waitForAsync(client.disconnect())
        Thread.sleep(100)
        def restored = fromAsync(mqttSessionService.restore(clientId))
    then:
        restored != null
  }
  
  def "client should re-use MQTT session between connections"() {
    given:
        def clientId = fromAsync(clientIdRegistry.generate())
        def client = buildExternalMqtt5Client(clientId)
    when:
        def restoredSession = mqttSessionService.restore(clientId).block()
    then: 'there no any stored session for this client'
        restoredSession == null
    when:
        def connectionResult = fromAsync(client.connectWith()
            .cleanStart(false)
            .sessionExpiryInterval(120)
            .send())
    then:
        with(connectionResult) {
          !isSessionPresent()
          getReasonCode() == Mqtt5ConnAckReasonCode.SUCCESS
        }
    when:
        waitForAsync(mqttSessionService.restore(clientId))
    then: 'there is active session for this client'
        def exception = thrown(IllegalStateException)
        exception.message == "Client:[$clientId] already has active session"
    when:
        waitForAsync(client.disconnect())
        Thread.sleep(100)
        def restored = fromAsync(mqttSessionService.restore(clientId))
    then:
        restored != null
        restored.clientId() == clientId
        restored.expiryInterval() != null
    when:
        waitForAsync(mqttSessionService.restore(clientId))
    then: 'The session was already restored'
        exception = thrown(IllegalStateException)
        exception.message == "Client:[$clientId] already has active session"
    when:
        waitForAsync(mqttSessionService.store(clientId, restored))
        connectionResult = fromAsync(client.connectWith()
            .cleanStart(false)
            .sessionExpiryInterval(120)
            .send())
    then:
        with(connectionResult) {
          isSessionPresent()
          getReasonCode() == Mqtt5ConnAckReasonCode.SUCCESS
        }
    when:
        waitForAsync(client.disconnect())
        Thread.sleep(100)
        def restored2 = fromAsync(mqttSessionService.restore(clientId))
    then: 'should be the same session instance'
        restored2 != null
        restored2 == restored
  }
}
