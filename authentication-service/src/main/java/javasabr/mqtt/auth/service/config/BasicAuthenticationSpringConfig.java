package javasabr.mqtt.auth.service.config;

import io.r2dbc.spi.ConnectionFactory;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import javasabr.mqtt.auth.api.AnonymousAuthenticationProvider;
import javasabr.mqtt.auth.api.AuthenticationConfigException;
import javasabr.mqtt.auth.api.AuthenticationProvider;
import javasabr.mqtt.auth.api.AuthenticationService;
import javasabr.mqtt.auth.api.CredentialSource;
import javasabr.mqtt.auth.service.DefaultAuthenticationService;
import javasabr.mqtt.auth.credentials.source.DatabaseCredentialsSource;
import javasabr.mqtt.auth.provider.BasicAuthenticationProvider;
import javasabr.mqtt.auth.credentials.source.FileCredentialsSource;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.r2dbc.core.DatabaseClient;

@CustomLog
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({
    CredentialsSourceDatabaseProperties.class
})
public class BasicAuthenticationSpringConfig {

  @Bean
  AuthenticationService authenticationService(
      List<AuthenticationProvider> authenticationProviders,
      @Value("${authentication.provider.default:#{null}}") String defaultProviderName) {
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
  DatabaseClient databaseClient(ConnectionFactory connectionFactory) {
    return DatabaseClient.create(connectionFactory);
  }

  @Bean
  @ConditionalOnProperty(name = "authentication.credentials.source", havingValue = "file")
  @ConditionalOnClass(name = "javasabr.mqtt.auth.credentials.source.FileCredentialsSource")
  CredentialSource fileCredentialSource(@Value("${credentials.source.file.name:credentials}") Resource fileName)
      throws IOException {
    FileCredentialsSource fileCredentialsSource = new FileCredentialsSource(fileName.getURI());
    fileCredentialsSource.init();
    return fileCredentialsSource;
  }

  @Bean
  @ConditionalOnProperty(name = "authentication.credentials.source", havingValue = "database")
  @ConditionalOnClass(name = "javasabr.mqtt.auth.credentials.source.DatabaseCredentialsSource")
  CredentialSource dbCredentialSource(DatabaseClient connectionFactory) {
    return new DatabaseCredentialsSource(connectionFactory);
  }

  @Bean
  @ConditionalOnProperty(name = "authentication.provider", havingValue = "basic")
  @ConditionalOnClass(name = "javasabr.mqtt.auth.provider.BasicAuthenticationProvider")
  AuthenticationProvider passwordBasedAuthenticationProvider(CredentialSource credentialSource) {
    return new BasicAuthenticationProvider(credentialSource);
  }

  @Bean
  @ConditionalOnProperty(name = "authentication.allow.anonymous", havingValue = "true")
  AuthenticationProvider anonymousAuthenticationProvider() {
    return new AnonymousAuthenticationProvider();
  }
}
