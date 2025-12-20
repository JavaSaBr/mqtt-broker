package javasabr.mqtt.broker.application

import com.hivemq.client.mqtt.MqttClient
import org.springframework.boot.env.PropertiesPropertySourceLoader
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.core.env.PropertySource
import org.springframework.core.io.ClassPathResource
import spock.lang.Specification

abstract class ApplicationPropertiesSpecification extends Specification {

  ApplicationContextRunner contextRunner

  def applyProperties(Class springConfigClass, String applicationPropertiesFile) {
    PropertySource propertySource = new PropertiesPropertySourceLoader()
        .load("test-props", new ClassPathResource(applicationPropertiesFile)).getFirst()
    contextRunner = new ApplicationContextRunner()
        .withAllowBeanDefinitionOverriding(true)
        .withUserConfiguration(springConfigClass)
        .withInitializer { context ->
          context.getEnvironment().getPropertySources().addLast(propertySource)
        }
  }

  void runContextWithApplicationProperties(String[] properties, Closure clientConstructor, Closure assertion) {
    Objects.requireNonNull(
        contextRunner,
        "ApplicationContextRunner is not initialized. See `ApplicationPropertiesSpecification.applyProperties`")
    contextRunner
        .withPropertyValues(properties)
        .run({ assertion(clientConstructor(it.getBean(InetSocketAddress))) })
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
    return "ApplicationContextRunner_" + IntegrationSpecification.idGenerator.incrementAndGet()
  }
}
