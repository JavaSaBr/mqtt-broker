package javasabr.mqtt.auth.service.config;

import static javasabr.rlib.collections.dictionary.DictionaryCollectors.toRefToRefDictionary;

import java.util.List;
import java.util.function.Function;
import javasabr.mqtt.auth.api.AuthenticationProvider;
import javasabr.mqtt.auth.api.AuthenticationService;
import javasabr.mqtt.auth.api.AuthenticationMethod;
import javasabr.mqtt.auth.api.CredentialsSource;
import javasabr.mqtt.auth.api.CredentialsSourceType;
import javasabr.mqtt.auth.api.exception.AuthenticationConfigException;
import javasabr.mqtt.auth.credentials.source.DatabaseCredentialsSource;
import javasabr.mqtt.auth.credentials.source.FileCredentialsSource;
import javasabr.mqtt.auth.provider.BasicAuthenticationProvider;
import javasabr.mqtt.auth.service.AnonymousAuthenticationProvider;
import javasabr.mqtt.auth.service.DefaultAuthenticationService;
import javasabr.mqtt.auth.service.config.property.AuthenticationProperties;
import javasabr.mqtt.auth.service.config.property.CredentialsSourceProperties;
import javasabr.rlib.collections.dictionary.RefToRefDictionary;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;

@CustomLog
@Configuration(proxyBeanMethods = false)
@Import(DatabaseCredentialsSourceSpringConfig.class)
@EnableConfigurationProperties(AuthenticationProperties.class)
public class AuthenticationServiceSpringConfig {

  @Bean
  AuthenticationService authenticationService(
      List<AuthenticationProvider> authenticationProviders,
      AuthenticationProperties authenticationProperties,
      @Autowired(required = false) AnonymousAuthenticationProvider anonymousAuthenticationProvider) {
    log.info("Initializing AuthenticationService...");
    if (authenticationProviders.isEmpty()) {
      throw new AuthenticationConfigException("Authenticator providers are not configured");
    }
    RefToRefDictionary<AuthenticationMethod, AuthenticationProvider> providers = authenticationProviders.stream()
        .collect(toRefToRefDictionary(AuthenticationProvider::getAuthenticationMethod, Function.identity()));
    AuthenticationProvider defaultProvider;
    AuthenticationMethod defaultAuthenticationMethod = authenticationProperties.defaultMethod();
    if (defaultAuthenticationMethod == null) {
      defaultProvider = authenticationProviders.getFirst();
    } else {
      defaultProvider = providers.get(defaultAuthenticationMethod);
    }
    if (defaultProvider == null) {
      throw new AuthenticationConfigException("[%s] authenticator method not found".formatted(
          defaultAuthenticationMethod));
    }
    return new DefaultAuthenticationService(providers, defaultProvider, anonymousAuthenticationProvider);
  }

  @Bean
  public CredentialsSourceProperties fileCredentialsSourceProperties(AuthenticationProperties authenticationProperties) {
    return authenticationProperties
        .method()
        .get(AuthenticationMethod.BASIC)
        .credentialsSource()
        .get(CredentialsSourceType.FILE);
  }

  @Bean
  @ConditionalOnClass(name = "javasabr.mqtt.auth.credentials.source.FileCredentialsSource")
  @ConditionalOnProperty(name = "authentication.method.basic.credentials-source.file.enabled", havingValue = "true")
  CredentialsSource fileCredentialsSource(CredentialsSourceProperties fileCredentialsSourceProperties) {
    FileCredentialsSource fileCredentialsSource = new FileCredentialsSource(fileCredentialsSourceProperties.fsPath());
    fileCredentialsSource.init();
    return fileCredentialsSource;
  }

  @Bean
  @ConditionalOnClass(name = "javasabr.mqtt.auth.credentials.source.DatabaseCredentialsSource")
  @ConditionalOnProperty(name = "authentication.method.basic.credentials-source.database.enabled", havingValue = "true")
  CredentialsSource dbCredentialsSource(DatabaseClient databaseClient) {
    return new DatabaseCredentialsSource(databaseClient);
  }

  @Bean
  @ConditionalOnClass(name = "javasabr.mqtt.auth.provider.BasicAuthenticationProvider")
  @ConditionalOnProperty(name = "authentication.method.basic.enabled", havingValue = "true")
  @ConditionalOnBean(CredentialsSource.class)
  AuthenticationProvider basicAuthenticationProvider(CredentialsSource credentialsSource) {
    return new BasicAuthenticationProvider(credentialsSource);
  }

  @Bean
  @ConditionalOnProperty(name = "authentication.allow-anonymous", havingValue = "true")
  AuthenticationProvider anonymousAuthenticationProvider() {
    return new AnonymousAuthenticationProvider();
  }
}
