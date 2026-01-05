package javasabr.mqtt.auth.credentials.source;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import java.util.Objects;
import javasabr.mqtt.auth.api.CredentialsSource;
import javasabr.mqtt.auth.api.CredentialsSourceType;
import javasabr.mqtt.auth.api.MqttCredentials;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DatabaseCredentialsSource implements CredentialsSource {

  @SuppressWarnings("SqlNoDataSourceInspection")
  private static final String CREDENTIALS_QUERY = """
      SELECT COUNT(*) > 0
        FROM user_credentials
       WHERE username = $1
         AND password = $2;
      """;

  private static Mono<Boolean> isCredentialsRecordFound(Result result) {
    return Mono.from(result.map((row, _) -> Objects.equals(row.get(0, Boolean.class), Boolean.TRUE)));
  }

  ConnectionFactory connectionFactory;

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
    Publisher<? extends Result> credentialsQuery = connection.createStatement(CREDENTIALS_QUERY)
        .bind("$1", credentials.username())
        .bind("$2", credentials.password())
        .execute();
    return Mono.from(credentialsQuery)
        .flatMap(DatabaseCredentialsSource::isCredentialsRecordFound)
        .defaultIfEmpty(false);
  }
}
