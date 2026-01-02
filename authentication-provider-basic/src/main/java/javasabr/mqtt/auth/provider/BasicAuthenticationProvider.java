package javasabr.mqtt.auth.provider;

import com.fasterxml.jackson.annotation.JsonValue;
import javasabr.mqtt.auth.api.AuthenticationProvider;
import javasabr.mqtt.auth.api.MqttCredentials;
import javasabr.mqtt.auth.api.AuthenticationMethod;
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
  public AuthenticationMethod getAuthenticationMethod() {
    return AuthenticationMethod.BASIC;
  }

  @Override
  public Mono<Boolean> authenticate(MqttCredentials credentials) {
    return credentialsSource.isCredentialsExists(credentials);
  }

  @Override
  public String toString() {
    return "{ \"authenticationMethod\": \"%s\", \"credentialSource\": %s }".formatted(
        getAuthenticationMethod(),
        credentialsSource);
  }
}
