package javasabr.mqtt.auth.service;

import javasabr.mqtt.auth.api.AuthRequest;
import javasabr.mqtt.auth.api.AuthenticationService;
import javasabr.mqtt.auth.api.AuthenticationProvider;
import javasabr.rlib.collections.dictionary.RefToRefDictionary;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DefaultAuthenticationService implements AuthenticationService {

  RefToRefDictionary<String, AuthenticationProvider> providers;
  AuthenticationProvider defaultProvider;

  @Override
  public Mono<Boolean> authenticate(AuthRequest request) {
    String username = request.username();
    return providers.getOrDefault(request.authenticationMethod(), defaultProvider)
        .authenticate(username, request.password(), request.authenticationData());
  }
}
