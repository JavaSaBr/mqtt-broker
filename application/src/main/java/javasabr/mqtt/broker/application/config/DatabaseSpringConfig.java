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
import javasabr.mqtt.auth.service.config.CredentialsSourceDatabaseProperties;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration(proxyBeanMethods = false)
public class DatabaseSpringConfig {

  private static final String LOCK_TIMEOUT_OPTION = "lock_timeout";
  private static final String STATEMENT_TIMEOUT_OPTION = "statement_timeout";

  @Bean
  @DependsOn("flyway")
  ConnectionFactory connectionFactory(
      CredentialsSourceDatabaseProperties config,
      DatabaseCredentialReaderProperties readerProperties) {
    Map<String, String> timeoutOptions = Map.of(
        LOCK_TIMEOUT_OPTION, config.lockTimeout(),
        STATEMENT_TIMEOUT_OPTION, config.statementTimeout());
    ConnectionFactoryOptions connectionFactoryOptions = ConnectionFactoryOptions.builder()
        .option(DRIVER, config.driver())
        .option(HOST, config.host())
        .option(PORT, config.port())
        .option(USER, readerProperties.username())
        .option(PASSWORD, readerProperties.password())
        .option(DATABASE, config.name())
        .option(OPTIONS, timeoutOptions).build();
    ConnectionFactory connectionFactory = ConnectionFactories.get(connectionFactoryOptions);
    ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder(connectionFactory)
        .maxIdleTime(config.maxIdleTime())
        .maxSize(config.maxPoolSize())
        .initialSize(config.initialPoolSize())
        .build();
    return new ConnectionPool(configuration);
  }

  @Bean(initMethod = "migrate")
  public Flyway flyway(
      DatabaseUrlBuilder databaseUrlBuilder,
      CredentialsSourceDatabaseProperties dbProperties,
      DatabaseCredentialWriterProperties dbCredentials) {
    return Flyway.configure()
        .dataSource(databaseUrlBuilder.build(dbProperties), dbCredentials.username(), dbCredentials.password())
        .locations("db/migration")
        .baselineOnMigrate(true)
        .load();
  }

  @Bean
  DatabaseUrlBuilder databaseUrlBuilder() {
    return dbProps -> "jdbc:%s://%s:%s/%s".formatted(dbProps.driver(), dbProps.host(), dbProps.port(), dbProps.name());
  }

  public interface DatabaseUrlBuilder {
    String build(CredentialsSourceDatabaseProperties dbProps);
  }
}
