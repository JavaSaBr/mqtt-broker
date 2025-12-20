package javasabr.mqtt.auth.api;

import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

public interface AuthenticationProvider {

  String getName();

  Mono<Boolean> authenticate(@Nullable String username, byte[] password, byte[] data);
}
