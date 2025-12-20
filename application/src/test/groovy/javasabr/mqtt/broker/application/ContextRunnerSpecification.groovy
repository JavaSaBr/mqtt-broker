package javasabr.mqtt.broker.application

import org.springframework.boot.env.PropertiesPropertySourceLoader
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.core.env.PropertySource
import org.springframework.core.io.ClassPathResource
import spock.lang.Specification

abstract class ContextRunnerSpecification extends Specification {

  ApplicationContextRunner contextRunner

  def prepareContext(Class springConfigClass, String applicationPropertiesFile) {
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
    return MqttClientFactory.buildMqtt5Client(generateClientId(), networkAddress)
  }

  def buildMqtt311Client(InetSocketAddress networkAddress) {
    return MqttClientFactory.buildMqtt311Client(generateClientId(), networkAddress)
  }

  def generateClientId() {
    return MqttClientFactory.generateClientId("ApplicationContextRunner")
  }
}
