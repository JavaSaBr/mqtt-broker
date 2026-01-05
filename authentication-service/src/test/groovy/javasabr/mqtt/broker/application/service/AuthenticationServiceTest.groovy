package javasabr.mqtt.broker.application.service

import javasabr.mqtt.auth.api.AuthenticationMethod
import javasabr.mqtt.auth.api.AuthenticationProvider
import javasabr.mqtt.auth.api.AuthenticationService
import javasabr.mqtt.auth.api.CredentialsSource
import javasabr.mqtt.auth.api.CredentialsSourceType
import javasabr.mqtt.auth.api.MqttCredentials
import javasabr.mqtt.auth.service.AnonymousAuthenticationProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared

import java.nio.charset.StandardCharsets

@TestPropertySource(properties = [
    "authentication.method.anonymous.enabled=false",
    "authentication.method.basic.enabled=true",
    "authentication.method.default.type=basic",
    "authentication.credentials-source.file.enabled=true",
    "authentication.credentials-source.database.enabled=true"
])
@Testcontainers
class AuthenticationServiceTest extends IntegrationSpecification {

  @Autowired
  List<CredentialsSource> credentialsSources
  @Autowired
  List<AuthenticationProvider> authenticationProviders

  @Shared
  static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:9.6.12")
      .withDatabaseName("testdb")
      .withUsername("user")
      .withPassword("")

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    postgreSQLContainer.start()
    registry.add(
        "authentication.credentials-source.database.port",
        { "${postgreSQLContainer.getMappedPort(5432)}" })
  }

  @Autowired
  AuthenticationService authenticationService

  def "should authenticate credentials according test database"() {
    given:
        def passwordBytes = password.getBytes(StandardCharsets.UTF_8)
        def request = new MqttCredentials(userName, passwordBytes, null, new byte[0])
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
    and:
        verifyEach(authenticationProviders) { provider ->
          provider.authenticationMethod != AuthenticationMethod.ANONYMOUS
          !(provider instanceof AnonymousAuthenticationProvider)
        }
  }
}
