package javasabr.mqtt.broker.application

import org.springframework.boot.env.PropertiesPropertySourceLoader
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.core.env.PropertySource
import org.springframework.core.io.ClassPathResource
import spock.lang.Specification

import static javasabr.mqtt.broker.application.MqttClientFactory.generateClientId

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

  void runContextWithProperties(String[] properties, Closure clientConstructor, Closure assertion) {
    Objects.requireNonNull(
        contextRunner,
        "ApplicationContextRunner is not initialized. See `ApplicationPropertiesSpecification.applyProperties`")
    contextRunner
        .withPropertyValues(properties)
        .run({ ctx ->
          assertion(clientConstructor(generateClientId("ApplicationContextRunner"), ctx.getBean(InetSocketAddress)))
        })
  }
}
