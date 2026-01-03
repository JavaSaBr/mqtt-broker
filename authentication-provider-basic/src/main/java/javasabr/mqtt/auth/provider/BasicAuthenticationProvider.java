package javasabr.mqtt.auth.provider;

import static java.util.Comparator.comparingInt;

import java.util.List;
import javasabr.mqtt.auth.api.AuthenticationMethod;
import javasabr.mqtt.auth.api.AuthenticationProvider;
import javasabr.mqtt.auth.api.CredentialsSource;
import javasabr.mqtt.auth.api.MqttCredentials;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayCollectors;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BasicAuthenticationProvider implements AuthenticationProvider {

  Array<CredentialsSource> credentialsSources;

  public BasicAuthenticationProvider(List<CredentialsSource> credentialsSources) {
    this.credentialsSources = credentialsSources.stream()
        .sorted(comparingInt(credentialsSource -> credentialsSource.getCredentialsSourceType().priority()))
        .collect(ArrayCollectors.toArray(CredentialsSource.class));
  }

  @Override
  public AuthenticationMethod getAuthenticationMethod() {
    return AuthenticationMethod.BASIC;
  }

  @Override
  public Mono<Boolean> authenticate(MqttCredentials credentials) {
    return Flux.fromIterable(credentialsSources)
        .concatMap(credentialsSource -> credentialsSource.isCredentialsExists(credentials))
        .any(Boolean::booleanValue);
  }

  @Override
  public String toString() {
    return "{ \"authenticationMethod\": \"%s\", \"credentialSource\": %s }".formatted(
        getAuthenticationMethod(),
        credentialsSources);
  }
}
