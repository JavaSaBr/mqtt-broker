package javasabr.mqtt.broker.application.config;

import javasabr.mqtt.network.handler.MqttClientReleaseHandler;
import javasabr.mqtt.service.AuthenticationService;
import javasabr.mqtt.service.ClientIdRegistry;
import javasabr.mqtt.service.CredentialSource;
import javasabr.mqtt.service.SessionService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.handler.client.ExternalMqttClientReleaseHandler;
import javasabr.mqtt.service.impl.FileCredentialsSource;
import javasabr.mqtt.service.impl.InMemoryClientIdRegistry;
import javasabr.mqtt.service.impl.InMemorySessionService;
import javasabr.mqtt.service.impl.SimpleAuthenticationService;
import javasabr.mqtt.service.impl.SimpleSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class MqttBrokerConfig {

  private final Environment env;

  @Bean
  ClientIdRegistry clientIdRegistry() {
    return new InMemoryClientIdRegistry(
        env.getProperty(
            "client.id.available.chars",
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_"),
        env.getProperty("client.id.max.length", int.class, 36));
  }

  @Bean
  SessionService mqttSessionService() {
    return new InMemorySessionService(env.getProperty("sessions.clean.thread.interval", int.class, 60000));
  }

  @Bean
  CredentialSource credentialSource() {
    return new FileCredentialsSource(env.getProperty("credentials.source.file.name", "credentials"));
  }

  @Bean
  AuthenticationService authenticationService(CredentialSource credentialSource) {
    return new SimpleAuthenticationService(
        credentialSource,
        env.getProperty("authentication.allow.anonymous", boolean.class, false));
  }

  @Bean
  MqttClientReleaseHandler mqttClientReleaseHandler(
      ClientIdRegistry clientIdRegistry,
      SessionService sessionService,
      SubscriptionService subscriptionService) {
    return new ExternalMqttClientReleaseHandler(clientIdRegistry, sessionService, subscriptionService);
  }

  @Bean
  SubscriptionService subscriptionService() {
    return new SimpleSubscriptionService();
  }
}
