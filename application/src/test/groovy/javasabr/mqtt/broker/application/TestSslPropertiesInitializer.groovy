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
        "mqtt.tls.keystore-path": sslContexts.serverKeystorePath.toString(),
        "mqtt.tls.keystore-password": sslContexts.password,
        "mqtt.tls.truststore-path": sslContexts.truststore.toString(),
        "mqtt.tls.truststore-password": sslContexts.password
    ]
    applicationContext.environment.propertySources.addFirst(new MapPropertySource("tlsProps", props))
  }
}
