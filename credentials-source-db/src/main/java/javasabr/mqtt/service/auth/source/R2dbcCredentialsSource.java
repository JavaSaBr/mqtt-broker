package javasabr.mqtt.service.auth.source;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class R2dbcCredentialsSource implements CredentialSource {

  private static final String CREDENTIALS_SOURCE_NAME = "database";
  private static final String PASSWORD_COLUMN = "password";
  private static final String USERNAME_BIND_PARAM = "$1";

  private final DatabaseClient databaseClient;
  private final String credentialsQuery;

  @Override
  public String getName() {
    return CREDENTIALS_SOURCE_NAME;
  }

  @Override
  public Mono<Boolean> isCredentialExists(String username, byte[] requestedPassword) {
    return databaseClient
        .sql(credentialsQuery)
        .bind(USERNAME_BIND_PARAM, username)
        .map(row -> row.get(PASSWORD_COLUMN, byte[].class))
        .all()
        .singleOrEmpty()
        .map(existingPassword -> Arrays.equals(existingPassword, requestedPassword))
        .defaultIfEmpty(false);
  }
}
