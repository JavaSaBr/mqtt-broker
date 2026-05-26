package javasabr.mqtt.broker.application.config;

import java.util.Collection;
import javasabr.mqtt.acl.service.conifg.GroovyDslBasedAclServiceSpringConfig;
import javasabr.mqtt.auth.api.AuthenticationService;
import javasabr.mqtt.auth.service.config.AuthenticationServiceSpringConfig;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttServerConnectionConfig;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.network.handler.NetworkMqttUserReleaseHandler;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.MqttPacketCodec;
import javasabr.mqtt.network.user.NetworkMqttUserFactory;
import javasabr.mqtt.service.AuthorizationService;
import javasabr.mqtt.service.ClientIdRegistry;
import javasabr.mqtt.service.ConnectionService;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.TopicService;
import javasabr.mqtt.service.handler.client.ExternalNetworkMqttUserReleaseHandler;
import javasabr.mqtt.service.impl.DefaultConnectionService;
import javasabr.mqtt.service.impl.DefaultMessageOutFactoryService;
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
import javasabr.mqtt.service.publish.IncomingPublishRouter;
import javasabr.mqtt.service.publish.IncomingPublishStorage;
import javasabr.mqtt.service.publish.PublishDataStorage;
import javasabr.mqtt.service.publish.PublishDispatcher;
import javasabr.mqtt.service.publish.RetainPublishService;
import javasabr.mqtt.service.publish.impl.DefaultIncomingPublishRouter;
import javasabr.mqtt.service.publish.impl.DefaultPublishDispatcher;
import javasabr.mqtt.service.publish.impl.InMemoryIncomingPublishStorage;
import javasabr.mqtt.service.publish.impl.InMemoryPublishDataStorage;
import javasabr.mqtt.service.publish.impl.InMemoryRetainPublishService;
import javasabr.mqtt.service.publish.processor.IncomingPublishProcessor;
import javasabr.mqtt.service.publish.processor.Qos0IncomingPublishProcessor;
import javasabr.mqtt.service.publish.processor.Qos1IncomingPublishProcessor;
import javasabr.mqtt.service.publish.processor.Qos2IncomingPublishProcessor;
import javasabr.mqtt.service.publish.sender.Qos0SubscriberPublishSender;
import javasabr.mqtt.service.publish.sender.Qos1SubscriberPublishSender;
import javasabr.mqtt.service.publish.sender.Qos2SubscriberPublishSender;
import javasabr.mqtt.service.publish.sender.SubscriberPublishSender;
import javasabr.mqtt.service.session.MqttSessionService;
import javasabr.mqtt.service.session.impl.InMemoryMqttSessionService;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;

