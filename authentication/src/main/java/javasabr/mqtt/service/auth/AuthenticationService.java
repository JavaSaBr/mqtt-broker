package javasabr.mqtt.service.auth;

import javasabr.mqtt.model.message.AuthRequest;
import reactor.core.publisher.Mono;

public interface AuthenticationService {
  Mono<Boolean> authenticate(AuthRequest authRequest);
}
