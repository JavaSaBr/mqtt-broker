//file:noinspection SpringBootApplicationProperties
package javasabr.mqtt.broker.application.service

import javasabr.mqtt.auth.api.AuthenticationProvider
import javasabr.mqtt.auth.api.CredentialsSource
import javasabr.mqtt.auth.credentials.source.FileCredentialsSource
import javasabr.mqtt.auth.provider.BasicAuthenticationProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource

import static javasabr.mqtt.auth.api.AuthenticationMethod.BASIC

@TestPropertySource(properties = [
    "authentication.provider.anonymous.enabled=false",
    "authentication.provider.basic.enabled=true",
    "authentication.credentials-source.file.enabled=true",
    "authentication.provider.default.method=basic"
])
class AuthenticationProviderTest extends IntegrationSpecification {

  @Autowired
  List<AuthenticationProvider> authenticationProviders

  @Autowired
  CredentialsSource credentialsSource

  def "should create file credentials source and basic authentication provider"() {
    expect:
        (credentialsSource instanceof FileCredentialsSource)
    and:
        verifyEach(authenticationProviders) { provider ->
          provider.authenticationMethod == BASIC
          provider instanceof BasicAuthenticationProvider
        }
  }

}
