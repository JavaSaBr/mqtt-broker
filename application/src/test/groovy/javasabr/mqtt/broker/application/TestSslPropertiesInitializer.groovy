package javasabr.mqtt.broker.application

import javasabr.mqtt.test.support.TestSslContexts
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.MapPropertySource

class TestSslPropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  @Override
  void initialize(ConfigurableApplicationContext applicationContext) {
    applicationContext.environment.propertySources.addFirst(new MapPropertySource("tlsProps", getProps()))
  }

  static Map<String, Object> getProps() {
    return [
        "mqtt.external.tls.keystore-path": TestSslContexts.getInstance().serverKeystorePath.toString(),
        "mqtt.external.tls.keystore-password": TestSslContexts.getInstance().password,
        "mqtt.external.tls.truststore-path": TestSslContexts.getInstance().truststore.toString(),
        "mqtt.external.tls.truststore-password": TestSslContexts.getInstance().password
    ]
  }
}
