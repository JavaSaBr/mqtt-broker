package javasabr.mqtt.auth.service.config;

import java.util.List;
import javasabr.mqtt.auth.api.AuthenticationMethod;
import javasabr.mqtt.auth.api.AuthenticationProvider;
import javasabr.mqtt.auth.api.AuthenticationService;
import javasabr.mqtt.auth.api.CredentialsSource;
import javasabr.mqtt.auth.provider.BasicAuthenticationProvider;
import javasabr.mqtt.auth.service.AnonymousAuthenticationProvider;
import javasabr.mqtt.auth.service.DefaultAuthenticationService;
import javasabr.mqtt.auth.service.config.property.AuthenticationProperties;
import javasabr.mqtt.auth.service.config.property.DatabaseCredentialsSourceProperties;
import javasabr.mqtt.auth.service.config.property.FileCredentialsSourceProperties;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Autowired;
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
    FileCredentialsSourceProperties.class,
    DatabaseCredentialsSourceProperties.class
})
public class AuthenticationServiceSpringConfig {

  @Bean
  AuthenticationService authenticationService(
      List<AuthenticationProvider> availableProviders,
      AuthenticationProperties authenticationProperties,
      @Autowired(required = false) AnonymousAuthenticationProvider anonymousProvider) {
    log.info("Initializing AuthenticationService...");
    AuthenticationMethod defaultAuthenticationMethod = authenticationProperties.defaultMethod();
    return new DefaultAuthenticationService(availableProviders, defaultAuthenticationMethod, anonymousProvider);
  }

  @Bean
  @ConditionalOnClass(name = "javasabr.mqtt.auth.provider.BasicAuthenticationProvider")
  @ConditionalOnProperty(name = "authentication.method.basic.enabled", havingValue = "true")
  @ConditionalOnBean(CredentialsSource.class)
  AuthenticationProvider basicAuthenticationProvider(List<CredentialsSource> configuredCredentialsSources) {
    return new BasicAuthenticationProvider(configuredCredentialsSources);
  }

  @Bean
  @ConditionalOnProperty(name = "authentication.allow-anonymous", havingValue = "true")
  AuthenticationProvider anonymousAuthenticationProvider() {
    return new AnonymousAuthenticationProvider();
  }
}
