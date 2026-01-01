package javasabr.mqtt.auth.provider;

import com.fasterxml.jackson.annotation.JsonValue;
import javasabr.mqtt.auth.api.AuthenticationProvider;
import javasabr.mqtt.auth.api.MqttCredentials;
import javasabr.mqtt.auth.api.AuthenticationType;
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
  public AuthenticationType getAuthenticationType() {
    return AuthenticationType.BASIC;
  }

  @Override
  public Mono<Boolean> authenticate(MqttCredentials credentials) {
    return credentialsSource.isCredentialsExists(credentials);
  }

  @JsonValue
  @Override
  public String toString() {
    return "{ \"authenticationType\": \"%s\", \"credentialSource\": %s }".formatted(
        getAuthenticationType(),
        credentialsSource);
  }
}
