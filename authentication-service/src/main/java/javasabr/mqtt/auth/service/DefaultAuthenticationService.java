package javasabr.mqtt.auth.service;

import javasabr.mqtt.auth.api.AuthenticationProvider;
import javasabr.mqtt.auth.api.AuthenticationService;
import javasabr.mqtt.auth.api.AuthenticationMethod;
import javasabr.mqtt.auth.api.MqttCredentials;
import javasabr.rlib.collections.dictionary.RefToRefDictionary;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

@CustomLog
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DefaultAuthenticationService implements AuthenticationService {

  RefToRefDictionary<AuthenticationMethod, AuthenticationProvider> availableProviders;
  AuthenticationProvider defaultProvider;
  @Nullable AnonymousAuthenticationProvider anonymousProvider;

  public DefaultAuthenticationService(
      RefToRefDictionary<AuthenticationMethod, AuthenticationProvider> availableProviders,
      AuthenticationProvider defaultProvider,
      @Nullable AnonymousAuthenticationProvider anonymousAuthenticationProvider) {
    this.availableProviders = availableProviders;
    this.defaultProvider = defaultProvider;
    this.anonymousProvider = anonymousAuthenticationProvider;
    log.info(availableProviders, DefaultAuthenticationService::buildServiceDescription);
  }

  @Override
  public Mono<Boolean> authenticate(MqttCredentials request) {
    AuthenticationMethod authenticationMethod = request.authenticationMethod();
    AuthenticationProvider targetProvider =
        authenticationMethod == null ? defaultProvider : availableProviders.get(authenticationMethod);
    return Mono.justOrEmpty(anonymousProvider)
        .flatMap(provider -> provider.authenticate(request))
        .onErrorReturn(false)
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.justOrEmpty(targetProvider)
            .flatMap(provider -> provider.authenticate(request))
            .onErrorReturn(false)
            .defaultIfEmpty(false));
  }

  private static String buildServiceDescription(
      RefToRefDictionary<AuthenticationMethod, AuthenticationProvider> providers) {

    var builder = new StringBuilder()
        .append("[\n");
    for (AuthenticationProvider provider : providers) {
      builder
          .append("  ")
          .append(provider)
          .append(",\n");
    }
    builder
        .delete(builder.length() - 2, builder.length())
        .append("\n]");

    return "Loaded total [%s] authentication method: %s".formatted(providers.size(), builder);
  }
}
