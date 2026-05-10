package javasabr.mqtt.service

import javasabr.mqtt.auth.api.AuthenticationService
import javasabr.mqtt.auth.api.MqttCredentials
import javasabr.mqtt.model.MqttClientConnectionConfig
import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.MqttServerConnectionConfig
import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.publish.PublishData
import javasabr.mqtt.network.MqttConnection
import javasabr.mqtt.network.handler.NetworkMqttUserReleaseHandler
import javasabr.mqtt.service.impl.DefaultMessageOutFactoryService
import javasabr.mqtt.service.impl.DefaultTopicService
import javasabr.mqtt.service.impl.DisabledAuthorizationService
import javasabr.mqtt.service.impl.InMemoryClientIdRegistry
import javasabr.mqtt.service.impl.InMemorySubscriptionService
import javasabr.mqtt.service.message.handler.impl.PublishReleaseMqttInMessageHandler
import javasabr.mqtt.service.message.out.factory.Mqtt311MessageOutFactory
import javasabr.mqtt.service.message.out.factory.Mqtt5MessageOutFactory
import javasabr.mqtt.service.publish.impl.DefaultIncomingPublishRouter
import javasabr.mqtt.service.publish.impl.DefaultPublishDispatcher
import javasabr.mqtt.service.publish.impl.InMemoryIncomingPublishStorage
import javasabr.mqtt.service.publish.impl.InMemoryPublishDataStorage
import javasabr.mqtt.service.publish.impl.InMemoryRetainPublishService
import javasabr.mqtt.service.publish.processor.Qos0IncomingPublishProcessor
import javasabr.mqtt.service.publish.processor.Qos1IncomingPublishProcessor
import javasabr.mqtt.service.publish.processor.Qos2IncomingPublishProcessor
import javasabr.mqtt.service.publish.sender.Qos0SubscriberPublishSender
import javasabr.mqtt.service.publish.sender.Qos1SubscriberPublishSender
import javasabr.mqtt.service.publish.sender.Qos2SubscriberPublishSender
import javasabr.mqtt.service.session.MqttSessionService
import javasabr.mqtt.service.session.impl.InMemoryMqttSessionService
import javasabr.mqtt.test.support.BaseSpecification
import javasabr.rlib.network.Network
import javasabr.rlib.network.ServerNetworkConfig.SimpleServerNetworkConfig
import javasabr.rlib.network.impl.DefaultBufferAllocator
import reactor.core.publisher.Mono
import spock.lang.Shared

import java.nio.channels.AsynchronousSocketChannel
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger

abstract class IntegrationServiceSpecification extends BaseSpecification {

  @Shared
  def testPayloadString = "testpayload"
  @Shared
  def testPayloadBytes = testPayloadString.getBytes(StandardCharsets.UTF_8)
  @Shared
  def testPayload = PublishData.wrap(testPayloadBytes)
  
  @Shared
  def clientIdGenerator = new AtomicInteger()

  @Shared
  def defaultTopicService = new DefaultTopicService()
  
  @Shared
  def defaultPublishDataStorage = new InMemoryPublishDataStorage()
  
  @Shared
  def defaultIncomingPublishStorage = new InMemoryIncomingPublishStorage(
      defaultPublishDataStorage,
      1000)
  
  @Shared
  def authorizationService = new DisabledAuthorizationService();
  
  @Shared
  def defaultClientIdRegistry = new InMemoryClientIdRegistry(
      "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_",
      36)
  
  @Shared
  AuthenticationService defaultAuthenticationService = { MqttCredentials _ ->
    return Mono.just(true)
  }
  
  @Shared
  MqttSessionService defaultSessionService = new InMemoryMqttSessionService(1000)

  @Shared
  def defaultMessageOutFactoryService = new DefaultMessageOutFactoryService([
      new Mqtt311MessageOutFactory(),
      new Mqtt5MessageOutFactory()
  ])

  @Shared
  def defaultPublishDispatcher = new DefaultPublishDispatcher([
      new Qos0SubscriberPublishSender(defaultMessageOutFactoryService, defaultIncomingPublishStorage),
      new Qos1SubscriberPublishSender(defaultMessageOutFactoryService, defaultIncomingPublishStorage),
      new Qos2SubscriberPublishSender(defaultMessageOutFactoryService, defaultIncomingPublishStorage)
  ])

