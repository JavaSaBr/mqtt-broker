package javasabr.mqtt.auth.service.config;

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
import java.util.Map;
import javasabr.mqtt.auth.api.AuthenticationType;
import javasabr.mqtt.auth.api.CredentialsSourceType;
import javasabr.mqtt.auth.service.config.property.AuthenticationProperties;
import javasabr.mqtt.auth.service.config.property.AuthenticationProviderProperties;
import javasabr.mqtt.auth.service.config.property.DatabaseCredentials;
import javasabr.mqtt.auth.service.config.property.CredentialsSourceProperties;
import javasabr.mqtt.auth.service.config.property.DatabaseConnectionProperties;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.r2dbc.core.DatabaseClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DatabaseConnectionProperties.class)
@ConditionalOnProperty(name = "authentication.provider.basic.credentials-sources.database.enabled", havingValue = "true")
public class DatabaseSpringConfig {

  @Bean
  ConnectionFactoryOptions connectionFactoryOptions(
      AuthenticationProperties authenticationProperties,
      DatabaseCredentials readerDatabaseCredentials) {
    AuthenticationProviderProperties authenticationProviderProperties = authenticationProperties.provider()
        .get(AuthenticationType.BASIC);
    CredentialsSourceProperties credentialsSourceProperties = authenticationProviderProperties.credentialsSources()
        .get(CredentialsSourceType.DATABASE);
    Map<String, String> timeoutOptions = Map.of(
        "lock_timeout", credentialsSourceProperties.lockTimeout(),
        "statement_timeout", credentialsSourceProperties.statementTimeout());
    return ConnectionFactoryOptions.builder()
        .option(DATABASE, credentialsSourceProperties.dbName())
        .option(DRIVER, credentialsSourceProperties.dbDriver().value())
        .option(HOST, credentialsSourceProperties.dbHost())
        .option(PORT, credentialsSourceProperties.dbPort())
        .option(USER, readerDatabaseCredentials.username())
        .option(PASSWORD, readerDatabaseCredentials.password())
        .option(OPTIONS, timeoutOptions)
        .build();
  }

  @Bean
  DatabaseClient databaseClient(ConnectionFactory connectionFactory) {
    return DatabaseClient.create(connectionFactory);
  }

  @Bean
  @DependsOn("flyway")
  ConnectionFactory connectionFactory(
      AuthenticationProperties authenticationProperties,
      ConnectionFactoryOptions connectionFactoryOptions) {
    AuthenticationProviderProperties authenticationProviderProperties = authenticationProperties.provider()
        .get(AuthenticationType.BASIC);
    CredentialsSourceProperties credentialsSourceProperties = authenticationProviderProperties.credentialsSources()
        .get(CredentialsSourceType.DATABASE);
    ConnectionFactory connectionFactory = ConnectionFactories.get(connectionFactoryOptions);
    ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder(connectionFactory)
        .maxIdleTime(credentialsSourceProperties.maxIdleTime())
        .maxSize(credentialsSourceProperties.maxPoolSize())
        .initialSize(credentialsSourceProperties.initialPoolSize())
        .build();
    return new ConnectionPool(configuration);
  }

  @Bean(initMethod = "migrate")
  Flyway flyway(AuthenticationProperties authenticationProperties, DatabaseCredentials adminDatabaseCredentials) {

    AuthenticationProviderProperties authenticationProviderProperties = authenticationProperties.provider()
        .get(AuthenticationType.BASIC);
    CredentialsSourceProperties credentialsSourceProperties = authenticationProviderProperties.credentialsSources()
        .get(CredentialsSourceType.DATABASE);

    String databaseUrl = "jdbc:%s://%s:%s/%s".formatted(
        credentialsSourceProperties.dbDriver().value(),
        credentialsSourceProperties.dbHost(),
        credentialsSourceProperties.dbPort(),
        credentialsSourceProperties.dbName());
    return Flyway.configure()
        .dataSource(databaseUrl, adminDatabaseCredentials.username(), adminDatabaseCredentials.password())
        .load();
  }
}
