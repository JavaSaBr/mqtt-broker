package javasabr.mqtt.auth.api;

import reactor.core.publisher.Mono;

public interface CredentialsSource {

  DatasourceType getName();

  Mono<Boolean> isCredentialsExists(String userName, byte[] password);
}
