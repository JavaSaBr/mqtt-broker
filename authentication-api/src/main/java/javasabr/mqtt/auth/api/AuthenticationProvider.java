package javasabr.mqtt.auth.api;

import reactor.core.publisher.Mono;

public interface AuthenticationProvider {

  AuthenticationMethod getAuthenticationMethod();

  Mono<Boolean> authenticate(MqttCredentials credentials);
}
