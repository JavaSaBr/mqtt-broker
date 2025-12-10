package javasabr.mqtt.service.auth;

import javasabr.mqtt.service.auth.provider.AuthenticationProvider;
import javasabr.mqtt.service.auth.source.CredentialSource;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PasswordBasedAuthenticationProvider implements AuthenticationProvider {

  CredentialSource credentialsSource;

  @Override
  public String getAuthMethodName() {
    return "basic";
  }

  @Override
  public Mono<Boolean> authenticate(@Nullable String username, byte[] password, byte[] data) {
    // processData(data);
    if (username == null) {
      return Mono.just(false);
    } else {
      return credentialsSource.isCredentialExists(username, password);
    }
  }
}
