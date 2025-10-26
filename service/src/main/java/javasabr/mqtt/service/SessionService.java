package javasabr.mqtt.service;

import javasabr.mqtt.network.MqttSession;
import reactor.core.publisher.Mono;

public interface SessionService {

  Mono<MqttSession> restore(String clientId);

  Mono<MqttSession> create(String clientId);

  Mono<Boolean> store(String clientId, MqttSession session, long expiryInterval);
}
