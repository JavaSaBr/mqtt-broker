package javasabr.mqtt.service.session;

import javasabr.mqtt.network.session.MqttNetworkSession;
import reactor.core.publisher.Mono;

public interface MqttSessionService {

  Mono<MqttNetworkSession> restore(String clientId);

  Mono<MqttNetworkSession> create(String clientId);

  Mono<Boolean> store(String clientId, MqttNetworkSession session, long expiryInterval);
}
