package javasabr.mqtt.legacy.integration.config

import javasabr.mqtt.legacy.config.MqttBrokerConfig
import javasabr.mqtt.legacy.config.MqttNetworkConfig
import javasabr.mqtt.legacy.network.MqttConnection
import javasabr.rlib.network.server.ServerNetwork
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.PropertySource

import java.util.function.Consumer

@Import([
    MqttBrokerConfig,
    MqttNetworkConfig
])
@Configuration(proxyBeanMethods = false)
@PropertySource("classpath:application-test.properties")
class MqttBrokerTestConfig {

  @Bean
  InetSocketAddress externalNetworkAddress(
      @Qualifier("externalNetwork") ServerNetwork<MqttConnection> externalNetwork,
      @Qualifier("externalConnectionConsumer") Consumer<MqttConnection> externalConnectionConsumer) {
    def address = externalNetwork.start()
    externalNetwork.onAccept(externalConnectionConsumer)
    return address
  }

  @Bean
  InetSocketAddress internalNetworkAddress(
      @Qualifier("internalNetwork") ServerNetwork<MqttConnection> internalNetwork,
      @Qualifier("internalConnectionConsumer") Consumer<MqttConnection> internalConnectionConsumer) {
    def address = internalNetwork.start()
    internalNetwork.onAccept(internalConnectionConsumer)
    return address
  }
}
