package javasabr.mqtt.auth.service;

import javasabr.mqtt.auth.api.AuthenticationMethod;
import javasabr.mqtt.auth.api.AuthenticationProvider;
import javasabr.mqtt.auth.api.MqttCredentials;
import javasabr.rlib.common.util.ArrayUtils;
import javasabr.rlib.common.util.StringUtils;
import reactor.core.publisher.Mono;

public class AnonymousAuthenticationProvider implements AuthenticationProvider {
  @Override
  public AuthenticationMethod getAuthenticationMethod() {
    return AuthenticationMethod.ANONYMOUS;
  }

  @Override
  public Mono<Boolean> authenticate(MqttCredentials credentials) {
    return Mono.just(StringUtils.isEmpty(credentials.username()) && ArrayUtils.isEmpty(credentials.password()));
  }

  @Override
  public String toString() {
    return "{ \"authenticationMethod\": \"%s\", \"enabled\": true }".formatted(getAuthenticationMethod());
  }
}
