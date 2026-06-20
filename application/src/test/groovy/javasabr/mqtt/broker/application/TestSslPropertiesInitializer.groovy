package javasabr.mqtt.broker.application

import javasabr.mqtt.test.support.TestSslContexts
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.MapPropertySource

class TestSslPropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  static final TestSslContexts TEST_SSL_CONTEXT = TestSslContexts.getInstance()

  @Override
  void initialize(ConfigurableApplicationContext applicationContext) {
    applicationContext.environment.propertySources.addFirst(new MapPropertySource("tlsProps", getProps()))
  }

  static Map<String, Object> getProps() {
    return [
        "mqtt.external.tls.keystore-path": TEST_SSL_CONTEXT.serverKeystorePath.toString(),
        "mqtt.external.tls.keystore-password": TEST_SSL_CONTEXT.password,
        "mqtt.external.tls.truststore-path": TEST_SSL_CONTEXT.truststore.toString(),
        "mqtt.external.tls.truststore-password": TEST_SSL_CONTEXT.password
    ]
  }
}
