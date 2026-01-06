package javasabr.mqtt.auth.service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javasabr.mqtt.auth.api.AuthenticationMethod;
import javasabr.mqtt.auth.api.AuthenticationProvider;
import javasabr.mqtt.auth.api.AuthenticationService;
import javasabr.mqtt.auth.api.MqttCredentials;
import javasabr.mqtt.auth.api.exception.AuthenticationConfigException;
import javasabr.mqtt.auth.service.config.property.DefaultProviderProperties;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

@CustomLog
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DefaultAuthenticationService implements AuthenticationService {

  private static Mono<? extends Boolean> onAnonymousProviderErrorHandler(Throwable exception) {
    log.error("Anonymous authentication provider threw an error: %s".formatted(exception.getMessage()));
    return Mono.just(false);
  }

  private static Mono<? extends Boolean> onAuthenticationProviderErrorHandler(
      @Nullable AuthenticationProvider provider,
      Throwable exception) {
    String authenticationMethod = provider == null ? null : provider.getAuthenticationMethod().value();
    log.error("%s authentication provider threw an error: %s".formatted(authenticationMethod, exception.getMessage()));
    return Mono.just(false);
  }

  Map<AuthenticationMethod, AuthenticationProvider> availableProviders;
  @Nullable AuthenticationProvider defaultProvider;
  AuthenticationProvider anonymousProvider;

  public DefaultAuthenticationService(
      List<AuthenticationProvider> configuredProviders,
      DefaultProviderProperties defaultProviderProperties) {

    if (configuredProviders.isEmpty()) {
      throw new AuthenticationConfigException("Authenticator providers are not configured");
    }
    this.availableProviders = configuredProviders.stream()
        .collect(Collectors.toMap(
            AuthenticationProvider::getAuthenticationMethod,
            Function.identity(),
            DefaultAuthenticationService::onDuplicateProviderErrorHandler,
            () -> new EnumMap<>(AuthenticationMethod.class)));

    this.anonymousProvider = availableProviders.get(AuthenticationMethod.ANONYMOUS);
    AuthenticationMethod defaultMethod = defaultProviderProperties.method();
    if (defaultMethod == null && anonymousProvider == null) {
      throw new AuthenticationConfigException("Default authenticator method is not configured");
    }
    this.defaultProvider = defaultMethod == null ? null : availableProviders.get(defaultMethod);
    if (defaultProvider == null && anonymousProvider == null) {
      throw new AuthenticationConfigException("Default [%s] authentication provider is not configured"
          .formatted(defaultMethod.value()));
    }

    log.info(this.availableProviders, DefaultAuthenticationService::buildServiceDescription);
  }

  private static AuthenticationProvider onDuplicateProviderErrorHandler(
      AuthenticationProvider first,
      AuthenticationProvider second) {
    throw new AuthenticationConfigException("There are several [%s] authentication providers"
        .formatted(first.getAuthenticationMethod()));
  }

  @Override
  public Mono<Boolean> authenticate(MqttCredentials request) {
    AuthenticationMethod authenticationMethod = request.authenticationMethod();
    AuthenticationProvider targetProvider =
        authenticationMethod == null ? defaultProvider : availableProviders.get(authenticationMethod);
    return Mono.justOrEmpty(anonymousProvider)
        .flatMap(provider -> provider.authenticate(request))
        .onErrorResume(DefaultAuthenticationService::onAnonymousProviderErrorHandler)
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.justOrEmpty(targetProvider)
            .flatMap(provider -> provider.authenticate(request))
            .onErrorResume(exception -> onAuthenticationProviderErrorHandler(targetProvider, exception))
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
