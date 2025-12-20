package javasabr.mqtt.auth.credentials.source;

import java.util.Arrays;
import javasabr.mqtt.auth.api.CredentialSource;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DatabaseCredentialsSource implements CredentialSource {

  @SuppressWarnings("SqlNoDataSourceInspection")
  private static final String CREDENTIALS_QUERY = """
      SELECT password
        FROM user_credentials
       WHERE username = $1
      """;

  DatabaseClient databaseClient;

  @Override
  public String getName() {
    return "database";
  }

  @Override
  public Mono<Boolean> isCredentialExists(String username, byte[] requestedPassword) {
    return databaseClient
        .sql(CREDENTIALS_QUERY)
        .bind("$1", username)
        .map(row -> row.get("password", byte[].class))
        .all()
        .singleOrEmpty()
        .map(existingPassword -> Arrays.equals(existingPassword, requestedPassword))
        .defaultIfEmpty(false);
  }

  @Override
  public String toString() {
    String driver = databaseClient.getConnectionFactory().getMetadata().getName();
    return "DatabaseCredentialsSource{driver='%s'}".formatted(driver);
  }
}
