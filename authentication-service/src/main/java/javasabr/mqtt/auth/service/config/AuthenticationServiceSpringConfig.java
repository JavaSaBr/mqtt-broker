package javasabr.mqtt.auth.service.config;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import javasabr.mqtt.auth.api.AuthenticationProvider;
import javasabr.mqtt.auth.api.AuthenticationService;
import javasabr.mqtt.auth.api.CredentialsSource;
import javasabr.mqtt.auth.api.exception.AuthenticationConfigException;
import javasabr.mqtt.auth.credentials.source.DatabaseCredentialsSource;
import javasabr.mqtt.auth.credentials.source.FileCredentialsSource;
import javasabr.mqtt.auth.provider.BasicAuthenticationProvider;
import javasabr.mqtt.auth.service.AnonymousAuthenticationProvider;
import javasabr.mqtt.auth.service.DefaultAuthenticationService;
import javasabr.mqtt.auth.service.config.annotation.ConditionalOnAnonymousProvider;
import javasabr.mqtt.auth.service.config.annotation.ConditionalOnBasicAuthenticationProvider;
import javasabr.mqtt.auth.service.config.annotation.ConditionalOnDatabaseCredentialsSource;
import javasabr.mqtt.auth.service.config.annotation.ConditionalOnFileCredentialsSource;
import javasabr.mqtt.auth.service.config.property.AuthenticationProperties;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayCollectors;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;

@CustomLog
@Configuration(proxyBeanMethods = false)
@Import(DatabaseSpringConfig.class)
@EnableConfigurationProperties(AuthenticationProperties.class)
public class AuthenticationServiceSpringConfig {

  @Bean
  AuthenticationService authenticationService(List<AuthenticationProvider> providers) {
    log.info("Initializing AuthenticationService...");
    if (providers.isEmpty()) {
      throw new AuthenticationConfigException("Authenticator providers are not configured");
    }
    Array<AuthenticationProvider> prioritySortedAuthenticationProviders = providers.stream()
        .sorted(Comparator.comparingInt(provider -> provider.getAuthenticationType().priority()))
        .collect(ArrayCollectors.toArray(AuthenticationProvider.class));
    return new DefaultAuthenticationService(prioritySortedAuthenticationProviders);
  }

  @Bean
  @ConditionalOnFileCredentialsSource
  CredentialsSource fileCredentialsSource(@Value("${credentials.source.file.name:credentials}") URI fileName) {
    FileCredentialsSource fileCredentialsSource = new FileCredentialsSource(fileName);
    fileCredentialsSource.init();
    return fileCredentialsSource;
  }

  @Bean
  @ConditionalOnDatabaseCredentialsSource
  CredentialsSource dbCredentialsSource(DatabaseClient databaseClient) {
    return new DatabaseCredentialsSource(databaseClient);
  }

  @Bean
  @ConditionalOnBasicAuthenticationProvider
  AuthenticationProvider basicAuthenticationProvider(CredentialsSource credentialsSource) {
    return new BasicAuthenticationProvider(credentialsSource);
  }

  @Bean
  @ConditionalOnAnonymousProvider
  AuthenticationProvider anonymousAuthenticationProvider() {
    return new AnonymousAuthenticationProvider();
  }
}
