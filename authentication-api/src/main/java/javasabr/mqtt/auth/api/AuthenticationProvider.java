package javasabr.mqtt.auth.api;

import reactor.core.publisher.Mono;

public interface AuthenticationProvider {

  AuthenticationType getAuthenticationType();

  Mono<Boolean> authenticate(String username, byte[] password, byte[] data);
}
