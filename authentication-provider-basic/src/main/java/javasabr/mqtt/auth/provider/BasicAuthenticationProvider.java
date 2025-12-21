package javasabr.mqtt.auth.provider;

import javasabr.mqtt.auth.api.AuthenticationProvider;
import javasabr.mqtt.auth.api.CredentialsSource;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BasicAuthenticationProvider implements AuthenticationProvider {

  CredentialsSource credentialsSource;

  @Override
  public String getName() {
    return "basic";
  }

  @Override
  public Mono<Boolean> authenticate(String username, byte[] password, byte[] data) {
    return credentialsSource.isCredentialsExists(username, password);
  }

  @Override
  public String toString() {
    return "\"%s\": { \"credentialSource\": %s }".formatted(getName(), credentialsSource);
  }
}
