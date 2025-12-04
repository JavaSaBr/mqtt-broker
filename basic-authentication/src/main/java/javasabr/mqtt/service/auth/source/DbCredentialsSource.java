package javasabr.mqtt.service.auth.source;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import java.time.Duration;
import java.util.Arrays;
import javasabr.mqtt.service.auth.CredentialSource;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import reactor.core.publisher.Mono;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DbCredentialsSource implements CredentialSource {

  private static final String DB_USER = "dbuser";
  private static final String DB_PASSWORD = "dbpassword";
  private static final String DATABASE_URL =
      "r2dbc:postgresql://localhost:5432/credentials_db" + "?user=" + DB_USER + "&password=" + DB_PASSWORD;

  private final ConnectionPool connectionPool;

  private static final String SELECT_PASSWORD_SQL = "SELECT password FROM user_credentials WHERE username = $1";

  private final String sql;

  public DbCredentialsSource(String dbUrl, String sql) {
    ConnectionFactory connectionFactory = ConnectionFactories.get(dbUrl);

    ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration
        .builder(connectionFactory)
        .maxIdleTime(Duration.ofMinutes(30))
        .maxSize(10)
        .initialSize(5)
        .build();

    this.connectionPool = new ConnectionPool(configuration);
    this.sql = sql;
  }

  private Mono<byte[]> executeQuery(Connection connection, String username) {
    return Mono
        .from(connection
            .createStatement(sql)
            .bind("$1", username)
            .execute())
        .flatMapMany(result -> result.map((row, _) -> row.get("password", byte[].class)))
        .singleOrEmpty();
  }

  @Override
  public Mono<Boolean> check(String user, byte[] pass) {
    return Mono
        .usingWhen(connectionPool.create(), connection -> executeQuery(connection, user), Connection::close)
        .map(existingPass -> Arrays.equals(existingPass, pass))
        .defaultIfEmpty(false);
  }

  @Override
  public Mono<Boolean> check(byte[] pass) {
    return Mono.just(false);
  }
}
