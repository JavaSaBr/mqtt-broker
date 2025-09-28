package javasabr.mqtt.service;

import reactor.core.publisher.Mono;

public interface AuthenticationService {
  Mono<Boolean> auth(String userName, byte[] password);
}
