package javasabr.mqtt.auth.service;

import java.util.Comparator;
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
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayCollectors;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@CustomLog
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DefaultAuthenticationService implements AuthenticationService {

  Map<AuthenticationMethod, AuthenticationProvider> authMethodToProvider;
  Array<AuthenticationProvider> providers;
  boolean allowAnonymous;

  public DefaultAuthenticationService(List<AuthenticationProvider> configuredProviders, boolean allowAnonymous) {
    if (configuredProviders.isEmpty()) {
      throw new AuthenticationConfigException("Authenticator providers are not configured");
    }
    this.allowAnonymous = allowAnonymous;
    this.authMethodToProvider = configuredProviders.stream()
        .collect(Collectors.toMap(
            AuthenticationProvider::getAuthenticationMethod,
            Function.identity(),
            DefaultAuthenticationService::onDuplicateProviderErrorHandler,
            () -> new EnumMap<>(AuthenticationMethod.class)));
    this.providers = configuredProviders.stream()
        .sorted(Comparator.comparingInt(provider -> provider.getAuthenticationMethod().priority()))
        .collect(ArrayCollectors.toArray(AuthenticationProvider.class));
    log.info(this.authMethodToProvider, DefaultAuthenticationService::buildServiceDescription);
  }

  @Override
  public Mono<Boolean> authenticate(MqttCredentials mqttCredentials) {
    if (mqttCredentials.isAnonymous()) {
      return Mono.just(allowAnonymous);
    } else if (mqttCredentials.isMethodDefined()) {
      AuthenticationMethod authenticationMethod = mqttCredentials.authenticationMethod();
      AuthenticationProvider provider = authMethodToProvider.get(authenticationMethod);
      return provider == null
             ? onAuthenticationProviderNotFoundHandler(mqttCredentials)
             : tryToAuthenticate(provider, mqttCredentials);
    } else {
      return Flux.fromIterable(providers)
          .filter(provider -> provider.supports(mqttCredentials))
          .concatMap(provider -> tryToAuthenticate(provider, mqttCredentials))
          .any(Boolean::booleanValue);
    }
  }

  private Mono<Boolean> onAuthenticationProviderNotFoundHandler(MqttCredentials request) {
    log.debug(request.clientId(), request.authenticationMethod(),
        "%s Uses unsupported authentication method '%s'"::formatted);
    return Mono.just(false);
  }

  private Mono<Boolean> tryToAuthenticate(AuthenticationProvider provider, MqttCredentials mqttCredentials) {
    return provider.authenticate(mqttCredentials)
        .onErrorResume(exception -> onAuthenticationProviderErrorHandler(provider, mqttCredentials, exception));
  }

  private static AuthenticationProvider onDuplicateProviderErrorHandler(
      AuthenticationProvider first,
      AuthenticationProvider second) {
    throw new AuthenticationConfigException("Duplicate authentication provider [%s] found"
        .formatted(first.getAuthenticationMethod()));
  }

  private static Mono<? extends Boolean> onAuthenticationProviderErrorHandler(
      AuthenticationProvider provider,
      MqttCredentials mqttCredentials,
      Throwable exception) {
    String clientId = mqttCredentials.clientId();
    String authenticationMethod = provider.getAuthenticationMethod().value();
    log.error(
        clientId,
        authenticationMethod,
        exception.getMessage(),
        "%s Authentication provider '%s' threw an error: %s"::formatted);
    return Mono.just(false);
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
