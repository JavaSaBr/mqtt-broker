package javasabr.mqtt.broker.application.config;

import java.net.InetSocketAddress;
import java.util.Collection;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttServerConnectionConfig;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.network.MqttClientFactory;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.MqttConnectionFactory;
import javasabr.mqtt.network.handler.MqttClientReleaseHandler;
import javasabr.mqtt.service.AuthenticationService;
import javasabr.mqtt.service.ClientIdRegistry;
import javasabr.mqtt.service.ConnectionService;
import javasabr.mqtt.service.CredentialSource;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.PublishDeliveringService;
import javasabr.mqtt.service.PublishReceivingService;
import javasabr.mqtt.service.SessionService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.handler.client.ExternalMqttClientReleaseHandler;
import javasabr.mqtt.service.impl.DefaultConnectionService;
import javasabr.mqtt.service.impl.DefaultMessageOutFactoryService;
import javasabr.mqtt.service.impl.DefaultMqttConnectionFactory;
import javasabr.mqtt.service.impl.DefaultPublishDeliveringService;
import javasabr.mqtt.service.impl.DefaultPublishReceivingService;
import javasabr.mqtt.service.impl.ExternalMqttClientFactory;
import javasabr.mqtt.service.impl.FileCredentialsSource;
import javasabr.mqtt.service.impl.InMemoryClientIdRegistry;
import javasabr.mqtt.service.impl.InMemorySessionService;
import javasabr.mqtt.service.impl.SimpleAuthenticationService;
import javasabr.mqtt.service.impl.SimpleSubscriptionService;
import javasabr.mqtt.service.message.handler.MqttInMessageHandler;
import javasabr.mqtt.service.message.handler.impl.ConnectInMqttInMessageHandler;
import javasabr.mqtt.service.message.handler.impl.DisconnectMqttInMessageHandler;
import javasabr.mqtt.service.message.handler.impl.PublishAckMqttInMessageHandler;
import javasabr.mqtt.service.message.handler.impl.PublishCompleteMqttInMessageHandler;
import javasabr.mqtt.service.message.handler.impl.PublishMqttInMessageHandler;
import javasabr.mqtt.service.message.handler.impl.PublishReceiveMqttInMessageHandler;
import javasabr.mqtt.service.message.handler.impl.PublishReleaseMqttInMessageHandler;
import javasabr.mqtt.service.message.handler.impl.SubscribeMqttInMessageHandler;
import javasabr.mqtt.service.message.handler.impl.UnsubscribeMqttInMessageHandler;
import javasabr.mqtt.service.message.out.factory.Mqtt311MessageOutFactory;
import javasabr.mqtt.service.message.out.factory.Mqtt5MessageOutFactory;
import javasabr.mqtt.service.message.out.factory.MqttMessageOutFactory;
import javasabr.mqtt.service.publish.handler.MqttPublishInMessageHandler;
import javasabr.mqtt.service.publish.handler.MqttPublishOutMessageHandler;
import javasabr.mqtt.service.publish.handler.impl.Qos0MqttPublishInMessageHandler;
import javasabr.mqtt.service.publish.handler.impl.Qos0MqttPublishOutMessageHandler;
import javasabr.mqtt.service.publish.handler.impl.Qos1MqttPublishInMessageHandler;
import javasabr.mqtt.service.publish.handler.impl.Qos1MqttPublishOutMessageHandler;
import javasabr.mqtt.service.publish.handler.impl.Qos2MqttPublishInMessageHandler;
import javasabr.mqtt.service.publish.handler.impl.Qos2MqttPublishOutMessageHandler;
import javasabr.rlib.network.NetworkFactory;
import javasabr.rlib.network.ServerNetworkConfig;
import javasabr.rlib.network.server.ServerNetwork;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@CustomLog
@Configuration(proxyBeanMethods = false)
public class MqttBrokerSpringConfig {

