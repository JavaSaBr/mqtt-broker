package javasabr.mqtt.broker.application.config;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import javasabr.mqtt.acl.service.conifg.GroovyDslBasedAclServiceSpringConfig;
import javasabr.mqtt.auth.api.AuthenticationService;
import javasabr.mqtt.auth.service.config.AuthenticationServiceSpringConfig;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttServerConnectionConfig;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.MqttConnectionFactory;
import javasabr.mqtt.network.handler.NetworkMqttUserReleaseHandler;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.in.PublishMqttInMessage;
import javasabr.mqtt.network.user.NetworkMqttUserFactory;
import javasabr.mqtt.service.AuthorizationService;
import javasabr.mqtt.service.ClientIdRegistry;
import javasabr.mqtt.service.ConnectionService;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.PublishDeliveringService;
import javasabr.mqtt.service.PublishReceivingService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.TopicService;
import javasabr.mqtt.service.handler.client.ExternalNetworkMqttUserReleaseHandler;
import javasabr.mqtt.service.impl.DefaultConnectionService;
import javasabr.mqtt.service.impl.DefaultMessageOutFactoryService;
import javasabr.mqtt.service.impl.DefaultMqttConnectionFactory;
import javasabr.mqtt.service.impl.DefaultPublishDeliveringService;
import javasabr.mqtt.service.impl.DefaultPublishReceivingService;
import javasabr.mqtt.service.impl.DefaultTopicService;
import javasabr.mqtt.service.impl.DisabledAuthorizationService;
import javasabr.mqtt.service.impl.ExternalNetworkMqttUserFactory;
import javasabr.mqtt.service.impl.InMemoryClientIdRegistry;
import javasabr.mqtt.service.impl.InMemorySubscriptionService;
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
import javasabr.mqtt.service.message.validator.MqttInMessageFieldValidator;
import javasabr.mqtt.service.message.validator.PublishMessageExpiryIntervalMqttInMessageFieldValidator;
import javasabr.mqtt.service.message.validator.PublishPayloadMqttInMessageFieldValidator;
import javasabr.mqtt.service.message.validator.PublishQosMqttInMessageFieldValidator;
import javasabr.mqtt.service.message.validator.PublishResponseTopicMqttInMessageFieldValidator;
import javasabr.mqtt.service.message.validator.PublishRetainMqttInMessageFieldValidator;
import javasabr.mqtt.service.message.validator.PublishTopicAliasMqttInMessageFieldValidator;
import javasabr.mqtt.service.publish.handler.MqttPublishInMessageHandler;
import javasabr.mqtt.service.publish.handler.MqttPublishOutMessageHandler;
import javasabr.mqtt.service.publish.handler.impl.Qos0MqttPublishInMessageHandler;
import javasabr.mqtt.service.publish.handler.impl.Qos0MqttPublishOutMessageHandler;
import javasabr.mqtt.service.publish.handler.impl.Qos1MqttPublishInMessageHandler;
import javasabr.mqtt.service.publish.handler.impl.Qos1MqttPublishOutMessageHandler;
import javasabr.mqtt.service.publish.handler.impl.Qos2MqttPublishInMessageHandler;
import javasabr.mqtt.service.publish.handler.impl.Qos2MqttPublishOutMessageHandler;
import javasabr.mqtt.service.session.MqttSessionService;
import javasabr.mqtt.service.session.impl.InMemoryMqttSessionService;
import javasabr.rlib.network.NetworkFactory;
import javasabr.rlib.network.ServerNetworkConfig;
import javasabr.rlib.network.server.ServerNetwork;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;

