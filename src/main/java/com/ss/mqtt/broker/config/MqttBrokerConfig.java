package com.ss.mqtt.broker.config;

import com.ss.mqtt.broker.handler.client.DefaultMqttClientReleaseHandler;
import com.ss.mqtt.broker.handler.client.MqttClientReleaseHandler;
import com.ss.mqtt.broker.handler.packet.in.ConnectInPacketHandler;
import com.ss.mqtt.broker.handler.packet.in.DisconnetInPacketHandler;
import com.ss.mqtt.broker.handler.packet.in.PacketInHandler;
import com.ss.mqtt.broker.handler.packet.in.PublishAckInPacketHandler;
import com.ss.mqtt.broker.handler.packet.in.PublishCompleteInPacketHandler;
import com.ss.mqtt.broker.handler.packet.in.PublishInPacketHandler;
import com.ss.mqtt.broker.handler.packet.in.PublishReceiveInPacketHandler;
import com.ss.mqtt.broker.handler.packet.in.PublishReleaseInPacketHandler;
import com.ss.mqtt.broker.handler.packet.in.SubscribeInPacketHandler;
import com.ss.mqtt.broker.handler.packet.in.UnsubscribeInPacketHandler;
import com.ss.mqtt.broker.handler.publish.in.PublishInHandler;
import com.ss.mqtt.broker.handler.publish.in.Qos0PublishInHandler;
import com.ss.mqtt.broker.handler.publish.in.Qos1PublishInHandler;
import com.ss.mqtt.broker.handler.publish.in.Qos2PublishInHandler;
import com.ss.mqtt.broker.handler.publish.out.PublishOutHandler;
import com.ss.mqtt.broker.handler.publish.out.Qos0PublishOutHandler;
import com.ss.mqtt.broker.handler.publish.out.Qos1PublishOutHandler;
import com.ss.mqtt.broker.handler.publish.out.Qos2PublishOutHandler;
import com.ss.mqtt.broker.network.packet.PacketType;
import com.ss.mqtt.broker.service.AuthenticationService;
import com.ss.mqtt.broker.service.ClientIdRegistry;
import com.ss.mqtt.broker.service.CredentialSource;
import com.ss.mqtt.broker.service.MqttSessionService;
import com.ss.mqtt.broker.service.PublishingService;
import com.ss.mqtt.broker.service.SubscriptionService;
import com.ss.mqtt.broker.service.impl.DefaultPublishingService;
import com.ss.mqtt.broker.service.impl.FileCredentialsSource;
import com.ss.mqtt.broker.service.impl.InMemoryClientIdRegistry;
import com.ss.mqtt.broker.service.impl.InMemoryMqttSessionService;
import com.ss.mqtt.broker.service.impl.SimpleAuthenticationService;
import com.ss.mqtt.broker.service.impl.SimpleSubscriptionService;
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
