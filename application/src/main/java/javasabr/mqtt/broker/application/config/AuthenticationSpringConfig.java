package javasabr.mqtt.broker.application.config;

import static io.r2dbc.postgresql.PostgresqlConnectionFactoryProvider.OPTIONS;
import static io.r2dbc.spi.ConnectionFactoryOptions.DATABASE;
import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER;
import static io.r2dbc.spi.ConnectionFactoryOptions.HOST;
import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD;
import static io.r2dbc.spi.ConnectionFactoryOptions.PORT;
import static io.r2dbc.spi.ConnectionFactoryOptions.USER;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import java.util.List;
import java.util.Map;
import javasabr.mqtt.service.auth.AuthenticationService;
import javasabr.mqtt.service.auth.DefaultAuthenticationService;
import javasabr.mqtt.service.auth.PasswordBasedAuthenticationProvider;
import javasabr.mqtt.service.auth.provider.AuthenticationProvider;
import javasabr.mqtt.service.auth.source.CredentialSource;
import javasabr.mqtt.service.auth.source.DatabaseProperties;
import javasabr.mqtt.service.auth.source.FileCredentialsSource;
import javasabr.mqtt.service.auth.source.R2dbcCredentialsSource;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;

@Configuration(proxyBeanMethods = false)
public class AuthenticationSpringConfig {

  private static final String LOCK_TIMEOUT_OPTION = "lock_timeout";
  private static final String STATEMENT_TIMEOUT_OPTION = "statement_timeout";

  @Bean
  ConnectionFactory connectionFactory(DatabaseProperties config) {
    Map<String, String> timeoutOptions = Map.of(
        LOCK_TIMEOUT_OPTION,
        config.lockTimeout(),
        STATEMENT_TIMEOUT_OPTION,
        config.statementTimeout());
    ConnectionFactoryOptions connectionFactoryOptions = ConnectionFactoryOptions
        .builder()
        .option(DRIVER, config.driver())
        .option(HOST, config.host())
        .option(PORT, config.port())
        .option(USER, config.username())
        .option(PASSWORD, config.password())
        .option(DATABASE, config.name())
        .option(OPTIONS, timeoutOptions)
        .build();
    ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration
        .builder(ConnectionFactories.get(connectionFactoryOptions))
        .maxIdleTime(config.maxIdleTime())
        .maxSize(config.maxPoolSize())
        .initialSize(config.initialPoolSize())
        .build();
    return new ConnectionPool(configuration);
  }

  @Bean
  DatabaseClient databaseClient(ConnectionFactory connectionFactory) {
    return DatabaseClient.create(connectionFactory);
  }

  @Bean
  CredentialSource credentialSource(@Value("${credentials.source.file.name:credentials}") String fileName) {
    return new FileCredentialsSource(fileName);
  }

  @Bean
  CredentialSource dbCredentialSource(DatabaseClient connectionFactory, DatabaseProperties databaseProperties) {
    return new R2dbcCredentialsSource(connectionFactory, databaseProperties.credentialsQuery());
  }

  @Bean
  AuthenticationProvider passwordBasedAuthenticationProvider(CredentialSource credentialSource) {
    return new PasswordBasedAuthenticationProvider(credentialSource);
  }

  @Bean
  AuthenticationService authenticationService(
      List<AuthenticationProvider> credentialSource,
      @Value("${authentication.allow.anonymous:false}") boolean allowAnonymousAuth,
      @Value("${authentication.provider.default:basic}") String defaultProviderName) {
    var authenticationProviders = DictionaryFactory.mutableRefToRefDictionary(
        String.class,
        AuthenticationProvider.class);
    credentialSource.forEach(value -> authenticationProviders.put(value.getAuthMethodName(), value));
    AuthenticationProvider defaultProvider = authenticationProviders.get(defaultProviderName);
    if (defaultProvider == null) {
      throw new IllegalArgumentException("[%s] authenticator provider not found".formatted(defaultProviderName));
    }
    return new DefaultAuthenticationService(authenticationProviders.toReadOnly(), defaultProvider, allowAnonymousAuth);
  }
}
