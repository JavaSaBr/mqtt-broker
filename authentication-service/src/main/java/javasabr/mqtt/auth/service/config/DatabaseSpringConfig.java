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
import javasabr.mqtt.auth.service.config.annotation.ConditionalOnDatabaseCredentialsSource;
import javasabr.mqtt.auth.service.config.property.Credentials;
import javasabr.mqtt.auth.service.config.property.DatabaseConnectionProperties;
import javasabr.mqtt.auth.service.config.property.DatabasePoolConfig;
import javasabr.mqtt.auth.service.config.property.DatabaseTimeoutsConfig;
import javasabr.mqtt.auth.service.config.property.DatabaseUrlConfig;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.r2dbc.core.DatabaseClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DatabaseConnectionProperties.class)
@ConditionalOnDatabaseCredentialsSource
public class DatabaseSpringConfig {

  @Bean
  ConnectionFactoryOptions connectionFactoryOptions(
      DatabaseTimeoutsConfig databaseTimeoutsConfig,
      DatabaseUrlConfig databaseUrlConfig,
      @Qualifier("readerCredentials") Credentials credentials){
    Map<String, String> timeoutOptions = Map.of(
        "lock_timeout", databaseTimeoutsConfig.lockTimeout(),
        "statement_timeout", databaseTimeoutsConfig.statementTimeout());
    return ConnectionFactoryOptions.builder()
        .option(DATABASE, databaseUrlConfig.name())
        .option(DRIVER, databaseUrlConfig.driver())
        .option(HOST, databaseUrlConfig.host())
        .option(PORT, databaseUrlConfig.port())
        .option(USER, credentials.username())
        .option(PASSWORD, credentials.password())
        .option(OPTIONS, timeoutOptions)
        .build();
  }

  @Bean
  DatabaseUrlBuilder databaseUrlBuilder() {
    return db -> "jdbc:%s://%s:%s/%s".formatted(db.driver(), db.host(), db.port(), db.name());
  }

  @Bean
  DatabaseClient databaseClient(ConnectionFactory connectionFactory) {
    return DatabaseClient.create(connectionFactory);
  }

  @Bean
  @DependsOn("flyway")
  ConnectionFactory connectionFactory(
      DatabasePoolConfig databasePoolConfig,
      ConnectionFactoryOptions connectionFactoryOptions) {
    ConnectionFactory connectionFactory = ConnectionFactories.get(connectionFactoryOptions);
    ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder(connectionFactory)
        .maxIdleTime(databasePoolConfig.maxIdleTime())
        .maxSize(databasePoolConfig.maxPoolSize())
        .initialSize(databasePoolConfig.initialPoolSize())
        .build();
    return new ConnectionPool(configuration);
  }

  @Bean(initMethod = "migrate")
  Flyway flyway(
      DatabaseUrlBuilder databaseUrlBuilder,
      DatabaseUrlConfig databaseUrlConfig,
      @Qualifier("adminCredentials") Credentials credentials) {
    return Flyway.configure()
        .dataSource(databaseUrlBuilder.build(databaseUrlConfig), credentials.username(), credentials.password()).load();
  }

  @Bean
  @ConditionalOnProperty(name = "persistence.database.users.reader.username")
  public Credentials readerCredentials(DatabaseUsersConfig usersConfig) {
      return usersConfig.users().get("reader");
  }

  @Bean
  @ConditionalOnProperty(name = "persistence.database.users.writer.username")
  public Credentials writerCredentials(DatabaseUsersConfig usersConfig) {
    return usersConfig.users().get("writer");
  }

  @Bean
  @ConditionalOnProperty(name = "persistence.database.users.admin.username")
  public Credentials adminCredentials(DatabaseUsersConfig usersConfig) {
    return usersConfig.users().get("admin");
  }
}
