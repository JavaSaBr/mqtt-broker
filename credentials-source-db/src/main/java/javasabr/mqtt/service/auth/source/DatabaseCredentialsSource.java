package javasabr.mqtt.service.auth.source;

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
import io.r2dbc.spi.ConnectionFactoryOptions;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import javasabr.mqtt.service.auth.CredentialSource;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DatabaseCredentialsSource implements CredentialSource {

  private final ConnectionPool connectionPool;
  private final String credentialsQuery;

  @Override
  public String getName() {
    return "database";
  }

  @Override
  public Mono<Boolean> isCredentialExists(String user, byte[] pass) {
    return Mono
        .usingWhen(connectionPool.create(), connection -> executeQuery(connection, user), Connection::close)
        .map(existingPass -> Arrays.equals(existingPass, pass))
        .defaultIfEmpty(false);
  }

  private Mono<byte[]> executeQuery(Connection connection, String username) {
    return Mono
        .from(connection.createStatement(credentialsQuery).bind("$1", username).execute())
        .flatMapMany(resultset -> resultset.map((row, _) -> row.get("password", byte[].class)))
        .singleOrEmpty();
  }

  @Builder
  private static CredentialSource dbCredentialSource(
      String dbDriver,
      String dbHost,
      int dbPort,
      String dbUsername,
      String dbPassword,
      String dbName,
      String lockTimeout,
      String statementTimeout,
      String credentialsQuery,
      Duration maxIdleTime,
      int initialPoolSize,
      int maxPoolSize) {
    ConnectionFactoryOptions connectionFactoryOptions = ConnectionFactoryOptions
        .builder()
        .option(DRIVER, dbDriver)
        .option(HOST, dbHost)
        .option(PORT, dbPort)
        .option(USER, dbUsername)
        .option(PASSWORD, dbPassword)
        .option(DATABASE, dbName)
        .option(OPTIONS, Map.of("lock_timeout", lockTimeout, "statement_timeout", statementTimeout))
        .build();
    ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration
        .builder(ConnectionFactories.get(connectionFactoryOptions))
        .maxIdleTime(maxIdleTime)
        .maxSize(maxPoolSize)
        .initialSize(initialPoolSize)
        .build();
    ConnectionPool connectionPool = new ConnectionPool(configuration);
    return new DatabaseCredentialsSource(connectionPool, credentialsQuery);
  }
}
