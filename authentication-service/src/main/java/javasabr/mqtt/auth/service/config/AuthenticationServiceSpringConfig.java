package javasabr.mqtt.auth.service.config;

import java.util.List;
import javasabr.mqtt.auth.api.AuthenticationProvider;
import javasabr.mqtt.auth.api.AuthenticationService;
import javasabr.mqtt.auth.api.CredentialsSource;
import javasabr.mqtt.auth.provider.BasicAuthenticationProvider;
import javasabr.mqtt.auth.service.AnonymousAuthenticationProvider;
import javasabr.mqtt.auth.service.DefaultAuthenticationService;
import javasabr.mqtt.auth.service.config.property.AuthenticationMethodProperties;
import javasabr.mqtt.auth.service.config.property.AuthenticationProperties;
import lombok.CustomLog;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@CustomLog
@Configuration(proxyBeanMethods = false)
@Import({
    DatabaseCredentialsSourceSpringConfig.class,
    FileCredentialsSourceSpringConfig.class
})
@EnableConfigurationProperties({
    AuthenticationProperties.class,
    AuthenticationMethodProperties.class
})
public class AuthenticationServiceSpringConfig {

  @Bean
  AuthenticationService authenticationService(
      List<AuthenticationProvider> availableProviders,
      AuthenticationProperties authenticationProperties) {
    log.info("Initializing AuthenticationService...");
    return new DefaultAuthenticationService(availableProviders, authenticationProperties);
  }

  @Bean
  @ConditionalOnClass(name = "javasabr.mqtt.auth.provider.BasicAuthenticationProvider")
  @ConditionalOnProperty(name = "authentication.provider.basic.enabled", havingValue = "true")
  @ConditionalOnBean(CredentialsSource.class)
  AuthenticationProvider basicAuthenticationProvider(List<CredentialsSource> configuredCredentialsSources) {
    return new BasicAuthenticationProvider(configuredCredentialsSources);
  }

  @Bean
  @ConditionalOnProperty(name = "authentication.provider.anonymous.enabled", havingValue = "true")
  AuthenticationProvider anonymousAuthenticationProvider() {
    return new AnonymousAuthenticationProvider();
  }
}
