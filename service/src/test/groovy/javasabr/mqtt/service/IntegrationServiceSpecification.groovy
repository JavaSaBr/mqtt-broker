package javasabr.mqtt.service


import javasabr.mqtt.model.MqttClientConnectionConfig
import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.MqttServerConnectionConfig
import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.QoS
import javasabr.mqtt.network.MqttConnection
import javasabr.mqtt.network.handler.MqttClientReleaseHandler
import javasabr.mqtt.service.impl.DefaultMessageOutFactoryService
import javasabr.mqtt.service.impl.DefaultTopicService
import javasabr.mqtt.service.impl.InMemorySubscriptionService
import javasabr.mqtt.service.message.out.factory.Mqtt311MessageOutFactory
import javasabr.mqtt.service.message.out.factory.Mqtt5MessageOutFactory
import javasabr.mqtt.service.session.impl.InMemoryMqttSessionService
import javasabr.rlib.network.Network
import javasabr.rlib.network.ServerNetworkConfig.SimpleServerNetworkConfig
import javasabr.rlib.network.impl.DefaultBufferAllocator
import spock.lang.Shared
import spock.lang.Specification

import java.nio.channels.AsynchronousSocketChannel
import java.util.concurrent.atomic.AtomicInteger

class IntegrationServiceSpecification extends Specification {

  @Shared
  def clientIdGenerator = new AtomicInteger();

  @Shared
  def defaultTopicService = new DefaultTopicService()

  @Shared
  def defaultSubscriptionService = new InMemorySubscriptionService()

  @Shared
  def defaultMessageOutFactoryService = new DefaultMessageOutFactoryService([
      new Mqtt311MessageOutFactory(),
      new Mqtt5MessageOutFactory()
  ])

  @Shared
  def defaultBufferAllocator = new DefaultBufferAllocator(SimpleServerNetworkConfig.builder().build())

  @Shared
  def defaultMqttSessionService = new InMemoryMqttSessionService(60_000);

  @Shared
  def defaultExternalServerConnectionConfig = new MqttServerConnectionConfig(
      QoS.EXACTLY_ONCE,
      MqttProperties.MAXIMUM_MESSAGE_SIZE_DEFAULT,
      MqttProperties.MAXIMUM_STRING_LENGTH,
      MqttProperties.MAXIMUM_BINARY_SIZE,
      MqttProperties.MAXIMUM_TOPIC_LEVELS,
      MqttProperties.SERVER_KEEP_ALIVE_DEFAULT,
      MqttProperties.RECEIVE_MAXIMUM_PUBLISHES_DEFAULT,
      MqttProperties.TOPIC_ALIAS_DEFAULT,
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
          def client = new TestExternalMqttClient(ownedConnection, Mock(MqttClientReleaseHandler))
          client.session(createdSession)
          client.clientId(generatedClientId)
          return client
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
