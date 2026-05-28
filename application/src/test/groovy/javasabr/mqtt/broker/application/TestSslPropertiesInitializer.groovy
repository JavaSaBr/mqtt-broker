package javasabr.mqtt.broker.application

import javasabr.mqtt.test.support.TestSslContexts
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.MapPropertySource

class TestSslPropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  TestSslContexts sslContexts = TestSslContexts.getInstance()

  @Override
  void initialize(ConfigurableApplicationContext applicationContext) {
    def props = [
        "mqtt.external.tls.keystore-path": sslContexts.serverKeystorePath.toString(),
        "mqtt.external.tls.keystore-password": sslContexts.password,
        "mqtt.external.tls.truststore-path": sslContexts.truststore.toString(),
        "mqtt.external.tls.truststore-password": sslContexts.password
    ]
    applicationContext.environment.propertySources.addFirst(new MapPropertySource("tlsProps", props))
  }
}
