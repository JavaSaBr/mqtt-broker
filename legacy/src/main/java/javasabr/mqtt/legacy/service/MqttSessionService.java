package javasabr.mqtt.legacy.service;

import javasabr.mqtt.network.MqttSession;
import reactor.core.publisher.Mono;

public interface MqttSessionService {

  Mono<MqttSession> restore(String clientId);

  Mono<MqttSession> create(String clientId);

  Mono<Boolean> store(String clientId, MqttSession session, long expiryInterval);
}
