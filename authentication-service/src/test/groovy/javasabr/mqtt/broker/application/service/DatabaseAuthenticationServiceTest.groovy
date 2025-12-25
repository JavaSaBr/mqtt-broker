package javasabr.mqtt.broker.application.service

import javasabr.mqtt.auth.api.AnonymousAuthenticationProvider
import javasabr.mqtt.auth.api.AuthRequest
import javasabr.mqtt.auth.api.AuthenticationProvider
import javasabr.mqtt.auth.api.AuthenticationService
import javasabr.mqtt.auth.api.CredentialsSource
import javasabr.mqtt.auth.credentials.source.DatabaseCredentialsSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared

import java.nio.charset.StandardCharsets

@TestPropertySource(properties = [
    "authentication.allow-anonymous=false",
    "authentication.credentials-sources[0]=database",
    "authentication.providers[0]=basic"
])
@Testcontainers
class DatabaseAuthenticationServiceTest extends IntegrationSpecification {

  @Autowired
  CredentialsSource credentialsSource
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
    registry.add("persistence.database.port", { "${postgreSQLContainer.getMappedPort(5432)}" })
  }

  @Autowired
  AuthenticationService authenticationService

  def "should authenticate credentials according [credentials/test] file"() {
    given:
        def passwordBytes = password.getBytes(StandardCharsets.UTF_8)
        def request = new AuthRequest(userName, passwordBytes, "", new byte[0])
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
  }

  def "should create file credentials source and basic authentication provider"() {
    expect:
        credentialsSource instanceof DatabaseCredentialsSource
        verifyEach(authenticationProviders) { provider ->
          provider.name != "anonymous"
          !(provider instanceof AnonymousAuthenticationProvider)
        }
  }
}
