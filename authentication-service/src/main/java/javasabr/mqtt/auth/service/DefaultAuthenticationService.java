package javasabr.mqtt.auth.service;

import static java.util.stream.Collectors.toMap;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javasabr.mqtt.auth.api.AuthenticationMethod;
import javasabr.mqtt.auth.api.AuthenticationProvider;
import javasabr.mqtt.auth.api.AuthenticationService;
import javasabr.mqtt.auth.api.MqttCredentials;
import javasabr.mqtt.auth.api.exception.AuthenticationConfigException;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

@CustomLog
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DefaultAuthenticationService implements AuthenticationService {

  Map<AuthenticationMethod, AuthenticationProvider> availableProviders;
  AuthenticationProvider defaultProvider;
  @Nullable AnonymousAuthenticationProvider anonymousProvider;

  public DefaultAuthenticationService(
      List<AuthenticationProvider> configuredProviders,
      @Nullable AuthenticationMethod defaultMethod,
      @Nullable AnonymousAuthenticationProvider anonymousProvider) {
    if (configuredProviders.isEmpty()) {
      throw new AuthenticationConfigException("Authenticator providers are not configured");
    }
    this.availableProviders = new EnumMap<>(configuredProviders.stream()
        .collect(toMap(AuthenticationProvider::getAuthenticationMethod, Function.identity())));
    this.defaultProvider = Optional.ofNullable(defaultMethod)
        .map(this.availableProviders::get)
        .orElseGet(configuredProviders::getFirst);
    if (defaultProvider == null) {
      throw new AuthenticationConfigException("[%s] method not found".formatted(defaultMethod));
    }
    this.anonymousProvider = anonymousProvider;
    log.info(this.availableProviders, DefaultAuthenticationService::buildServiceDescription);
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

  private static String buildServiceDescription(Map<AuthenticationMethod, AuthenticationProvider> providers) {

    var builder = new StringBuilder()
        .append("[\n");
    for (AuthenticationProvider provider : providers.values()) {
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
