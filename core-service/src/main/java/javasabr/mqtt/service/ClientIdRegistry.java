package javasabr.mqtt.service;

import reactor.core.publisher.Mono;

public interface ClientIdRegistry {

  Mono<Boolean> register(String clientId);

  Mono<Boolean> unregister(String clientId);

  boolean validate(String clientId);

  Mono<String> generate();
}