@Import({
    AuthenticationServiceSpringConfig.class,
    GroovyDslBasedAclServiceSpringConfig.class,
    MqttExternalPlainNetworkConfig.class,
    MqttExternalTlsNetworkConfig.class
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
      @Value("${in.memory.session.service.clean.interval.ms:60000}") int cleanInterval,
      @Value("${in.memory.session.service.max.not.expirable.sessions:1000}") int maxNotExpirableSessions,
      @Value("${in.memory.session.service.max.expirable.sessions:1000}") int maxExpirableStoredSessions,
      @Value("${in.memory.session.service.hard.sessions.limit:20000}") int hardSessionsLimit,
      @Value("${in.memory.session.service.cleanup.batch.size:50}") int cleanupBatchSize) {
    return new InMemoryMqttSessionService(
        cleanInterval, 
        maxNotExpirableSessions,
        maxExpirableStoredSessions, 
        hardSessionsLimit, 
        cleanupBatchSize);
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
  SubscriptionService subscriptionService(AuthorizationService authorizationService) {
    return new InMemorySubscriptionService(authorizationService);
  }
  
  @Bean
  IncomingPublishStorage incomingPublishStorage() {
    return new InMemoryIncomingPublishStorage();
  }
  
  @Bean
  PublishDataStorage publishDataStorage() {
    return new InMemoryPublishDataStorage();
  }

  @Bean
  RetainPublishService retainMessageService() {
    return new InMemoryRetainPublishService();
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
  MqttInMessageHandler publishMqttInMessageHandler(
      IncomingPublishRouter incomingPublishRouter,
      MessageOutFactoryService messageOutFactoryService,
      TopicService topicService,
      AuthorizationService authorizationService,
      PublishDataStorage publishDataStorage,
      IncomingPublishStorage incomingPublishStorage) {
    return new PublishMqttInMessageHandler(
        incomingPublishRouter,
        messageOutFactoryService,
        topicService,
        authorizationService,
        publishDataStorage,
        incomingPublishStorage);
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
      TopicService topicService,
      RetainPublishService retainPublishService,
      PublishDispatcher publishDispatcher) {
    return new SubscribeMqttInMessageHandler(
        subscriptionService,
        messageOutFactoryService,
        topicService,
        retainPublishService, 
        publishDispatcher);
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
  SubscriberPublishSender qos0SubscriberPublishSender(
      MessageOutFactoryService messageOutFactoryService,
      IncomingPublishStorage incomingPublishStorage) {
    return new Qos0SubscriberPublishSender(messageOutFactoryService, incomingPublishStorage);
  }

  @Bean
  SubscriberPublishSender qos1SubscriberPublishSender(
      MessageOutFactoryService messageOutFactoryService,
      IncomingPublishStorage incomingPublishStorage) {
    return new Qos1SubscriberPublishSender(messageOutFactoryService, incomingPublishStorage);
  }

  @Bean
  SubscriberPublishSender qos2SubscriberPublishSender(
      MessageOutFactoryService messageOutFactoryService,
      IncomingPublishStorage incomingPublishStorage) {
    return new Qos2SubscriberPublishSender(messageOutFactoryService, incomingPublishStorage);
  }

  @Bean
  PublishDispatcher publishDispatcher(
      Collection<? extends SubscriberPublishSender> knownSubscriberPublishSenders) {
    return new DefaultPublishDispatcher(knownSubscriberPublishSenders);
  }

  @Bean
  IncomingPublishProcessor qos0IncomingPublishProcessor(
      SubscriptionService subscriptionService,
      PublishDispatcher publishDispatcher,
      MessageOutFactoryService messageOutFactoryService,
      RetainPublishService retainPublishService,
      IncomingPublishStorage incomingPublishStorage) {
    return new Qos0IncomingPublishProcessor(
        subscriptionService,
        publishDispatcher,
        messageOutFactoryService, 
        retainPublishService,
        incomingPublishStorage);
  }

  @Bean
  IncomingPublishProcessor qos1IncomingPublishProcessor(
      SubscriptionService subscriptionService,
      PublishDispatcher publishDispatcher,
      MessageOutFactoryService messageOutFactoryService,
      RetainPublishService retainPublishService,
      IncomingPublishStorage incomingPublishStorage) {
    return new Qos1IncomingPublishProcessor(
        subscriptionService,
        publishDispatcher,
        messageOutFactoryService,
        retainPublishService,
        incomingPublishStorage);
  }

  @Bean
  IncomingPublishProcessor qos2IncomingPublishProcessor(
      SubscriptionService subscriptionService,
      PublishDispatcher publishDispatcher,
      MessageOutFactoryService messageOutFactoryService,
      RetainPublishService retainPublishService,
      IncomingPublishStorage incomingPublishStorage) {
    return new Qos2IncomingPublishProcessor(
        subscriptionService, 
        publishDispatcher,
        messageOutFactoryService,
        retainPublishService,
        incomingPublishStorage);
  }

  @Bean
  IncomingPublishRouter incomingPublishRouter(
      Collection<? extends IncomingPublishProcessor> knownIncomingPublishProcessors) {
    return new DefaultIncomingPublishRouter(knownIncomingPublishProcessors);
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
            MqttProperties.MAX_MESSAGE_SIZE_DEFAULT),
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
            MqttProperties.RECEIVE_MAX_PUBLISHES_DEFAULT),
        env.getProperty(
            "mqtt.external.connection.topic.alias.maximum",
            int.class,
            0),
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
  NetworkMqttUserFactory externalClientFactory(NetworkMqttUserReleaseHandler externalNetworkMqttUserReleaseHandler) {
    return new ExternalNetworkMqttUserFactory(externalNetworkMqttUserReleaseHandler);
  }


  @Bean
  MqttPacketCodec mqttPacketCreator() {
    return new MqttPacketCodec();
  }

}
