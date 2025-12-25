package javasabr.mqtt.auth.api;

import com.fasterxml.jackson.annotation.JsonValue;
import javasabr.rlib.common.util.StringUtils;
import reactor.core.publisher.Mono;

public class AnonymousAuthenticationProvider implements AuthenticationProvider {
  @Override
  public String getName() {
    return "anonymous";
  }

  @Override
  public Mono<Boolean> authenticate(String username, byte[] password, byte[] data) {
    return Mono.just(StringUtils.isEmpty(username));
  }

  @JsonValue
  @Override
  public String toString() {
    return "\"enabled\"";
  }
}