  @Bean
  ClientIdRegistry clientIdRegistry(Environment env) {
    return new InMemoryClientIdRegistry(
        env.getProperty(
            "client.id.available.chars",
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_"),
        env.getProperty("client.id.max.length", int.class, 36));
  }

  @Bean
  SessionService mqttSessionService(
      @Value("${sessions.clean.thread.interval:60000}") int cleanInterval) {
    return new InMemorySessionService(cleanInterval);
  }

  @Bean
  CredentialSource credentialSource(
      @Value("${credentials.source.file.name:credentials}") String fileName) {
    return new FileCredentialsSource(fileName);
  }

  @Bean
  AuthenticationService authenticationService(
      CredentialSource credentialSource,
      @Value("${authentication.allow.anonymous:false}") boolean allowAnonymousAuth) {
    return new SimpleAuthenticationService(credentialSource, allowAnonymousAuth);
  }

  @Bean
  SubscriptionService subscriptionService() {
    return new SimpleSubscriptionService();
  }

  @Bean
  MqttMessageOutFactory mqtt311MessageOutFactory() {
    return new Mqtt311MessageOutFactory();
  }

  @Bean
  MqttMessageOutFactory mqtt5MessageOutFactory() {
    return new Mqtt5MessageOutFactory();
  }

  @Bean
  MessageOutFactoryService mqttMessageOutFactoryService(
      Collection<? extends MqttMessageOutFactory> knownFactories) {
    return new DefaultMessageOutFactoryService(knownFactories);
  }

  @Bean
  MqttInMessageHandler connectInMqttInMessageHandler(
      ClientIdRegistry clientIdRegistry,
      AuthenticationService authenticationService,
      SessionService sessionService,
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService) {
    return new ConnectInMqttInMessageHandler(
        clientIdRegistry,
        authenticationService,
        sessionService,
        subscriptionService,
        messageOutFactoryService);
  }

  @Bean
  MqttInMessageHandler publishAckMqttInMessageHandler() {
    return new PublishAckMqttInMessageHandler();
  }

  @Bean
  MqttInMessageHandler publishCompleteMqttInMessageHandler() {
    return new PublishCompleteMqttInMessageHandler();
  }

  @Bean
  MqttInMessageHandler publishMqttInMessageHandler(PublishReceivingService publishReceivingService) {
    return new PublishMqttInMessageHandler(publishReceivingService);
  }

  @Bean
  MqttInMessageHandler publishReceiveMqttInMessageHandler() {
    return new PublishReceiveMqttInMessageHandler();
  }

  @Bean
  MqttInMessageHandler publishReleaseMqttInMessageHandler() {
    return new PublishReleaseMqttInMessageHandler();
  }

  @Bean
  MqttInMessageHandler disconnectMqttInMessageHandler() {
    return new DisconnectMqttInMessageHandler();
  }

  @Bean
  MqttInMessageHandler subscribeMqttInMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService) {
    return new SubscribeMqttInMessageHandler(subscriptionService, messageOutFactoryService);
  }

