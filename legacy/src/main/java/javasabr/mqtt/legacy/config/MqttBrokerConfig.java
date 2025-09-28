package javasabr.mqtt.legacy.config;

import javasabr.mqtt.legacy.handler.client.DefaultMqttClientReleaseHandler;
import javasabr.mqtt.network.handler.client.MqttClientReleaseHandler;
import javasabr.mqtt.legacy.handler.packet.in.ConnectInPacketHandler;
import javasabr.mqtt.legacy.handler.packet.in.DisconnetInPacketHandler;
import javasabr.mqtt.network.handler.packet.in.PacketInHandler;
import javasabr.mqtt.legacy.handler.packet.in.PublishAckInPacketHandler;
import javasabr.mqtt.legacy.handler.packet.in.PublishCompleteInPacketHandler;
import javasabr.mqtt.legacy.handler.packet.in.PublishInPacketHandler;
import javasabr.mqtt.legacy.handler.packet.in.PublishReceiveInPacketHandler;
import javasabr.mqtt.legacy.handler.packet.in.PublishReleaseInPacketHandler;
import javasabr.mqtt.legacy.handler.packet.in.SubscribeInPacketHandler;
import javasabr.mqtt.legacy.handler.packet.in.UnsubscribeInPacketHandler;
import javasabr.mqtt.legacy.handler.publish.in.PublishInHandler;
import javasabr.mqtt.legacy.handler.publish.in.Qos0PublishInHandler;
import javasabr.mqtt.legacy.handler.publish.in.Qos1PublishInHandler;
import javasabr.mqtt.legacy.handler.publish.in.Qos2PublishInHandler;
import javasabr.mqtt.legacy.handler.publish.out.PublishOutHandler;
import javasabr.mqtt.legacy.handler.publish.out.Qos0PublishOutHandler;
import javasabr.mqtt.legacy.handler.publish.out.Qos1PublishOutHandler;
import javasabr.mqtt.legacy.handler.publish.out.Qos2PublishOutHandler;
import javasabr.mqtt.network.packet.PacketType;
import javasabr.mqtt.legacy.service.AuthenticationService;
import javasabr.mqtt.legacy.service.ClientIdRegistry;
import javasabr.mqtt.legacy.service.CredentialSource;
import javasabr.mqtt.legacy.service.MqttSessionService;
import javasabr.mqtt.legacy.service.PublishingService;
import javasabr.mqtt.legacy.service.SubscriptionService;
import javasabr.mqtt.legacy.service.impl.DefaultPublishingService;
import javasabr.mqtt.legacy.service.impl.FileCredentialsSource;
import javasabr.mqtt.legacy.service.impl.InMemoryClientIdRegistry;
import javasabr.mqtt.legacy.service.impl.InMemoryMqttSessionService;
import javasabr.mqtt.legacy.service.impl.SimpleAuthenticationService;
import javasabr.mqtt.legacy.service.impl.SimpleSubscriptionService;
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

    var handlers = new PacketInHandler[PacketType.INVALID.ordinal()];
    handlers[PacketType.CONNECT.ordinal()] = new ConnectInPacketHandler(
        clientIdRegistry,
        authenticationService,
        mqttSessionService,
        subscriptionService);
    handlers[PacketType.SUBSCRIBE.ordinal()] = new SubscribeInPacketHandler(subscriptionService);
    handlers[PacketType.UNSUBSCRIBE.ordinal()] = new UnsubscribeInPacketHandler(subscriptionService);
    handlers[PacketType.PUBLISH.ordinal()] = new PublishInPacketHandler(publishingService);
    handlers[PacketType.DISCONNECT.ordinal()] = new DisconnetInPacketHandler();
    handlers[PacketType.PUBLISH_ACK.ordinal()] = new PublishAckInPacketHandler();
    handlers[PacketType.PUBLISH_RECEIVED.ordinal()] = new PublishReceiveInPacketHandler();
    handlers[PacketType.PUBLISH_RELEASED.ordinal()] = new PublishReleaseInPacketHandler();
    handlers[PacketType.PUBLISH_COMPLETED.ordinal()] = new PublishCompleteInPacketHandler();

    return handlers;
  }

  @Bean
  MqttClientReleaseHandler mqttClientReleaseHandler(
      ClientIdRegistry clientIdRegistry,
      MqttSessionService mqttSessionService,
      SubscriptionService subscriptionService) {
    return new DefaultMqttClientReleaseHandler(clientIdRegistry, mqttSessionService, subscriptionService);
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
