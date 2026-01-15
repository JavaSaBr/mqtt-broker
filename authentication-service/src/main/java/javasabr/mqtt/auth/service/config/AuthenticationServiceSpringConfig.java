package javasabr.mqtt.auth.service.config;

import java.util.List;
import javasabr.mqtt.auth.api.AuthenticationProvider;
import javasabr.mqtt.auth.api.AuthenticationService;
import javasabr.mqtt.auth.api.CredentialsSource;
import javasabr.mqtt.auth.provider.BasicAuthenticationProvider;
import javasabr.mqtt.auth.service.DefaultAuthenticationService;
import javasabr.mqtt.auth.service.NoOpAuthenticationService;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@CustomLog
@Configuration(proxyBeanMethods = false)
@Import({
    DatabaseCredentialsSourceSpringConfig.class,
    FileCredentialsSourceSpringConfig.class
})
public class AuthenticationServiceSpringConfig {

  @Bean
  @ConditionalOnClass(name = "javasabr.mqtt.auth.provider.BasicAuthenticationProvider")
  @ConditionalOnProperty(name = "authentication.provider.basic.enabled", havingValue = "true")
  AuthenticationProvider basicAuthenticationProvider(List<CredentialsSource> configuredCredentialsSources) {
    log.info("Initializing BasicAuthenticationProvider...");
    return new BasicAuthenticationProvider(configuredCredentialsSources);
  }

  @Bean
  @ConditionalOnBean(AuthenticationProvider.class)
  AuthenticationService authenticationService(
      List<AuthenticationProvider> availableProviders,
      @Value("${authentication.provider.anonymous.enabled:false}") boolean allowAnonymous) {
    log.info("Initializing AuthenticationService...");
    return new DefaultAuthenticationService(availableProviders, allowAnonymous);
  }

  @Bean
  @ConditionalOnMissingBean(AuthenticationService.class)
  AuthenticationService noOpAuthenticationService() {
    log.info("Initializing NoOpAuthenticationService...");
    return new NoOpAuthenticationService();
  }
}
