package javasabr.mqtt.service.auth.provider;

import javasabr.rlib.common.util.StringUtils;
import reactor.core.publisher.Mono;

public class DenyProvider implements AuthenticationProvider {

  @Override
  public String getAuthMethodName() {
    return StringUtils.EMPTY;
  }

  @Override
  public Mono<Boolean> authenticate(String username, byte[] password, byte[] data) {
    return Mono.just(false);
  }
}
