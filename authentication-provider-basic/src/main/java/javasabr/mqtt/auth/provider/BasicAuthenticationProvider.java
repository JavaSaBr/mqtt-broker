package javasabr.mqtt.auth.provider;

import static java.util.Comparator.comparingInt;

import java.util.List;
import javasabr.mqtt.auth.api.AuthenticationMethod;
import javasabr.mqtt.auth.api.AuthenticationProvider;
import javasabr.mqtt.auth.api.CredentialsSource;
import javasabr.mqtt.auth.api.MqttCredentials;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayCollectors;
import javasabr.rlib.common.util.ArrayUtils;
import javasabr.rlib.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BasicAuthenticationProvider implements AuthenticationProvider {

  private static final byte MIN_PRINTABLE_ASCII = ' '; // 32
  private static final byte MAX_PRINTABLE_ASCII = '~'; // 126

  Array<CredentialsSource> credentialsSources;

  public BasicAuthenticationProvider(List<CredentialsSource> credentialsSources) {
    this.credentialsSources = credentialsSources.stream()
        .sorted(comparingInt(credentialsSource -> credentialsSource.getType().priority()))
        .collect(ArrayCollectors.toArray(CredentialsSource.class));
  }

  @Override
  public AuthenticationMethod getAuthenticationMethod() {
    return AuthenticationMethod.BASIC;
  }

  @Override
  public Mono<Boolean> authenticate(MqttCredentials credentials) {
    return Flux.fromIterable(credentialsSources)
        .concatMap(credentialsSource -> credentialsSource.isCredentialsValid(credentials))
        .any(Boolean::booleanValue);
  }

  @Override
  public String toString() {
    return "{ \"authenticationMethod\": \"%s\", \"credentialSource\": %s }".formatted(
        getAuthenticationMethod(),
        credentialsSources);
  }

  @Override
  public boolean supports(MqttCredentials credentials) {
    byte[] password = credentials.password();
    if (StringUtils.isEmpty(credentials.username()) || ArrayUtils.isEmpty(password)) {
      return false;
    }
    for (byte charByte : password) {
      if (charByte < MIN_PRINTABLE_ASCII || charByte > MAX_PRINTABLE_ASCII) {
        return false;
      }
    }
    return true;
  }
}
