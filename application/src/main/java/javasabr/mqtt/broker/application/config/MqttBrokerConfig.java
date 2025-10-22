package javasabr.mqtt.broker.application.config;

import javasabr.mqtt.network.handler.MqttClientReleaseHandler;
import javasabr.mqtt.network.handler.PacketInHandler;
import javasabr.mqtt.network.handler.PublishInHandler;
import javasabr.mqtt.network.packet.MqttPacketType;
import javasabr.mqtt.service.AuthenticationService;
import javasabr.mqtt.service.ClientIdRegistry;
import javasabr.mqtt.service.CredentialSource;
import javasabr.mqtt.service.MqttSessionService;
import javasabr.mqtt.service.PublishingService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.handler.client.ExternalMqttClientReleaseHandler;
import javasabr.mqtt.service.handler.in.ConnectInPacketHandler;
import javasabr.mqtt.service.handler.in.DisconnetInPacketHandler;
import javasabr.mqtt.service.handler.in.PublishAckInPacketHandler;
import javasabr.mqtt.service.handler.in.PublishCompleteInPacketHandler;
import javasabr.mqtt.service.handler.in.PublishInPacketHandler;
import javasabr.mqtt.service.handler.in.PublishReceiveInPacketHandler;
import javasabr.mqtt.service.handler.in.PublishReleaseInPacketHandler;
import javasabr.mqtt.service.handler.in.SubscribeInPacketHandler;
import javasabr.mqtt.service.handler.in.UnsubscribeInPacketHandler;
import javasabr.mqtt.service.handler.publish.in.Qos0PublishInHandler;
import javasabr.mqtt.service.handler.publish.in.Qos1PublishInHandler;
import javasabr.mqtt.service.handler.publish.in.Qos2PublishInHandler;
import javasabr.mqtt.service.handler.publish.out.PublishOutHandler;
import javasabr.mqtt.service.handler.publish.out.Qos0PublishOutHandler;
import javasabr.mqtt.service.handler.publish.out.Qos1PublishOutHandler;
import javasabr.mqtt.service.handler.publish.out.Qos2PublishOutHandler;
import javasabr.mqtt.service.impl.DefaultPublishingService;
import javasabr.mqtt.service.impl.FileCredentialsSource;
import javasabr.mqtt.service.impl.InMemoryClientIdRegistry;
import javasabr.mqtt.service.impl.InMemoryMqttSessionService;
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
  MqttSessionService mqttSessionService() {
    return new InMemoryMqttSessionService(env.getProperty("sessions.clean.thread.interval", int.class, 60000));
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
  PacketInHandler[] packetHandlers(
      AuthenticationService authenticationService,
      ClientIdRegistry clientIdRegistry,
      SubscriptionService subscriptionService,
      PublishingService publishingService,
      MqttSessionService mqttSessionService) {

    var handlers = new PacketInHandler[MqttPacketType.INVALID.ordinal()];
    handlers[MqttPacketType.CONNECT.ordinal()] = new ConnectInPacketHandler(
        clientIdRegistry,
        authenticationService,
        mqttSessionService,
        subscriptionService);
    handlers[MqttPacketType.SUBSCRIBE.ordinal()] = new SubscribeInPacketHandler(subscriptionService);
    handlers[MqttPacketType.UNSUBSCRIBE.ordinal()] = new UnsubscribeInPacketHandler(subscriptionService);
    handlers[MqttPacketType.PUBLISH.ordinal()] = new PublishInPacketHandler(publishingService);
    handlers[MqttPacketType.DISCONNECT.ordinal()] = new DisconnetInPacketHandler();
    handlers[MqttPacketType.PUBLISH_ACK.ordinal()] = new PublishAckInPacketHandler();
    handlers[MqttPacketType.PUBLISH_RECEIVED.ordinal()] = new PublishReceiveInPacketHandler();
    handlers[MqttPacketType.PUBLISH_RELEASED.ordinal()] = new PublishReleaseInPacketHandler();
    handlers[MqttPacketType.PUBLISH_COMPLETED.ordinal()] = new PublishCompleteInPacketHandler();

    return handlers;
  }

  @Bean
  MqttClientReleaseHandler mqttClientReleaseHandler(
      ClientIdRegistry clientIdRegistry,
      MqttSessionService mqttSessionService,
      SubscriptionService subscriptionService) {
    return new ExternalMqttClientReleaseHandler(clientIdRegistry, mqttSessionService, subscriptionService);
  }

  @Bean
  SubscriptionService subscriptionService() {
    return new SimpleSubscriptionService();
  }

  @Bean
  PublishOutHandler[] publishOutHandlers() {
    return new PublishOutHandler[]{
        new Qos0PublishOutHandler(),
        new Qos1PublishOutHandler(),
        new Qos2PublishOutHandler()
    };
  }

  @Bean
  PublishInHandler[] publishInHandlers(
      SubscriptionService subscriptionService,
      PublishOutHandler[] publishOutHandlers) {
    return new PublishInHandler[]{
        new Qos0PublishInHandler(subscriptionService, publishOutHandlers),
        new Qos1PublishInHandler(subscriptionService, publishOutHandlers),
        new Qos2PublishInHandler(subscriptionService, publishOutHandlers)
    };
  }

  @Bean
  PublishingService publishingService(PublishInHandler[] publishInHandlers) {
    return new DefaultPublishingService(publishInHandlers);
  }
}
