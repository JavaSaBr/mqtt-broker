package javasabr.mqtt.service.auth.source;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.Arrays;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class R2dbcCredentialsSource implements CredentialSource {

  private static final String CREDENTIALS_SOURCE_NAME = "database";
  private static final String PASSWORD_COLUMN = "password";
  private static final String USERNAME_BIND_PARAM = "$1";

  private final ConnectionFactory connectionPool;
  private final String credentialsQuery;

  @Override
  public String getName() {
    return CREDENTIALS_SOURCE_NAME;
  }

  @Override
  public Mono<Boolean> isCredentialExists(String username, byte[] requestedPassword) {
    return Mono
        .usingWhen(connectionPool.create(), connection -> executeQuery(connection, username), Connection::close)
        .map(existingPassword -> Arrays.equals(existingPassword, requestedPassword))
        .defaultIfEmpty(false);
  }

  private Mono<byte[]> executeQuery(Connection connection, String username) {
    Publisher<? extends Result> passwordsResultset = connection
        .createStatement(credentialsQuery)
        .bind(USERNAME_BIND_PARAM, username)
        .execute();
    return Mono.from(passwordsResultset).flatMapMany(R2dbcCredentialsSource::streamPasswordsResultset).singleOrEmpty();
  }

  private static Publisher<byte[]> streamPasswordsResultset(Result resultset) {
    return resultset.map(R2dbcCredentialsSource::getPassword);
  }

  private static byte @Nullable [] getPassword(Row row, RowMetadata rowMetadata) {
    return row.get(PASSWORD_COLUMN, byte[].class);
  }
}
