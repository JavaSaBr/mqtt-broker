package javasabr.mqtt.broker.application

import com.hivemq.client.mqtt.MqttClient
import javasabr.mqtt.broker.application.config.MqttBrokerTestConfig
import org.springframework.boot.env.PropertiesPropertySourceLoader
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.core.io.ClassPathResource
import spock.lang.Specification

class ApplicationPropertiesSpecification extends Specification {

  def loader = new PropertiesPropertySourceLoader()
  def testProperties = loader.load("test-props", new ClassPathResource("application-test.properties")).get(0)

  def contextRunner = new ApplicationContextRunner()
      .withAllowBeanDefinitionOverriding(true)
      .withInitializer { context ->
        context.getEnvironment().getPropertySources().addLast(testProperties)
      }
      .withUserConfiguration(MqttBrokerTestConfig)

  void mqtt3ClientWithProperties(String[] properties, Closure assertion) {
    contextRunner
        .withPropertyValues(properties)
        .run({ assertion(buildMqtt311Client(it.getBean(InetSocketAddress))) })
  }

  void mqtt5ClientWithProperties(String[] properties, Closure assertion) {
    contextRunner
        .withPropertyValues(properties)
        .run({ assertion(buildMqtt5Client(it.getBean(InetSocketAddress))) })
  }

  def buildMqtt5Client(InetSocketAddress networkAddress) {
    return buildMqtt5Client(generateClientId(), networkAddress)
  }

  def buildMqtt5Client(String clientId, InetSocketAddress address) {
    return MqttClient.builder()
        .identifier(clientId)
        .serverHost(address.getHostName())
        .serverPort(address.getPort())
        .useMqttVersion5()
        .addDisconnectedListener {
          println "[${clientId}|mqtt5] disconnected:[${it.cause.message}]"
        }
        .build()
        .toAsync()
  }

  def buildMqtt311Client(InetSocketAddress networkAddress) {
    return buildMqtt311Client(generateClientId(), networkAddress)
  }

  def buildMqtt311Client(String clientId, InetSocketAddress address) {
    return MqttClient.builder()
        .identifier(clientId)
        .serverHost(address.getHostName())
        .serverPort(address.getPort())
        .useMqttVersion3()
        .addDisconnectedListener {
          println "[${clientId}|mqtt311] disconnected:[${it.cause.message}]"
        }
        .build()
        .toAsync()
  }

  def generateClientId() {
    return generateClientId("Default")
  }

  def generateClientId(String prefix) {
    return prefix + "_" + IntegrationSpecification.idGenerator.incrementAndGet()
  }
}
