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

  RefToRefDictionary<AuthenticationMethod, AuthenticationProvider> providers;
  AuthenticationProvider defaultProvider;
  @Nullable AnonymousAuthenticationProvider anonymousProvider;

  public DefaultAuthenticationService(
      RefToRefDictionary<AuthenticationMethod, AuthenticationProvider> providers,
      AuthenticationProvider defaultProvider,
      AnonymousAuthenticationProvider anonymousAuthenticationProvider) {
    this.providers = providers;
    this.defaultProvider = defaultProvider;
    this.anonymousProvider = anonymousAuthenticationProvider;
    log.info(providers, DefaultAuthenticationService::buildServiceDescription);
  }

  @Override
  public Mono<Boolean> authenticate(MqttCredentials request) {
    AuthenticationProvider primary = (request.authenticationMethod() == null)
                                     ? defaultProvider
                                     : providers.get(request.authenticationMethod());

    Mono<Boolean> anonymousStep =
        anonymousProvider != null ? anonymousProvider.authenticate(request).onErrorReturn(false) : Mono.just(false);

    return anonymousStep.flatMap(success -> (success || primary == null)
                                            ? Mono.just(success)
                                            : primary.authenticate(request).onErrorReturn(false));
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
