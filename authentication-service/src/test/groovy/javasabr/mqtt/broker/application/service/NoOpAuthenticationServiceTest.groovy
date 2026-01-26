package javasabr.mqtt.broker.application.service

import javasabr.mqtt.auth.api.AuthenticationService
import javasabr.mqtt.auth.api.MqttCredentials
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource

import java.nio.charset.StandardCharsets

@TestPropertySource(properties = [
    "authentication.provider.anonymous.enabled=false"
])
class NoOpAuthenticationServiceTest extends IntegrationSpecification {

  @Autowired
  AuthenticationService authenticationService

  def "should authenticate credentials according test database"() {
    given:
        def clientId = "clientId"
        def userName = "any_user_name"
        def passwordBytes = "any_password".getBytes(StandardCharsets.UTF_8)
        def request = new MqttCredentials(clientId, userName, passwordBytes, null, new byte[0])
    when:
        def result = authenticationService.authenticate(request)
    then:
        result.block()
  }
}