  @Shared
  def defaultRetainMessageService = new InMemoryRetainPublishService()

  @Shared
  def defaultSubscriptionService = new InMemorySubscriptionService(authorizationService)

  @Shared
  def qos0IncomingPublishProcessor = new Qos0IncomingPublishProcessor(
      defaultSubscriptionService,
      defaultPublishDispatcher,
      defaultMessageOutFactoryService,
      defaultRetainMessageService,
      defaultIncomingPublishStorage)

  @Shared
  def defaultIncomingPublishRouter = new DefaultIncomingPublishRouter([
      qos0IncomingPublishProcessor,
      new Qos1IncomingPublishProcessor(
          defaultSubscriptionService,
          defaultPublishDispatcher,
          defaultMessageOutFactoryService,
          defaultRetainMessageService,
          defaultIncomingPublishStorage),
      new Qos2IncomingPublishProcessor(
          defaultSubscriptionService,
          defaultPublishDispatcher,
          defaultMessageOutFactoryService,
          defaultRetainMessageService,
          defaultIncomingPublishStorage)
  ])

  @Shared
  def defaultPublishReleaseMqttInMessageHandler = new PublishReleaseMqttInMessageHandler(defaultMessageOutFactoryService)

  @Shared
  def defaultBufferAllocator = new DefaultBufferAllocator(SimpleServerNetworkConfig.builder().build())

  @Shared
  def defaultMqttSessionService = new InMemoryMqttSessionService(60_000)

  @Shared
  def disabledAclService = new DisabledAuthorizationService()

  @Shared
  def defaultExternalServerConnectionConfig = new MqttServerConnectionConfig(
      QoS.EXACTLY_ONCE,
      MqttProperties.MAX_MESSAGE_SIZE_DEFAULT,
      MqttProperties.MAXIMUM_STRING_LENGTH,
      MqttProperties.MAXIMUM_BINARY_SIZE,
      MqttProperties.MAXIMUM_TOPIC_LEVELS,
      MqttProperties.SERVER_KEEP_ALIVE_DEFAULT,
      MqttProperties.RECEIVE_MAX_PUBLISHES_DEFAULT,
      MqttProperties.TOPIC_ALIAS_MAX_DEFAULT,
      true,
      true,
      true,
      true,
      true,
      true)

  def mockedExternalConnection(MqttVersion mqttVersion) {
    return mockedExternalConnection(defaultExternalServerConnectionConfig, mqttVersion)
  }

  def mockedExternalConnection(MqttServerConnectionConfig serverConnectionConfig, MqttVersion mqttVersion) {

    def connection = new MqttConnection(
        Mock(Network),
        Mock(AsynchronousSocketChannel),
        defaultBufferAllocator,
        100,
        serverConnectionConfig,
        { MqttConnection ownedConnection ->
          def generatedClientId = "mockedClient_${clientIdGenerator.incrementAndGet()}"
          def createdSession = defaultMqttSessionService.createClean(generatedClientId).block()
          def user = new TestExternalNetworkMqttUser(ownedConnection, Mock(NetworkMqttUserReleaseHandler))
          user.session(createdSession)
          user.clientId(generatedClientId)
          return user
        })

    connection.configure(new MqttClientConnectionConfig(
        serverConnectionConfig,
        serverConnectionConfig.maxQos(),
        mqttVersion,
        MqttProperties.SESSION_EXPIRY_DURATION_DISABLED,
        serverConnectionConfig.receiveMaxPublishes(),
        serverConnectionConfig.maxMessageSize(),
        serverConnectionConfig.topicAliasMaxValue(),
        0,
        false,
        false))

    return Spy(connection)
  }

  def initMockedExternalConnection() {
    return initMockedExternalConnection(defaultExternalServerConnectionConfig)
  }
  
  def initMockedExternalConnection(MqttServerConnectionConfig serverConnectionConfig) {
    return new MqttConnection(
        Mock(Network),
        Mock(AsynchronousSocketChannel),
        defaultBufferAllocator,
        100,
        serverConnectionConfig,
        { MqttConnection ownedConnection ->
          return new TestExternalNetworkMqttUser(ownedConnection, Mock(NetworkMqttUserReleaseHandler))
        })
  }
}
