package javasabr.mqtt.legacy.service.impl;

import javasabr.mqtt.legacy.service.AuthenticationService;
import javasabr.mqtt.legacy.service.CredentialSource;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SimpleAuthenticationService implements AuthenticationService {

  private final CredentialSource credentialSource;
  private final boolean allowAnonymousAuth;

  @Override
  public Mono<Boolean> auth(String userName, byte[] password) {
    if (allowAnonymousAuth && userName.isEmpty()) {
      return Mono.just(Boolean.TRUE);
    } else {
      return credentialSource.check(userName, password);
    }
  }
}
