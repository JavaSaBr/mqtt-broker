package javasabr.mqtt.auth.api;

import javasabr.rlib.common.util.StringUtils;
import org.jspecify.annotations.Nullable;
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
}
