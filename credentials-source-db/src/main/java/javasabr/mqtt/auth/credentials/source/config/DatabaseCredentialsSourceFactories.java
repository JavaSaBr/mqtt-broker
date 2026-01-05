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
import javasabr.mqtt.auth.api.database.DatabaseConnectionProperties;
import javasabr.mqtt.auth.api.database.DatabaseCredentials;
import javasabr.mqtt.auth.api.database.DatabasePoolProperties;
import javasabr.mqtt.auth.api.database.DatabaseTimeoutsProperties;
import javasabr.mqtt.auth.credentials.source.DatabaseCredentialsSource;
import lombok.Builder;
import org.flywaydb.core.Flyway;

public class DatabaseCredentialsSourceFactories {

  @Builder(builderMethodName = "flyway")
  private static Flyway createFlyway(
      DatabaseConnectionProperties databaseCredentialsSourceProperties,
      DatabaseCredentials adminDatabaseCredentials) {
    String databaseUrl = "jdbc:%s://%s:%s/%s".formatted(
        databaseCredentialsSourceProperties.driver().value(),
        databaseCredentialsSourceProperties.host(),
        databaseCredentialsSourceProperties.port(),
        databaseCredentialsSourceProperties.name());
    return Flyway.configure()
        .dataSource(databaseUrl, adminDatabaseCredentials.username(), adminDatabaseCredentials.password())
        .load();
  }

  @Builder(builderMethodName = "databaseCredentialsSource")
  private static DatabaseCredentialsSource createDatabaseCredentialsSource(
      DatabasePoolProperties databasePoolProperties,
      DatabaseTimeoutsProperties databaseTimeoutsProperties,
      DatabaseConnectionProperties databaseConnectionProperties,
      DatabaseCredentials readerDatabaseCredentials) {
    Map<String, String> timeoutOptions = Map.of(
        "lock_timeout", databaseTimeoutsProperties.lockTimeout(),
        "statement_timeout", databaseTimeoutsProperties.statementTimeout());
    ConnectionFactoryOptions connectionFactoryOptions = ConnectionFactoryOptions.builder()
        .option(DATABASE, databaseConnectionProperties.name())
        .option(DRIVER, databaseConnectionProperties.driver().value())
        .option(HOST, databaseConnectionProperties.host())
        .option(PORT, databaseConnectionProperties.port())
        .option(USER, readerDatabaseCredentials.username())
        .option(PASSWORD, readerDatabaseCredentials.password())
        .option(OPTIONS, timeoutOptions).build();
    ConnectionFactory connectionFactory = ConnectionFactories.get(connectionFactoryOptions);
    ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder(connectionFactory)
        .maxIdleTime(databasePoolProperties.maxIdleTime())
        .maxSize(databasePoolProperties.maxSize())
        .initialSize(databasePoolProperties.initialSize())
        .build();
    ConnectionPool connectionPool = new ConnectionPool(configuration);
    return new DatabaseCredentialsSource(connectionPool);
  }
}
