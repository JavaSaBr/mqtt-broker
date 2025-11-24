package javasabr.mqtt.service;

import reactor.core.publisher.Mono;

public interface CredentialSource {

  Mono<Boolean> check(String user, byte[] pass);

  Mono<Boolean> check(byte[] pass);
}
