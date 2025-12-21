package javasabr.mqtt.auth.api;

import reactor.core.publisher.Mono;

public interface AuthenticationProvider {

  String getName();

  Mono<Boolean> authenticate(String username, byte[] password, byte[] data);
}
