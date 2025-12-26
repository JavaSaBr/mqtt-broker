package javasabr.mqtt.auth.service;

import com.fasterxml.jackson.annotation.JsonValue;
import javasabr.mqtt.auth.api.AuthenticationProvider;
import javasabr.mqtt.auth.api.AuthenticationType;
import javasabr.rlib.common.util.StringUtils;
import reactor.core.publisher.Mono;

public class AnonymousAuthenticationProvider implements AuthenticationProvider {
  @Override
  public AuthenticationType getAuthenticationType() {
    return AuthenticationType.ANONYMOUS;
  }

  @Override
  public Mono<Boolean> authenticate(String username, byte[] password, String authenticationMethod, byte[] data) {
    return Mono.just(StringUtils.isEmpty(username));
  }

  @JsonValue
  @Override
  public String toString() {
    return "{ \"authenticationType\": \"%s\", \"enabled\": true }".formatted(getAuthenticationType());
  }
}
