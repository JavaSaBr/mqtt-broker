package javasabr.mqtt.service.session;

import javasabr.mqtt.network.session.NetworkMqttSession;
import reactor.core.publisher.Mono;

public interface MqttSessionService {

  Mono<NetworkMqttSession> createClean(String clientId);
  
  Mono<NetworkMqttSession> restore(String clientId);

  /**
   * @return async result 'true' if session was stored
   */
  Mono<Boolean> store(String clientId, NetworkMqttSession session);
  
  Mono<Boolean> close(String clientId, NetworkMqttSession session);
}
