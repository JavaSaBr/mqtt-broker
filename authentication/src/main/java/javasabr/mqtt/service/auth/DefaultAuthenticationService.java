package javasabr.mqtt.service.auth;

import javasabr.mqtt.model.message.AuthRequest;
import javasabr.mqtt.service.auth.provider.AuthenticationProvider;
import javasabr.rlib.collections.dictionary.RefToRefDictionary;
import javasabr.rlib.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DefaultAuthenticationService implements AuthenticationService {

  RefToRefDictionary<String, AuthenticationProvider> providers;
  AuthenticationProvider defaultProvider;
  boolean allowAnonymousAuth;

  @Override
  public Mono<Boolean> authenticate(AuthRequest request) {
    String username = request.username();
    if (allowAnonymousAuth && StringUtils.isEmpty(username)) {
      return Mono.just(true);
    } else {
      return providers
          .getOrDefault(request.authenticationMethod(), defaultProvider)
          .authenticate(username, request.password(), request.authenticationData());
    }
  }
}
