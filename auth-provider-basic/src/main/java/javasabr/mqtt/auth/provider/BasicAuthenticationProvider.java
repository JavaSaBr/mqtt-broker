package javasabr.mqtt.auth.provider;

import javasabr.mqtt.auth.api.AuthenticationProvider;
import javasabr.mqtt.auth.api.CredentialSource;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BasicAuthenticationProvider implements AuthenticationProvider {

  CredentialSource credentialsSource;

  @Override
  public String getName() {
    return "basic";
  }

  @Override
  public Mono<Boolean> authenticate(@Nullable String username, byte[] password, byte[] data) {
    if (username == null) {
      return Mono.just(false);
    } else {
      return credentialsSource.isCredentialExists(username, password);
    }
  }

  @Override
  public String toString() {
    return "\"%s\": { \"credentialSource\": %s }".formatted(getName(), credentialsSource);
  }
}
