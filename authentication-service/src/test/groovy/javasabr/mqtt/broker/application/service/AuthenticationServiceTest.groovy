package javasabr.mqtt.broker.application.service

import javasabr.mqtt.auth.api.AuthenticationProvider
import javasabr.mqtt.auth.api.AuthenticationService
import javasabr.mqtt.auth.api.CredentialsSource
import javasabr.mqtt.auth.api.CredentialsSourceType
import javasabr.mqtt.auth.api.MqttCredentials
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource

import java.nio.charset.StandardCharsets

@TestPropertySource(properties = [
    "authentication.provider.anonymous.enabled=false",
    "authentication.provider.basic.enabled=true",
    "authentication.provider.default.method=basic",
    "authentication.credentials-source.file.enabled=true",
    "authentication.credentials-source.database.enabled=true"
])
class AuthenticationServiceTest extends IntegrationSpecification {

  @Autowired
  List<CredentialsSource> credentialsSources
  @Autowired
  List<AuthenticationProvider> authenticationProviders

  @Autowired
  AuthenticationService authenticationService

  def "should authenticate credentials according test database"() {
    given:
        def clientId = "clientId"
        def passwordBytes = password.getBytes(StandardCharsets.UTF_8)
        def request = new MqttCredentials(clientId, userName, passwordBytes, null, new byte[0])
    when:
        def result = authenticationService.authenticate(request).block()
    then:
        result == expectedResult
    where:
        userName | password           | expectedResult
        "user"   | "wrong-password"   | false
        "user"   | "correct-password" | true
        ""       | "correct-password" | false
        "user"   | ""                 | false
        "user1"  | "correct-password" | true
  }

  def "should create file credentials source and basic authentication provider"() {
    given:
        def expectedSourceTypes = [CredentialsSourceType.FILE, CredentialsSourceType.DATABASE]
    expect:
        verifyEach(credentialsSources) { credentialsSource ->
          expectedSourceTypes.remove(credentialsSource.type)
        }
        expectedSourceTypes.isEmpty()
  }
}