  @Bean
  MqttInMessageHandler unsubscribeMqttInMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService) {
    return new UnsubscribeMqttInMessageHandler(subscriptionService, messageOutFactoryService);
  }

  @Bean
  ConnectionService mqttConnectionService(Collection<? extends MqttInMessageHandler> inMessageHandlers) {
    return new DefaultConnectionService(inMessageHandlers);
  }

  @Bean
  MqttPublishOutMessageHandler qos0MqttPublishOutMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService) {
    return new Qos0MqttPublishOutMessageHandler(subscriptionService, messageOutFactoryService);
  }

  @Bean
  MqttPublishOutMessageHandler qos1MqttPublishOutMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService) {
    return new Qos1MqttPublishOutMessageHandler(subscriptionService, messageOutFactoryService);
  }

  @Bean
  MqttPublishOutMessageHandler qos2MqttPublishOutMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService) {
    return new Qos2MqttPublishOutMessageHandler(subscriptionService, messageOutFactoryService);
  }

  @Bean
  PublishDeliveringService publishDeliveringService(
      Collection<? extends MqttPublishOutMessageHandler> knownPublishOutHandlers) {
    return new DefaultPublishDeliveringService(knownPublishOutHandlers);
  }

  @Bean
  MqttPublishInMessageHandler qos0MqttPublishInMessageHandler(
      SubscriptionService subscriptionService,
      PublishDeliveringService publishDeliveringService) {
    return new Qos0MqttPublishInMessageHandler(subscriptionService, publishDeliveringService);
  }

  @Bean
  MqttPublishInMessageHandler qos1MqttPublishInMessageHandler(
      SubscriptionService subscriptionService,
      PublishDeliveringService publishDeliveringService,
      MessageOutFactoryService messageOutFactoryService) {
    return new Qos1MqttPublishInMessageHandler(
        subscriptionService,
        publishDeliveringService,
        messageOutFactoryService);
  }

  @Bean
  MqttPublishInMessageHandler qos2MqttPublishInMessageHandler(
      SubscriptionService subscriptionService,
      PublishDeliveringService publishDeliveringService,
      MessageOutFactoryService messageOutFactoryService) {
    return new Qos2MqttPublishInMessageHandler(
        subscriptionService,
        publishDeliveringService,
        messageOutFactoryService);
  }

  @Bean
  PublishReceivingService publishReceivingService(
      Collection<? extends MqttPublishInMessageHandler> knownPublishInHandlers) {
    return new DefaultPublishReceivingService(knownPublishInHandlers);
  }

  @Bean
  MqttClientReleaseHandler externalMqttClientReleaseHandler(
      ClientIdRegistry clientIdRegistry,
      SessionService sessionService,
      SubscriptionService subscriptionService) {
    return new ExternalMqttClientReleaseHandler(clientIdRegistry, sessionService, subscriptionService);
  }

  @Bean
  MqttServerConnectionConfig externalConnectionConfig(Environment env) {
    return new MqttServerConnectionConfig(
        QoS.of(env.getProperty("mqtt.connection.max.qos", int.class, 2)),
        env.getProperty(
            "mqtt.external.connection.max.packet.size",
            int.class,
            MqttProperties.MAXIMUM_PACKET_SIZE_DEFAULT),
        env.getProperty(
            "mqtt.external.connection.max.string.length",
            int.class,
            MqttProperties.MAXIMUM_STRING_LENGTH),
        env.getProperty(
            "mqtt.external.connection.max.binary.size",
            int.class,
            MqttProperties.MAXIMUM_BINARY_SIZE),
        env.getProperty(
            "mqtt.external.connection.min.keep.alive",
            int.class,
            MqttProperties.SERVER_KEEP_ALIVE_DEFAULT),
        env.getProperty(
            "mqtt.external.connection.receive.maximum",
            int.class,
            MqttProperties.RECEIVE_MAXIMUM_DEFAULT),
        env.getProperty(
            "mqtt.external.connection.topic.alias.maximum",
            int.class,
            MqttProperties.TOPIC_ALIAS_MAXIMUM_DISABLED),
        env.getProperty(
            "mqtt.external.connection.default.session.expiration.time",
            long.class,
            MqttProperties.SESSION_EXPIRY_INTERVAL_DEFAULT),
        env.getProperty(
            "mqtt.external.connection.keep.alive.enabled",
            boolean.class,
            MqttProperties.KEEP_ALIVE_ENABLED_DEFAULT),
        env.getProperty(
            "mqtt.external.connection.sessions.enabled",
            boolean.class,
            MqttProperties.SESSIONS_ENABLED_DEFAULT),
        env.getProperty(
            "mqtt.external.connection.retain.available",
            boolean.class,
            MqttProperties.RETAIN_AVAILABLE_DEFAULT),
        env.getProperty(
            "mqtt.external.connection.wildcard.subscription.available",
            boolean.class,
            MqttProperties.WILDCARD_SUBSCRIPTION_AVAILABLE_DEFAULT),
        env.getProperty(
            "mqtt.external.connection.subscription.id.available",
            boolean.class,
            MqttProperties.SUBSCRIPTION_IDENTIFIER_AVAILABLE_DEFAULT),
        env.getProperty(
            "mqtt.external.connection.shared.subscription.available",
            boolean.class,
            MqttProperties.SHARED_SUBSCRIPTION_AVAILABLE_DEFAULT));
  }

  @Bean
  ServerNetworkConfig externalNetworkConfig(
      @Value("${mqtt.external.network.read.buffer.size:512}") int readBufferSize,
      @Value("${mqtt.external.network.pending.buffer.size:1024}") int pendingBufferSize,
      @Value("${mqtt.external.network.write.buffer.size:512}") int writeBufferSize,
      @Value("${mqtt.external.network.thread.group.name:ExternalNetwork}") String threadGroupName,
      @Value("${mqtt.external.network.thread.count:1}") int threadGroupMaxSize) {
    return ServerNetworkConfig.SimpleServerNetworkConfig
        .builder()
        .readBufferSize(readBufferSize)
        .pendingBufferSize(pendingBufferSize)
        .writeBufferSize(writeBufferSize)
        .threadGroupName(threadGroupName)
        .threadGroupMaxSize(threadGroupMaxSize)
        .build();
  }

  @Bean
  MqttClientFactory externalClientFactory(MqttClientReleaseHandler externalMqttClientReleaseHandler) {
    return new ExternalMqttClientFactory(externalMqttClientReleaseHandler);
  }

  @Bean
  MqttConnectionFactory externalConnectionFactory(
      MqttServerConnectionConfig externalServerConnectionConfig,
      MqttClientFactory externalClientFactory,
      @Value("${mqtt.external.connection.max.packets.by.read:100}") int maxPacketsByRead) {
    return new DefaultMqttConnectionFactory(externalServerConnectionConfig, externalClientFactory, maxPacketsByRead);
  }

  @Bean
  InetSocketAddress externalNetworkAddress(
      @Value("${mqtt.external.network.host:localhost}") String host,
      @Value("${mqtt.external.network.port:1883}") int port) {
    return new InetSocketAddress(host, port);
  }

  @Bean
  ServerNetwork<MqttConnection> externalNetwork(
      ServerNetworkConfig externalNetworkConfig,
      MqttConnectionFactory externalConnectionFactory) {
    return NetworkFactory.serverNetwork(externalNetworkConfig, externalConnectionFactory::newConnection);
  }

  @Bean
  ApplicationListener<ApplicationStartedEvent> externalNetworkStarter(
      ServerNetwork<MqttConnection> externalNetwork,
      ConnectionService connectionService,
      InetSocketAddress externalNetworkAddress) {
    return _ -> {
      externalNetwork.start(externalNetworkAddress);
      externalNetwork.onAccept(connectionService::processAcceptedConnection);
      log.info(externalNetworkAddress, "Started external MQTT network by address:[%s]"::formatted);
    };
  }
}
