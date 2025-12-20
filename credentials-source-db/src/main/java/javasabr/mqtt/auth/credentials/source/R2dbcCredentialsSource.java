package javasabr.mqtt.auth.credentials.source;

import java.util.Arrays;
import javasabr.mqtt.auth.api.CredentialSource;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class R2dbcCredentialsSource implements CredentialSource {

  @SuppressWarnings("SqlNoDataSourceInspection")
  private static final String CREDENTIALS_QUERY = """
      SELECT password
        FROM user_credentials
       WHERE username = $1
      """;

  private final DatabaseClient databaseClient;

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
}