@Import({
    GroovyDslBasedAclServiceSpringConfig.class,
    AuthenticationServiceSpringConfig.class
})
@CustomLog
@Configuration(proxyBeanMethods = false)
@PropertySources({
    @PropertySource("classpath:application.properties"),
    @PropertySource(value = "file:./application.properties", ignoreResourceNotFound = true),
    @PropertySource(value = "${BROKER_CONFIG}", ignoreResourceNotFound = true)
})
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
  MqttSessionService mqttSessionService(
      @Value("${sessions.clean.thread.interval:60000}") int cleanInterval) {
    return new InMemoryMqttSessionService(cleanInterval);
  }

  @Bean
  @ConditionalOnProperty(
      name = "acl.engine.type", 
      havingValue = "disabled", 
      matchIfMissing = true)
  AuthorizationService authorizationService() {
    return new DisabledAuthorizationService();
  }

  @Bean
  SubscriptionService subscriptionService() {
    return new InMemorySubscriptionService();
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
  TopicService topicService() {
    return new DefaultTopicService();
  }

  @Bean
  MqttInMessageHandler connectInMqttInMessageHandler(
      ClientIdRegistry clientIdRegistry,
      AuthenticationService authenticationService,
      MqttSessionService sessionService,
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
  MqttInMessageHandler publishAckMqttInMessageHandler(MessageOutFactoryService messageOutFactoryService) {
    return new PublishAckMqttInMessageHandler(messageOutFactoryService);
  }

  @Bean
  MqttInMessageHandler publishCompleteMqttInMessageHandler(MessageOutFactoryService messageOutFactoryService) {
    return new PublishCompleteMqttInMessageHandler(messageOutFactoryService);
  }

  @Bean
  PublishPayloadMqttInMessageFieldValidator publishPayloadMqttInMessageFieldValidator(
      MessageOutFactoryService messageOutFactoryService) {
    return new PublishPayloadMqttInMessageFieldValidator(messageOutFactoryService);
  }

  @Bean
  PublishQosMqttInMessageFieldValidator publishQosMqttInMessageFieldValidator(
      MessageOutFactoryService messageOutFactoryService) {
    return new PublishQosMqttInMessageFieldValidator(messageOutFactoryService);
  }

  @Bean
  PublishRetainMqttInMessageFieldValidator publishRetainMqttInMessageFieldValidator(
      MessageOutFactoryService messageOutFactoryService) {
    return new PublishRetainMqttInMessageFieldValidator(messageOutFactoryService);
  }

  @Bean
  PublishMessageExpiryIntervalMqttInMessageFieldValidator publishMessageExpiryIntervalMqttInMessageFieldValidator(
      MessageOutFactoryService messageOutFactoryService) {
    return new PublishMessageExpiryIntervalMqttInMessageFieldValidator(messageOutFactoryService);
  }

  @Bean
  PublishResponseTopicMqttInMessageFieldValidator publishResponseTopicMqttInMessageFieldValidator(
      MessageOutFactoryService messageOutFactoryService) {
    return new PublishResponseTopicMqttInMessageFieldValidator(messageOutFactoryService);
  }

  @Bean
  PublishTopicAliasMqttInMessageFieldValidator publishTopicAliasMqttInMessageFieldValidator(
      MessageOutFactoryService messageOutFactoryService) {
    return new PublishTopicAliasMqttInMessageFieldValidator(messageOutFactoryService);
  }

  @Bean
  MqttInMessageHandler publishMqttInMessageHandler(
      PublishReceivingService publishReceivingService,
      MessageOutFactoryService messageOutFactoryService,
      TopicService topicService,
      AuthorizationService authorizationService,
      List<? extends MqttInMessageFieldValidator<? super ExternalNetworkMqttUser, PublishMqttInMessage>> fieldValidators) {
    return new PublishMqttInMessageHandler(
        publishReceivingService,
        messageOutFactoryService,
        topicService,
        authorizationService,
        fieldValidators);
  }

  @Bean
  MqttInMessageHandler publishReceiveMqttInMessageHandler(MessageOutFactoryService messageOutFactoryService) {
    return new PublishReceiveMqttInMessageHandler(messageOutFactoryService);
  }

  @Bean
  MqttInMessageHandler publishReleaseMqttInMessageHandler(MessageOutFactoryService messageOutFactoryService) {
    return new PublishReleaseMqttInMessageHandler(messageOutFactoryService);
  }

  @Bean
  MqttInMessageHandler disconnectMqttInMessageHandler(MessageOutFactoryService messageOutFactoryService) {
    return new DisconnectMqttInMessageHandler(messageOutFactoryService);
  }

  @Bean
  MqttInMessageHandler subscribeMqttInMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService,
      TopicService topicService) {
    return new SubscribeMqttInMessageHandler(subscriptionService, messageOutFactoryService, topicService);
  }

  @Bean
  MqttInMessageHandler unsubscribeMqttInMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService,
      TopicService topicService) {
    return new UnsubscribeMqttInMessageHandler(
        subscriptionService,
        messageOutFactoryService,
        topicService);
  }

  @Bean
  ConnectionService externalMqttConnectionService(Collection<? extends MqttInMessageHandler> inMessageHandlers) {
    return new DefaultConnectionService(ExternalNetworkMqttUser.class, inMessageHandlers);
  }

  @Bean
  MqttPublishOutMessageHandler qos0MqttPublishOutMessageHandler(MessageOutFactoryService messageOutFactoryService) {
    return new Qos0MqttPublishOutMessageHandler(messageOutFactoryService);
  }

  @Bean
  MqttPublishOutMessageHandler qos1MqttPublishOutMessageHandler(
      MessageOutFactoryService messageOutFactoryService) {
    return new Qos1MqttPublishOutMessageHandler(messageOutFactoryService);
  }

  @Bean
  MqttPublishOutMessageHandler qos2MqttPublishOutMessageHandler(
      MessageOutFactoryService messageOutFactoryService) {
    return new Qos2MqttPublishOutMessageHandler(messageOutFactoryService);
  }

  @Bean
  PublishDeliveringService publishDeliveringService(
      Collection<? extends MqttPublishOutMessageHandler> knownPublishOutHandlers) {
    return new DefaultPublishDeliveringService(knownPublishOutHandlers);
  }

  @Bean
  MqttPublishInMessageHandler qos0MqttPublishInMessageHandler(
      SubscriptionService subscriptionService,
      PublishDeliveringService publishDeliveringService,
      MessageOutFactoryService messageOutFactoryService) {
    return new Qos0MqttPublishInMessageHandler(
        subscriptionService,
        publishDeliveringService,
        messageOutFactoryService);
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
  NetworkMqttUserReleaseHandler externalMqttClientReleaseHandler(
      ClientIdRegistry clientIdRegistry,
      MqttSessionService sessionService,
      SubscriptionService subscriptionService) {
    return new ExternalNetworkMqttUserReleaseHandler(clientIdRegistry, sessionService, subscriptionService);
  }

  @Bean
  MqttServerConnectionConfig externalConnectionConfig(Environment env) {
    return new MqttServerConnectionConfig(
        QoS.ofCode(env.getProperty("mqtt.connection.max.qos", int.class, 2)),
        env.getProperty(
            "mqtt.external.connection.max.message.size",
            int.class,
            MqttProperties.MAXIMUM_MESSAGE_SIZE_DEFAULT),
        env.getProperty(
            "mqtt.external.connection.max.string.length",
            int.class,
            MqttProperties.MAXIMUM_STRING_LENGTH),
        env.getProperty(
            "mqtt.external.connection.max.binary.size",
            int.class,
            MqttProperties.MAXIMUM_BINARY_SIZE),
        env.getProperty(
            "mqtt.external.connection.max.topic.levels",
            int.class,
            MqttProperties.MAXIMUM_TOPIC_LEVELS),
        env.getProperty(
            "mqtt.external.connection.min.keep.alive",
            int.class,
            MqttProperties.SERVER_KEEP_ALIVE_DEFAULT),
        env.getProperty(
            "mqtt.external.connection.receive.maximum",
            int.class,
            MqttProperties.RECEIVE_MAXIMUM_PUBLISHES_DEFAULT),
        env.getProperty(
            "mqtt.external.connection.topic.alias.maximum",
            int.class,
            0),
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
            false), // set false because currently it's not implemented and we should not allow for clients to use it
        env.getProperty(
            "mqtt.external.connection.wildcard.subscription.available",
            boolean.class,
            MqttProperties.WILDCARD_SUBSCRIPTION_AVAILABLE_DEFAULT),
        env.getProperty(
            "mqtt.external.connection.subscription.id.available",
            boolean.class,
            false), // set false because currently it's not implemented and we should not allow for clients to use it
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
  NetworkMqttUserFactory externalClientFactory(NetworkMqttUserReleaseHandler externalNetworkMqttUserReleaseHandler) {
    return new ExternalNetworkMqttUserFactory(externalNetworkMqttUserReleaseHandler);
  }

  @Bean
  MqttConnectionFactory externalConnectionFactory(
      MqttServerConnectionConfig externalServerConnectionConfig,
      NetworkMqttUserFactory mqttUserFactory,
      @Value("${mqtt.external.connection.max.packets.by.read:100}") int maxPacketsByRead) {
    return new DefaultMqttConnectionFactory(externalServerConnectionConfig, mqttUserFactory, maxPacketsByRead);
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
