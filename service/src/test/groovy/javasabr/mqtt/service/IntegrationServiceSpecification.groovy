package javasabr.mqtt.service

import javasabr.mqtt.model.*
import javasabr.mqtt.network.MqttConnection
import javasabr.mqtt.network.handler.MqttClientReleaseHandler
import javasabr.mqtt.network.impl.ExternalMqttClient
import javasabr.mqtt.service.impl.DefaultTopicService
import javasabr.mqtt.service.session.impl.InMemoryMqttSessionService
import javasabr.rlib.network.BufferAllocator
import javasabr.rlib.network.Network
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

  def mockedExternalConnection(
      MqttServerConnectionConfig serverConnectionConfig,
      MqttVersion mqttVersion) {

    def connection = new MqttConnection(
        Mock(Network),
        Mock(AsynchronousSocketChannel),
        Mock(BufferAllocator),
        100,
        serverConnectionConfig,
        { MqttConnection connection ->
          def client = new ExternalMqttClient(connection, Mock(MqttClientReleaseHandler))
          def clientId = "mockedClient_${clientIdGenerator.incrementAndGet()}"
          client.clientId(clientId)
          client.session(defaultMqttSessionService.create(clientId).block())
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

    return connection
  }
}
