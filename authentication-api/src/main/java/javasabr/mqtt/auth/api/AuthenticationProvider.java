package javasabr.mqtt.auth.api;

import reactor.core.publisher.Mono;

public interface AuthenticationProvider {

  AuthenticationMethod getAuthenticationMethod();

  boolean supports(MqttCredentials credentials);

  Mono<Boolean> authenticate(MqttCredentials credentials);
}
