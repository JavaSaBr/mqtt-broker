package javasabr.mqtt.broker.application

import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import javasabr.mqtt.broker.application.config.MqttBrokerTestConfig
import javasabr.mqtt.model.MqttClientConnectionConfig
import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.MqttServerConnectionConfig
import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.network.MqttConnection
import javasabr.mqtt.network.MqttMockClient
import javasabr.mqtt.network.user.ConfigurableNetworkMqttUser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicReference

@TestPropertySource("classpath:application-test.properties")
@SpringJUnitConfig(classes = MqttBrokerTestConfig)
class IntegrationSpecification extends Specification {

  public static final encoding = StandardCharsets.UTF_8
  public static final topicFilter = "topic/Filter"
  public static final publishPayload = "publishPayload".getBytes(encoding)
  public static final clientId = "testClientId"
  public static final keepAlive = 120

  @Autowired
  InetSocketAddress externalNetworkAddress

  @Autowired
  MqttServerConnectionConfig externalConnectionConfig

  def buildExternalMqtt311Client() {
    return buildExternalMqtt311Client(generateClientId())
  }

  def buildExternalMqtt5Client() {
    return buildExternalMqtt5Client(generateClientId())
  }

  def buildExternalMqtt311Client(String clientId) {
    return MqttClientFactory.buildMqtt311Client(clientId, externalNetworkAddress)
  }

  def buildExternalMqtt5Client(String clientId) {
    return MqttClientFactory.buildMqtt5Client(clientId, externalNetworkAddress)
  }

  def generateClientId() {
    return MqttClientFactory.generateClientId("Default")
  }

  def connectWith(Mqtt3AsyncClient client, String user, String pass) {
    return client.connectWith()
        .simpleAuth()
        .username(user)
        .password(pass.getBytes(encoding))
        .applySimpleAuth()
        .send()
        .join()
  }

  def connectWith(Mqtt5AsyncClient client, String user, String pass) {
    return client.connectWith()
        .simpleAuth()
        .username(user)
        .password(pass.getBytes(encoding))
        .applySimpleAuth()
        .send()
        .join()
  }

  def buildMqtt5MockClient() {
    return new MqttMockClient(
        externalNetworkAddress.getHostName(),
        externalNetworkAddress.getPort(),
        mqtt5MockedConnection(externalConnectionConfig)
    )
  }

  def buildMqtt311MockClient() {
    return new MqttMockClient(
        externalNetworkAddress.getHostName(),
        externalNetworkAddress.getPort(),
        mqtt311MockedConnection(externalConnectionConfig)
    )
  }

  def mqtt5MockedConnection(MqttServerConnectionConfig serverConnConfig) {
    MqttClientConnectionConfig clientConnConfig = new MqttClientConnectionConfig(
        serverConnConfig,
        serverConnConfig.maxQos(),
        MqttVersion.MQTT_5,
        MqttProperties.SESSION_EXPIRY_INTERVAL_DISABLED,
        serverConnConfig.receiveMaxPublishes(),
        serverConnConfig.maxMessageSize(),
        serverConnConfig.topicAliasMaxValue(),
        MqttProperties.SERVER_KEEP_ALIVE_DEFAULT,
        false,
        false)
    def connectionRef = new AtomicReference<MqttConnection>()
    def connection = Stub(MqttConnection) {
      isSupported(MqttVersion.MQTT_5) >> true
      isSupported(MqttVersion.MQTT_3_1_1) >> true
      serverConnectionConfig() >> serverConnConfig
      clientConnectionConfig() >> clientConnConfig
      user() >> Stub(ConfigurableNetworkMqttUser) {
        connectionConfig() >> clientConnConfig
        connection() >> connectionRef.get()
        clientId() >> clientId
      }
    }
    connectionRef.set(connection)
    return connection
  }

  def mqtt311MockedConnection(MqttServerConnectionConfig serverConnConfig) {
    MqttClientConnectionConfig clientConnConfig = new MqttClientConnectionConfig(
        serverConnConfig,
        serverConnConfig.maxQos(),
        MqttVersion.MQTT_3_1_1,
        MqttProperties.SESSION_EXPIRY_INTERVAL_DISABLED,
        serverConnConfig.receiveMaxPublishes(),
        serverConnConfig.maxMessageSize(),
        serverConnConfig.topicAliasMaxValue(),
        MqttProperties.SERVER_KEEP_ALIVE_DEFAULT,
        false,
        false)
    def connectionRef = new AtomicReference<MqttConnection>()
    def connection = Stub(MqttConnection) {
      isSupported(MqttVersion.MQTT_5) >> false
      isSupported(MqttVersion.MQTT_3_1_1) >> true
      serverConnectionConfig() >> serverConnConfig
      clientConnectionConfig() >> clientConnConfig
      user() >> Stub(ConfigurableNetworkMqttUser) {
        connectionConfig() >> clientConnConfig
        connection() >> connectionRef.get()
        clientId() >> clientId
      }
    }
    connectionRef.set(connection)
    return connection
  }
}
