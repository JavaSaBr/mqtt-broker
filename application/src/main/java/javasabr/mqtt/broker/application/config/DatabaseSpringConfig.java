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
import java.util.Map;
import javasabr.mqtt.model.Credentials;
import javasabr.mqtt.model.DatabaseUrlBuilder;
import javasabr.mqtt.model.database.DatabasePoolProperties;
import javasabr.mqtt.model.database.DatabaseTimeoutsProperties;
import javasabr.mqtt.model.database.DatabaseUrlProperties;
import javasabr.mqtt.model.database.DatabaseUsersProperties;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DatabaseConnectionProperties.class)
public class DatabaseSpringConfig {

  @Bean
  ConnectionFactoryOptions connectionFactoryOptions(
      DatabaseTimeoutsProperties config,
      DatabaseUrlProperties databaseUrlProperties,
      @Qualifier("readerCredentials") Credentials credentials){
    Map<String, String> timeoutOptions = Map.of(
        "lock_timeout", config.lockTimeout(),
        "statement_timeout", config.statementTimeout());
    return ConnectionFactoryOptions.builder()
        .option(DRIVER, databaseUrlProperties.driver())
        .option(HOST, databaseUrlProperties.host())
        .option(PORT, databaseUrlProperties.port())
        .option(USER, credentials.username())
        .option(PASSWORD, credentials.password())
        .option(DATABASE, databaseUrlProperties.name())
        .option(OPTIONS, timeoutOptions).build();
  }

  @Bean
  @DependsOn("flyway")
  ConnectionFactory connectionFactory(
      DatabasePoolProperties config,
      ConnectionFactoryOptions connectionFactoryOptions) {
    ConnectionFactory connectionFactory = ConnectionFactories.get(connectionFactoryOptions);
    ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder(connectionFactory)
        .maxIdleTime(config.maxIdleTime())
        .maxSize(config.maxPoolSize())
        .initialSize(config.initialPoolSize())
        .build();
    return new ConnectionPool(configuration);
  }

  @Bean(initMethod = "migrate")
  Flyway flyway(
      DatabaseUrlBuilder databaseUrlBuilder,
      DatabaseUrlProperties databaseUrlProperties,
      @Qualifier("adminCredentials") Credentials credentials) {
    return Flyway.configure()
        .dataSource(databaseUrlBuilder.build(databaseUrlProperties), credentials.username(), credentials.password())
        .load();
  }

  @Bean
  DatabaseUrlBuilder databaseUrlBuilder() {
    return db -> "jdbc:%s://%s:%s/%s".formatted(db.driver(), db.host(), db.port(), db.name());
  }

  @Bean
  public Credentials readerCredentials(DatabaseUsersProperties users) {
    return users.get("reader");
  }

  @Bean
  public Credentials writerCredentials(DatabaseUsersProperties users) {
    return users.get("writer");
  }

  @Bean
  public Credentials adminCredentials(DatabaseUsersProperties users) {
    return users.get("admin");
  }
}
