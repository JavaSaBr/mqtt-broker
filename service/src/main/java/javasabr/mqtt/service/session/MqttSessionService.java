package javasabr.mqtt.service.session;

import javasabr.mqtt.network.session.MqttSession;
import reactor.core.publisher.Mono;

public interface MqttSessionService {

  Mono<MqttSession> restore(String clientId);

  Mono<MqttSession> create(String clientId);

  Mono<Boolean> store(String clientId, MqttSession session, long expiryInterval);
}
