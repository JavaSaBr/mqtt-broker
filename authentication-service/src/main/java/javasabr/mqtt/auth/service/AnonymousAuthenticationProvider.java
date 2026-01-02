package javasabr.mqtt.auth.service;

import com.fasterxml.jackson.annotation.JsonValue;
import javasabr.mqtt.auth.api.AuthenticationProvider;
import javasabr.mqtt.auth.api.MqttCredentials;
import javasabr.mqtt.auth.api.AuthenticationMethod;
import javasabr.rlib.common.util.StringUtils;
import reactor.core.publisher.Mono;

public class AnonymousAuthenticationProvider implements AuthenticationProvider {
  @Override
  public AuthenticationMethod getAuthenticationMethod() {
    return AuthenticationMethod.ANONYMOUS;
  }

  @Override
  public Mono<Boolean> authenticate(MqttCredentials credentials) {
    return Mono.just(StringUtils.isEmpty(credentials.username()));
  }

  @Override
  public String toString() {
    return "{ \"authenticationMethod\": \"%s\", \"enabled\": true }".formatted(getAuthenticationMethod());
  }

  @JsonValue
  public String jsonDebugValue() {
    return toString();
  }
}
