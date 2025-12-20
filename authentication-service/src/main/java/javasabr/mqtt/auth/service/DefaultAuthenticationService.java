package javasabr.mqtt.auth.service;

import javasabr.mqtt.auth.api.AuthRequest;
import javasabr.mqtt.auth.api.AuthenticationProvider;
import javasabr.mqtt.auth.api.AuthenticationService;
import javasabr.rlib.collections.dictionary.RefToRefDictionary;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import reactor.core.publisher.Mono;

@CustomLog
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DefaultAuthenticationService implements AuthenticationService {

  RefToRefDictionary<String, AuthenticationProvider> providers;
  AuthenticationProvider defaultProvider;

  public DefaultAuthenticationService(
      RefToRefDictionary<String, AuthenticationProvider> providers,
      AuthenticationProvider defaultProvider) {
    this.providers = providers;
    this.defaultProvider = defaultProvider;
    log.info(providers, defaultProvider, DefaultAuthenticationService::buildServiceDescription);
  }

  @Override
  public Mono<Boolean> authenticate(AuthRequest request) {
    String username = request.username();
    return providers.getOrDefault(request.authenticationMethod(), defaultProvider)
        .authenticate(username, request.password(), request.authenticationData());
  }

  private static String buildServiceDescription(
      RefToRefDictionary<String, AuthenticationProvider> providers,
      AuthenticationProvider defaultProvider) {

    var builder = new StringBuilder();
    builder.append("{\n");
    builder.append("  \"DEFAULT\": \"").append(defaultProvider.getName()).append("\",\n");

    for (AuthenticationProvider provider : providers) {
      builder
          .append("  ")
          .append(provider)
          .append(",")
          .append("\n");
    }
    builder
        .delete(builder.length() - 2, builder.length())
        .append("\n}");

    return "Loaded total [%s] authentication providers: %s".formatted(providers.size(), builder);
  }
}
