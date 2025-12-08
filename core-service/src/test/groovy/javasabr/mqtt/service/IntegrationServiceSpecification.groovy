package javasabr.mqtt.service


import javasabr.mqtt.model.MqttClientConnectionConfig
import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.MqttServerConnectionConfig
import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.QoS
import javasabr.mqtt.network.MqttConnection
import javasabr.mqtt.network.handler.NetworkMqttUserReleaseHandler
import javasabr.mqtt.service.impl.DefaultMessageOutFactoryService
import javasabr.mqtt.service.impl.DefaultPublishDeliveringService
import javasabr.mqtt.service.impl.DefaultPublishReceivingService
import javasabr.mqtt.service.impl.DefaultRetainMessageService
import javasabr.mqtt.service.impl.DefaultTopicService
import javasabr.mqtt.service.impl.InMemorySubscriptionService
import javasabr.mqtt.service.message.handler.impl.PublishReleaseMqttInMessageHandler
import javasabr.mqtt.service.message.out.factory.Mqtt311MessageOutFactory
import javasabr.mqtt.service.message.out.factory.Mqtt5MessageOutFactory
import javasabr.mqtt.service.publish.handler.impl.Qos0MqttPublishInMessageHandler
import javasabr.mqtt.service.publish.handler.impl.Qos0MqttPublishOutMessageHandler
import javasabr.mqtt.service.publish.handler.impl.Qos1MqttPublishInMessageHandler
import javasabr.mqtt.service.publish.handler.impl.Qos1MqttPublishOutMessageHandler
import javasabr.mqtt.service.publish.handler.impl.Qos2MqttPublishInMessageHandler
import javasabr.mqtt.service.publish.handler.impl.Qos2MqttPublishOutMessageHandler
import javasabr.mqtt.service.session.impl.InMemoryMqttSessionService
import javasabr.rlib.network.Network
import javasabr.rlib.network.ServerNetworkConfig.SimpleServerNetworkConfig
import javasabr.rlib.network.impl.DefaultBufferAllocator
import spock.lang.Shared
import spock.lang.Specification

import java.nio.channels.AsynchronousSocketChannel
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger

abstract class IntegrationServiceSpecification extends Specification {

  @Shared
  def testPayload = "testpayload".getBytes(StandardCharsets.UTF_8)

  @Shared
  def clientIdGenerator = new AtomicInteger()

  @Shared
  def defaultTopicService = new DefaultTopicService()

  @Shared
  def defaultMessageOutFactoryService = new DefaultMessageOutFactoryService([
      new Mqtt311MessageOutFactory(),
      new Mqtt5MessageOutFactory()
  ])

  @Shared
  def defaultPublishDeliveringService = new DefaultPublishDeliveringService([
      new Qos0MqttPublishOutMessageHandler(defaultMessageOutFactoryService),
      new Qos1MqttPublishOutMessageHandler(defaultMessageOutFactoryService),
      new Qos2MqttPublishOutMessageHandler(defaultMessageOutFactoryService)
  ])

  @Shared
  def defaultRetainMessageService = new DefaultRetainMessageService(defaultPublishDeliveringService)

  @Shared
  def defaultSubscriptionService = new InMemorySubscriptionService(defaultRetainMessageService)

  @Shared
  def qos0MqttPublishInMessageHandler = new Qos0MqttPublishInMessageHandler(
      defaultSubscriptionService,
      defaultPublishDeliveringService,
      defaultMessageOutFactoryService,
      defaultRetainMessageService)

  @Shared
  def publishReceivingService = new DefaultPublishReceivingService([
      qos0MqttPublishInMessageHandler,
      new Qos1MqttPublishInMessageHandler(
          defaultSubscriptionService,
          defaultPublishDeliveringService,
          defaultMessageOutFactoryService,
          defaultRetainMessageService),
      new Qos2MqttPublishInMessageHandler(
          defaultSubscriptionService,
          defaultPublishDeliveringService,
          defaultMessageOutFactoryService,
          defaultRetainMessageService)
  ])

  @Shared
  def defaultPublishReleaseMqttInMessageHandler = new PublishReleaseMqttInMessageHandler(defaultMessageOutFactoryService)

  @Shared
  def defaultBufferAllocator = new DefaultBufferAllocator(SimpleServerNetworkConfig.builder().build())

  @Shared
  def defaultMqttSessionService = new InMemoryMqttSessionService(60_000)

  @Shared
  def defaultExternalServerConnectionConfig = new MqttServerConnectionConfig(
      QoS.EXACTLY_ONCE,
      MqttProperties.MAXIMUM_MESSAGE_SIZE_DEFAULT,
      MqttProperties.MAXIMUM_STRING_LENGTH,
      MqttProperties.MAXIMUM_BINARY_SIZE,
      MqttProperties.MAXIMUM_TOPIC_LEVELS,
      MqttProperties.SERVER_KEEP_ALIVE_DEFAULT,
      MqttProperties.RECEIVE_MAXIMUM_PUBLISHES_DEFAULT,
      MqttProperties.TOPIC_ALIAS_MAX_DEFAULT,
      0,
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
          def createdSession = defaultMqttSessionService.create(generatedClientId).block()
          def user = new TestExternalNetworkMqttUser(ownedConnection, Mock(NetworkMqttUserReleaseHandler))
          user.session(createdSession)
          user.clientId(generatedClientId)
          return user
        })

    connection.configure(new MqttClientConnectionConfig(
        serverConnectionConfig,
        serverConnectionConfig.maxQos(),
        mqttVersion,
        MqttProperties.SESSION_EXPIRY_INTERVAL_DEFAULT,
        serverConnectionConfig.receiveMaxPublishes(),
        serverConnectionConfig.maxMessageSize(),
        serverConnectionConfig.topicAliasMaxValue(),
        0,
        false,
        false))

    return Spy(connection)
  }
}
