package javasabr.mqtt.auth.service.config;

import java.net.URI;
import java.util.List;
import javasabr.mqtt.auth.api.AnonymousAuthenticationProvider;
import javasabr.mqtt.auth.api.AuthenticationProvider;
import javasabr.mqtt.auth.api.AuthenticationService;
import javasabr.mqtt.auth.api.CredentialsSource;
import javasabr.mqtt.auth.api.exception.AuthenticationConfigException;
import javasabr.mqtt.auth.credentials.source.DatabaseCredentialsSource;
import javasabr.mqtt.auth.credentials.source.FileCredentialsSource;
import javasabr.mqtt.auth.provider.BasicAuthenticationProvider;
import javasabr.mqtt.auth.service.DefaultAuthenticationService;
import javasabr.mqtt.auth.service.config.annotation.ConditionalOnAnonymousProvider;
import javasabr.mqtt.auth.service.config.annotation.ConditionalOnBasicAuthenticationProvider;
import javasabr.mqtt.auth.service.config.annotation.ConditionalOnDatabaseCredentialsSource;
import javasabr.mqtt.auth.service.config.annotation.ConditionalOnFileCredentialsSource;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import lombok.CustomLog;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;

@CustomLog
@Configuration(proxyBeanMethods = false)
@Import(DatabaseSpringConfig.class)
@EnableConfigurationProperties({
    AuthenticationProperties.class
})
public class AuthenticationServiceSpringConfig {

  @Bean
  AuthenticationService authenticationService(
      List<AuthenticationProvider> authenticationProviders,
      @Value("${authentication.default-provider:#{null}}") @Nullable String defaultProviderName) {
    log.info("Initializing AuthenticationService...");
    if (authenticationProviders.isEmpty()) {
      throw new AuthenticationConfigException("Authenticator providers are not configured");
    }
    var providers = DictionaryFactory.mutableRefToRefDictionary(String.class, AuthenticationProvider.class);
    authenticationProviders.forEach(value -> providers.put(value.getName(), value));
    AuthenticationProvider defaultProvider;
    if (defaultProviderName == null) {
      defaultProvider = authenticationProviders.getFirst();
    } else {
      defaultProvider = providers.get(defaultProviderName);
    }
    if (defaultProvider == null) {
      throw new AuthenticationConfigException("[%s] authenticator provider not found".formatted(defaultProviderName));
    }
    return new DefaultAuthenticationService(providers.toReadOnly(), defaultProvider);
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
  CredentialsSource dbCredentialsSource(
      DatabaseClient databaseClient,
      DatabaseUrlConfig databaseUrlConfig,
      DatabaseUrlBuilder databaseUrlBuilder) {
    return new DatabaseCredentialsSource(databaseClient, databaseUrlBuilder.build(databaseUrlConfig));
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
