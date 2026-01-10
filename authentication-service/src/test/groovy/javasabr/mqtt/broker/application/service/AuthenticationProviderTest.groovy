//file:noinspection SpringBootApplicationProperties
package javasabr.mqtt.broker.application.service

import javasabr.mqtt.auth.api.AuthenticationProvider
import javasabr.mqtt.auth.api.CredentialsSource
import javasabr.mqtt.auth.api.exception.AuthenticationConfigException
import javasabr.mqtt.auth.credentials.source.FileCredentialsSource
import javasabr.mqtt.auth.provider.BasicAuthenticationProvider
import javasabr.mqtt.auth.service.config.AuthenticationServiceSpringConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.env.PropertiesPropertySourceLoader
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.core.env.PropertySource
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

import static javasabr.mqtt.auth.api.AuthenticationMethod.BASIC

class AuthenticationProviderTest extends IntegrationSpecification {

  @Autowired
  List<AuthenticationProvider> authenticationProviders

  @TestPropertySource(properties = [
      "authentication.provider.anonymous.enabled=false",
      "authentication.provider.basic.enabled=true",
      "authentication.credentials-source.file.enabled=true",
      "authentication.provider.default.method=basic"
  ])
  static class FileCredentialsSourceTest extends AuthenticationProviderTest {
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

  static class EmptyProviderTest extends Specification {

    def "should fail start application context without any authentication provider"() {
      given:
          PropertySource propertySource = new PropertiesPropertySourceLoader()
              .load("test-props", new ClassPathResource("application-test.properties")).getFirst()
          def appContext = new ApplicationContextRunner()
              .withAllowBeanDefinitionOverriding(true)
              .withUserConfiguration(AuthenticationServiceSpringConfig)
              .withInitializer { context ->
                context.getEnvironment().getPropertySources().addLast(propertySource)
              }
      when:
          appContext
              .withPropertyValues("authentication.provider.anonymous.enabled=false")
              .run({ context ->
                if (context.startupFailure) {
                  throw context.startupFailure
                }
              })
      then:
          def exception = thrown(Exception)
          with(rootCauseOf(exception)) { rootCause ->
            assert rootCause instanceof AuthenticationConfigException
            assert message == "Authenticator providers are not configured"
          }
    }
  }

  static Throwable rootCauseOf(Throwable throwable) {
    Throwable rootCause = throwable
    while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
      rootCause = rootCause.getCause()
    }
    return rootCause
  }
}
