package javasabr.mqtt.auth.credentials.source;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import javasabr.mqtt.auth.api.CredentialsSource;
import javasabr.mqtt.auth.api.CredentialsSourceType;
import javasabr.mqtt.auth.api.MqttCredentials;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DatabaseCredentialsSource implements CredentialsSource {

  @SuppressWarnings("SqlNoDataSourceInspection")
  private static final String CREDENTIALS_QUERY = """
      SELECT password
        FROM user_credentials
       WHERE username = $1
      """;

  DatabaseClient databaseClient;

  @Override
  public CredentialsSourceType getCredentialsSourceType() {
    return CredentialsSourceType.DATABASE;
  }

  @Override
  public Mono<Boolean> isCredentialsExists(MqttCredentials credentials) {
    return databaseClient
        .sql(CREDENTIALS_QUERY)
        .bind("$1", credentials.username())
        .map(row -> row.get("password", byte[].class))
        .all()
        .singleOrEmpty()
        .map(existingPassword -> Arrays.equals(existingPassword, credentials.password()))
        .defaultIfEmpty(false);
  }

  @Override
  public String toString() {
    String dbDriver = databaseClient.getConnectionFactory().getMetadata().getName();
    return "{ \"credentialsSource\": \"%s\", \"databaseDriver\": \"%s\" }".formatted(getCredentialsSourceType(), dbDriver);
  }

  @JsonValue
  public String jsonDebugValue() {
    return toString();
  }
}
