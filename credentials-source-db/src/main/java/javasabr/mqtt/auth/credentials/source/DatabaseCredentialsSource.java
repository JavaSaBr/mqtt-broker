package javasabr.mqtt.auth.credentials.source;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import javasabr.mqtt.auth.api.CredentialsSource;
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
  String dbUrl;

  @Override
  public String getName() {
    return "database";
  }

  @Override
  public Mono<Boolean> isCredentialsExists(String userName, byte[] password) {
    return databaseClient
        .sql(CREDENTIALS_QUERY)
        .bind("$1", userName)
        .map(row -> row.get("password", byte[].class))
        .all()
        .singleOrEmpty()
        .map(existingPassword -> Arrays.equals(existingPassword, password))
        .defaultIfEmpty(false);
  }

  @JsonValue
  @Override
  public String toString() {
    return "{ \"credentialsSource\": \"%s\", \"databaseUrl\": \"%s\" }".formatted(getName(), dbUrl);
  }
}
