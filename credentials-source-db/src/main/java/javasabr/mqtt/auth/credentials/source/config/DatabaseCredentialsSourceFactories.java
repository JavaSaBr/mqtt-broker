package javasabr.mqtt.auth.credentials.source.config;

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
import javasabr.mqtt.auth.api.DatabaseConnectionProperties;
import javasabr.mqtt.auth.api.DatabaseCredentials;
import javasabr.mqtt.auth.api.DatabaseProperties;
import javasabr.mqtt.auth.credentials.source.DatabaseCredentialsSource;
import lombok.Builder;
import org.flywaydb.core.Flyway;

public class DatabaseCredentialsSourceFactories {

  @Builder(builderMethodName = "flyway")
  private static Flyway createFlyway(
      DatabaseConnectionProperties databaseCredentialsSourceProperties,
      DatabaseCredentials adminDatabaseCredentials) {
    String databaseUrl = "jdbc:%s://%s:%s/%s".formatted(
        databaseCredentialsSourceProperties.dbDriver().value(),
        databaseCredentialsSourceProperties.dbHost(),
        databaseCredentialsSourceProperties.dbPort(),
        databaseCredentialsSourceProperties.dbName());
    return Flyway.configure()
        .dataSource(databaseUrl, adminDatabaseCredentials.username(), adminDatabaseCredentials.password())
        .load();
  }

  @Builder(builderMethodName = "databaseCredentialsSource")
  private static DatabaseCredentialsSource createDatabaseCredentialsSource(
      DatabaseProperties databaseCredentialsSourceProperties,
      DatabaseCredentials readerDatabaseCredentials) {
    Map<String, String> timeoutOptions = Map.of(
        "lock_timeout", databaseCredentialsSourceProperties.lockTimeout(),
        "statement_timeout", databaseCredentialsSourceProperties.statementTimeout());
    ConnectionFactoryOptions connectionFactoryOptions = ConnectionFactoryOptions.builder()
        .option(DATABASE, databaseCredentialsSourceProperties.dbName())
        .option(DRIVER, databaseCredentialsSourceProperties.dbDriver().value())
        .option(HOST, databaseCredentialsSourceProperties.dbHost())
        .option(PORT, databaseCredentialsSourceProperties.dbPort())
        .option(USER, readerDatabaseCredentials.username())
        .option(PASSWORD, readerDatabaseCredentials.password())
        .option(OPTIONS, timeoutOptions).build();
    ConnectionFactory connectionFactory = ConnectionFactories.get(connectionFactoryOptions);
    ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder(connectionFactory)
        .maxIdleTime(databaseCredentialsSourceProperties.maxIdleTime())
        .maxSize(databaseCredentialsSourceProperties.maxPoolSize())
        .initialSize(databaseCredentialsSourceProperties.initialPoolSize())
        .build();
    ConnectionPool connectionPool = new ConnectionPool(configuration);
    return new DatabaseCredentialsSource(connectionPool);
  }
}
