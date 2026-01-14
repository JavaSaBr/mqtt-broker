package javasabr.mqtt.auth.credentials.source;

import static io.r2dbc.postgresql.PostgresqlConnectionFactoryProvider.OPTIONS;
import static io.r2dbc.spi.ConnectionFactoryOptions.DATABASE;
import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER;
import static io.r2dbc.spi.ConnectionFactoryOptions.HOST;
import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD;
import static io.r2dbc.spi.ConnectionFactoryOptions.PORT;
import static io.r2dbc.spi.ConnectionFactoryOptions.USER;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import java.util.Map;
import javasabr.mqtt.auth.api.CredentialsSource;
import javasabr.mqtt.auth.api.CredentialsSourceType;
import javasabr.mqtt.auth.api.MqttCredentials;
import javasabr.mqtt.auth.api.database.DatabaseConnectionProperties;
import javasabr.mqtt.auth.api.database.DatabaseCredentials;
import javasabr.mqtt.auth.api.database.DatabasePoolProperties;
import javasabr.mqtt.auth.api.database.DatabaseTimeoutProperties;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import reactor.core.publisher.Mono;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DatabaseCredentialsSource implements CredentialsSource {

  @SuppressWarnings("SqlNoDataSourceInspection")
  private static final String CREDENTIALS_QUERY = """
      SELECT 1
        FROM user_credentials
       WHERE username = $1
         AND password = $2
       LIMIT 1
      """;

  ConnectionFactory connectionFactory;

  @Builder
  public DatabaseCredentialsSource(
      DatabasePoolProperties databasePoolProperties,
      DatabaseTimeoutProperties databaseTimeoutsProperties,
      DatabaseConnectionProperties databaseConnectionProperties,
      DatabaseCredentials readerDatabaseCredentials) {
    Map<String, String> timeoutOptions = Map.of(
        "lock_timeout", databaseTimeoutsProperties.lockTimeout(),
        "statement_timeout", databaseTimeoutsProperties.statementTimeout());
    ConnectionFactoryOptions connectionFactoryOptions = ConnectionFactoryOptions.builder()
        .option(DATABASE, databaseConnectionProperties.dbName())
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
    this.connectionFactory = new ConnectionPool(configuration);
  }

  @Override
  public CredentialsSourceType getType() {
    return CredentialsSourceType.DATABASE;
  }

  @Override
  public Mono<Boolean> isCredentialsValid(MqttCredentials credentials) {
    return Mono.usingWhen(
            connectionFactory.create(),
            connection -> executeCredentialsQuery(connection, credentials),
            Connection::close);
  }

  @Override
  public String toString() {
    String dbDriver = connectionFactory.getMetadata().getName();
    return "{ \"credentialsSource\": \"%s\", \"databaseDriver\": \"%s\" }".formatted(getType(), dbDriver);
  }

  private Mono<Boolean> executeCredentialsQuery(Connection connection, MqttCredentials credentials) {
    return Mono.from(connection
        .createStatement(CREDENTIALS_QUERY)
        .bind("$1", credentials.username())
        .bind("$2", credentials.password())
        .execute())
        .map(result -> result.map((_, _) -> true))
        .flatMap(Mono::from)
        .defaultIfEmpty(false);
  }
}
