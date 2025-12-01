package javasabr.mqtt.service.session;

import javasabr.mqtt.network.session.NetworkMqttSession;
import reactor.core.publisher.Mono;

public interface MqttSessionService {

  Mono<NetworkMqttSession> restore(String clientId);

  Mono<NetworkMqttSession> create(String clientId);

  Mono<Boolean> store(String clientId, NetworkMqttSession session, long expiryInterval);
}
