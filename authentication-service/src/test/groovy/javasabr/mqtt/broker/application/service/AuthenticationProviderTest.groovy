//file:noinspection SpringBootApplicationProperties
package javasabr.mqtt.broker.application.service

import javasabr.mqtt.auth.api.AuthenticationProvider
import javasabr.mqtt.auth.api.CredentialsSource
import javasabr.mqtt.auth.api.exception.AuthenticationConfigException
import javasabr.mqtt.auth.credentials.source.FileCredentialsSource
import javasabr.mqtt.auth.service.AnonymousAuthenticationProvider
import javasabr.mqtt.auth.service.config.AuthenticationServiceSpringConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.env.PropertiesPropertySourceLoader
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.core.env.PropertySource
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

import static javasabr.mqtt.auth.api.AuthenticationType.ANONYMOUS

class AuthenticationProviderTest extends IntegrationSpecification {

  @Autowired
  List<AuthenticationProvider> authenticationProviders

  @TestPropertySource(properties = [
      "authentication.allow-anonymous=false",
      "authentication.provider.basic.enabled=true",
      "authentication.provider.basic.credentials-sources.file.enabled=true"
  ])
  static class FileCredentialsSourceTest extends AuthenticationProviderTest {
    @Autowired
    CredentialsSource credentialsSource

    def "should create file credentials source and basic authentication provider"() {
      expect:
          (credentialsSource instanceof FileCredentialsSource)
      and:
          verifyEach(authenticationProviders) { provider ->
            provider.authenticationType != ANONYMOUS
            !(provider instanceof AnonymousAuthenticationProvider)
          }
    }
  }

  @TestPropertySource(properties = [
      "authentication.allow-anonymous=true",
      "authentication.provider.basic.enabled=true",
      "authentication.provider.basic.credentials-sources.file.enabled=true"
  ])
  static class AnonymousProviderTest extends AuthenticationProviderTest {

    def "should create file credentials source and basic authentication provider"() {
      expect:
          authenticationProviders.any { provider ->
            provider.authenticationType == ANONYMOUS && provider instanceof AnonymousAuthenticationProvider
          }
      and:
          authenticationProviders.any { provider ->
            provider.authenticationType != ANONYMOUS && !(provider instanceof AnonymousAuthenticationProvider)
          }
    }
  }

  @TestPropertySource(properties = "authentication.allow-anonymous=true")
  static class AnonymousProvider2Test extends AuthenticationProviderTest {

    def "should create anonymous authentication provider"() {
      expect:
          authenticationProviders.any { provider ->
            provider.authenticationType == ANONYMOUS && provider instanceof AnonymousAuthenticationProvider
          }
      and:
          !authenticationProviders.any { provider ->
            provider.authenticationType != ANONYMOUS && !(provider instanceof AnonymousAuthenticationProvider)
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
              .withPropertyValues("authentication.allow-anonymous=false")
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
