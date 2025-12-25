//file:noinspection SpringBootApplicationProperties
package javasabr.mqtt.broker.application.service

import javasabr.mqtt.auth.api.AnonymousAuthenticationProvider
import javasabr.mqtt.auth.api.AuthenticationProvider
import javasabr.mqtt.auth.api.CredentialsSource
import javasabr.mqtt.auth.api.exception.AuthenticationConfigException
import javasabr.mqtt.auth.credentials.source.DatabaseCredentialsSource
import javasabr.mqtt.auth.credentials.source.FileCredentialsSource
import javasabr.mqtt.auth.service.config.BasicAuthenticationSpringConfig
import javasabr.mqtt.auth.service.config.DatabaseSpringConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.env.PropertiesPropertySourceLoader
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.core.env.PropertySource
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.TestPropertySource
import spock.lang.Ignore
import spock.lang.Specification

class AuthenticationProviderTest extends IntegrationSpecification {

  @Autowired
  List<AuthenticationProvider> authenticationProviders

  @TestPropertySource(properties = [
      "authentication.allow-anonymous=false",
      "authentication.providers[0]=basic",
      "authentication.credentials-sources[0]=database"
  ])
  static class DatabaseCredentialsSourceTest extends AuthenticationProviderTest {
    @Autowired
    CredentialsSource credentialsSource

    def "should create file credentials source and basic authentication provider"() {
      expect:
          credentialsSource instanceof DatabaseCredentialsSource
          verifyEach(authenticationProviders) { provider ->
            provider.name != "anonymous"
            !(provider instanceof AnonymousAuthenticationProvider)
          }
    }
  }

  @TestPropertySource(properties = [
      "authentication.allow-anonymous=false",
      "authentication.providers[0]=basic",
      "authentication.credentials-sources[0]=file"
  ])
  static class FileCredentialsSourceTest extends AuthenticationProviderTest {
    @Autowired
    CredentialsSource credentialsSource

    def "should create file credentials source and basic authentication provider"() {
      expect:
          (credentialsSource instanceof FileCredentialsSource)
      and:
          verifyEach(authenticationProviders) { provider ->
            provider.name != "anonymous"
            !(provider instanceof AnonymousAuthenticationProvider)
          }
    }
  }

  @TestPropertySource(properties = [
      "authentication.allow-anonymous=true",
      "authentication.providers[0]=basic",
      "authentication.credentials-sources[0]=file"
  ])
  static class AnonymousProviderTest extends AuthenticationProviderTest {

    def "should create file credentials source and basic authentication provider"() {
      expect:
          authenticationProviders.any { provider ->
            provider.name == "anonymous" && provider instanceof AnonymousAuthenticationProvider
          }
      and:
          authenticationProviders.any { provider ->
            provider.name != "anonymous" && !(provider instanceof AnonymousAuthenticationProvider)
          }
    }
  }

  @Ignore
  @TestPropertySource(properties = "authentication.allow-anonymous=true")
  static class AnonymousProvider2Test extends AuthenticationProviderTest {

    def "should create anonymous authentication provider"() {
      expect:
          authenticationProviders.any { provider ->
            provider.name == "anonymous" && provider instanceof AnonymousAuthenticationProvider
          }
      and:
          !authenticationProviders.any { provider ->
            provider.name != "anonymous" && !(provider instanceof AnonymousAuthenticationProvider)
          }
    }
  }

  @Ignore
  static class EmptyProviderTest extends Specification {

    def "should fail start application context without any authentication provider"() {
      given:
          PropertySource propertySource = new PropertiesPropertySourceLoader()
              .load("test-props", new ClassPathResource("application-test.properties")).getFirst()
          def appContext = new ApplicationContextRunner()
              .withAllowBeanDefinitionOverriding(true)
              .withUserConfiguration(BasicAuthenticationSpringConfig, DatabaseSpringConfig)
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
    Throwable rootCause = throwable;
    while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
      rootCause = rootCause.getCause();
    }
    return rootCause;
  }
}
