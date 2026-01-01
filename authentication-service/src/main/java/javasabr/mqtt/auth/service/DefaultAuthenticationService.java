package javasabr.mqtt.auth.service;

import javasabr.mqtt.auth.api.MqttCredentials;
import javasabr.mqtt.auth.api.AuthenticationProvider;
import javasabr.mqtt.auth.api.AuthenticationService;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@CustomLog
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DefaultAuthenticationService implements AuthenticationService {

  Array<AuthenticationProvider> providers;

  public DefaultAuthenticationService(Array<AuthenticationProvider> providers) {
    this.providers = providers;
    log.info(providers, DefaultAuthenticationService::buildServiceDescription);
  }

  @Override
  public Mono<Boolean> authenticate(MqttCredentials request) {
    return Flux.fromIterable(providers)
        .concatMap(provider -> provider.authenticate(request).onErrorReturn(false))
        .any(Boolean::booleanValue);
  }

  private static String buildServiceDescription(
      Array<AuthenticationProvider> providers) {

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

    return "Loaded total [%s] authentication providers: %s".formatted(providers.size(), builder);
  }
}
